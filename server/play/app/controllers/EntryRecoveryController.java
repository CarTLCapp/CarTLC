/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package controllers;

import play.mvc.*;
import play.data.*;
import play.data.validation.ValidationError;

import models.*;
import modules.WorkerExecutionContext;
import modules.ParseResult;
import modules.Status;

import views.formdata.EntryFormData;
import views.formdata.InputSearch;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import java.util.concurrent.*;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.ObjectMapper;

import modules.AmazonHelper;
import modules.AmazonHelper.OnDownloadComplete;
import modules.Globals;
import modules.TimeHelper;
import modules.StringHelper;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

import play.db.ebean.Transactional;
import play.Logger;
import play.libs.concurrent.HttpExecution;

/**
 * Manage a database of equipment.
 */
public class EntryRecoveryController extends Controller {

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'zzz";
    private static final String EXPORT_FILENAME = "/tmp/export.csv";

    private AmazonHelper mAmazonHelper;
    private EntryRecoveryPagedList mLastEntryList;
    private FormFactory mFormFactory;
    private SimpleDateFormat mDateFormat;
    private Globals mGlobals;
    private WorkerExecutionContext mExecutionContext;
    private ImportRecovery mImportRecovery;
    private boolean mAborted;
    private boolean mImporting;
    private boolean mInstalling;
    private boolean mDeleting;
    private DeleteAction mDeleteAction;
    private long mDownloadPicturesForEntryId;
    private long mDownloadPicturesLastAttemptEntryId;
    private ArrayList<Long> mDownloaded = new ArrayList<>();
    private ArrayList<CompletableFuture<Result>> mDownloadStatusRequests = new ArrayList<>();

    @Inject
    public EntryRecoveryController(AmazonHelper amazonHelper,
                                   FormFactory formFactory,
                                   WorkerExecutionContext executionContext,
                                   Globals globals) {
        mAmazonHelper = amazonHelper;
        mFormFactory = formFactory;
        mDateFormat = new SimpleDateFormat(DATE_FORMAT);
        mGlobals = globals;
        mExecutionContext = executionContext;
    }

    @Security.Authenticated(Secured.class)
    public Result list2(int page, int pageSize, String sortBy, String order, String searchTerm, String searchField) {
        return list(page, pageSize, sortBy, order, searchTerm, searchField);
    }

    @Security.Authenticated(Secured.class)
    public Result list(int page, int pageSize, String sortBy, String order, String searchTerm, String searchField) {
        EntryRecoveryPagedList list = new EntryRecoveryPagedList(mAmazonHelper);

        String decodedSearchTerm = StringHelper.decode(searchTerm);

        info("list(" + page + ", " + pageSize + ", " + sortBy + ", " + order + ", " + decodedSearchTerm + ", " + searchField + ")");

        Form<InputSearch> searchForm = mFormFactory.form(InputSearch.class);

        list.setSearch(decodedSearchTerm, searchField);
        list.setPage(page);
        list.setPageSize(pageSize);
        list.setSortBy(sortBy);
        list.setOrder(order);
        list.computeFilters(Secured.getClient(ctx()));
        list.compute();

        mLastEntryList = list;

        InputSearch isearch = new InputSearch(decodedSearchTerm, searchField);
        searchForm.fill(isearch);

        return ok(views.html.entry_recovery_list.render(list, searchForm, Secured.getClient(ctx())));
    }

    public Result list() {
        return list(0, 100, "date", "desc", "null", "null");
    }

    @Security.Authenticated(Secured.class)
    public Result search(int page, int pageSize, String sortBy, String order) {
        Form<InputSearch> searchForm = mFormFactory.form(InputSearch.class).bindFromRequest();
        InputSearch isearch = searchForm.get();
        String searchTerm = isearch.searchTerm;
        String searchField = isearch.searchField;

        info("search(" + page + ", " + pageSize + ", " + sortBy + ", " + order + ", " + searchTerm + ", " + searchField + ")");

        EntryRecoveryPagedList list = new EntryRecoveryPagedList(mAmazonHelper);
        list.setSearch(searchTerm, searchField);
        list.setPage(page);
        list.setPageSize(pageSize);
        list.setSortBy(sortBy);
        list.setOrder(order);
        list.computeFilters(Secured.getClient(ctx()));
        list.clearCache();
        list.compute();

        mLastEntryList = list;

        searchForm.fill(isearch);

        return ok(views.html.entry_recovery_list.render(list, searchForm, Secured.getClient(ctx())));
    }

    @Security.Authenticated(Secured.class)
    public Result searchClear() {
        EntryRecoveryPagedList list = new EntryRecoveryPagedList(mAmazonHelper);
        list.computeFilters(Secured.getClient(ctx()));
        list.compute();

        Form<InputSearch> searchForm = mFormFactory.form(InputSearch.class);

        mLastEntryList = list;

        return ok(views.html.entry_recovery_list.render(list, searchForm, Secured.getClient(ctx())));
    }

    @Security.Authenticated(Secured.class)
    public Result showByTruck(long truck_id) {
        mGlobals.setClearSearch(false);
        EntryRecoveryPagedList list = new EntryRecoveryPagedList(mAmazonHelper);
        list.setByTruckId(truck_id);
        list.computeFilters(Secured.getClient(ctx()));
        list.compute();

        mLastEntryList = list;

        Form<InputSearch> searchForm = mFormFactory.form(InputSearch.class);
        return ok(views.html.entry_recovery_list.render(list, searchForm, Secured.getClient(ctx())));
    }

    public CompletionStage<Result> computeTotalNumRows() {
        Executor myEc = HttpExecution.fromThread((Executor) mExecutionContext);
        return CompletableFuture.completedFuture(mLastEntryList.computeTotalNumRows()).thenApplyAsync(result -> {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(mLastEntryList.getPrevClass());
            sbuf.append("|");
            sbuf.append(mLastEntryList.getDisplayingXtoYofZ());
            sbuf.append("|");
            sbuf.append(mLastEntryList.getNextClass());
            sbuf.append("|");
            if (result == 0) {
                sbuf.append("No entries");
            } else if (result == 1) {
                sbuf.append("One entry");
            } else {
                sbuf.append(result);
                sbuf.append(" entries found");
            }
            return ok(sbuf.toString());
        }, myEc);
    }

    /**
     * Load pictures in background
     */
    public Result reloadImages() {
        mDownloaded.clear();
        return Results.redirect(routes.EntryController.pictures(mDownloadPicturesLastAttemptEntryId));
    }

    public CompletionStage<Result> checkLoadingPictures() {
        CompletableFuture<Result> completableFuture = new CompletableFuture<>();
        if (mDownloadPicturesForEntryId > 0) {
            downloadPicturesFor(mDownloadPicturesForEntryId);
            mDownloadPicturesForEntryId = 0;
            mDownloadStatusRequests.add(completableFuture);
        } else {
            completableFuture.complete(ok("0"));
        }
        return completableFuture;
    }

    private void downloadPicturesFor(long entry_id) {
        EntryRecovery entry = EntryRecovery.find.byId(entry_id);
        if (entry == null) {
            error("Could not find entry ID " + entry_id);
        } else {
            loadPictures(entry);
        }
    }

    /**
     * Display the picture for an entry.
     */
    public Result pictures(Long entry_id) {
        EntryRecovery entry = EntryRecovery.find.byId(entry_id);
        if (entry == null) {
            return badRequest2("Could not find entry ID " + entry_id);
        }
        mDownloadPicturesLastAttemptEntryId = entry_id;
        if (!mDownloaded.contains(entry_id)) {
            info("pictures(" + entry_id + "): permitted size=" + mDownloaded.size());
            mDownloaded.add(entry_id);
            mDownloadPicturesForEntryId = entry_id;
        } else {
            info("pictures(" + entry_id + "): already done");
        }
        return ok(views.html.entry_list_picture.render(entry.getPictures()));
    }

    private void loadPictures(EntryRecovery entry) {
        entry.loadPictures(request().host(), mAmazonHelper, () -> loadPicturesDone());
    }

    private void loadPicturesDone() {
        if (mDownloadStatusRequests.size() > 0) {
            for (CompletableFuture<Result> completableFuture : mDownloadStatusRequests) {
                completableFuture.complete(ok("1"));
            }
            mDownloadStatusRequests.clear();
        }
    }

    public Result getImage(String picture) {
        File localFile = mAmazonHelper.getLocalFile(picture);
        if (localFile.exists()) {
            return ok(localFile);
        } else {
            return ok(picture);
        }
    }

    /**
     * Display details for the entry including delete button.
     * Note: intentionally NOT secure
     */
    public Result view(Long entry_id) {
        EntryRecovery entry = EntryRecovery.find.byId(entry_id);
        if (entry == null) {
            return badRequest2("Could not find entry ID " + entry_id);
        }
        loadPictures(entry);
        return ok(views.html.entry_recovery_view.render(entry, Secured.getClient(ctx())));
    }

    /**
     * Display the notes for an entry.
     */
    public Result notes(Long entry_id) {
        EntryRecovery entry = EntryRecovery.find.byId(entry_id);
        if (entry == null) {
            return badRequest2("Could not find entry ID " + entry_id);
        }
        return ok(views.html.entry_list_note.render(entry.getNotes(getClientId())));
    }

    private long getClientId() {
        Client client = Secured.getClient(ctx());
        return client == null ? 0 : client.id;
    }

    // region DELETE

    @Security.Authenticated(Secured.class)
    public Result delete(Long entry_id) {
        String host = request().host();
        EntryRecovery entry = EntryRecovery.find.byId(entry_id);
        if (entry != null) {
            entry.remove();
            info("Recovery Entry has been deleted: " + entry_id);
        }
        return list();
    }

    @Security.Authenticated(Secured.class)
    public Result deleteEntries(String idsLine) {
        if (mDeleting) {
            mAborted = true;
            mDeleting = false;
            info("deleteEntries() ABORT INITIATED");
            return ok("R");
        }
        mDeleting = true;
        mAborted = false;

        mDeleteAction = new DeleteAction(idsLine);

        return ok("#0...");
    }

    @Security.Authenticated(Secured.class)
    public Result deleteNext() {
        if (mAborted) {
            mAborted = false;
            mDeleting = false;
            info("deleteNext(): ABORT");
            return ok("R");
        }
        int count = mDeleteAction.count;
        if (mDeleteAction.deleteNext()) {
            info("deleteNext() " + count);
            return ok("#" + Integer.toString(count) + "...");
        } else {
            info("deleteNext() DONE!");
            mDeleting = false;
            return ok("D" + Integer.toString(count));
        }
    }

    @Security.Authenticated(Secured.class)
    public Result deleteAbort() {
        if (mDeleting) {
            mAborted = true;
            mDeleting = false;
            info("deleteAbort()");
        }
        return list();
    }

    @Security.Authenticated(Secured.class)
    public Result importStart() {
        if (mImporting) {
            mAborted = true;
            mImporting = false;
            mInstalling = false;
            info("import() ABORT INITIATED");
            return ok("R");
        }
        mImporting = true;
        mAborted = false;
        Client client = Secured.getClient(ctx());

        info("importStart() START");

        mImportRecovery = new ImportRecovery(mInstalling);
        if (!mImportRecovery.initialize()) {
            mImporting = false;
            mInstalling = false;
            return badRequest2("Could not parse file.");
        }
        if (mImportRecovery.mJsonTop.getNodeType() != JsonNodeType.ARRAY) {
            mImporting = false;
            mInstalling = false;
            return badRequest2("Not an array.");
        }
        return ok("#0...");
    }

    // endregion DELETE

    // region IMPORT

    public Result importNext() {
        if (mAborted) {
            mAborted = false;
            mImporting = false;
            mInstalling = false;
            info("importNext(): ABORT");
            return ok("R");
        }
        if (!mImportRecovery.hasNext()) {
            info("importNext() DONE!");
            mImporting = false;
            mInstalling = false;
            return ok("D");
        }
        if (mImportRecovery.loadNext()) {
            info("importNext() " + mImportRecovery.mCount);
        }
        return ok("#" + mImportRecovery.report());
    }

    @Security.Authenticated(Secured.class)
    public Result installStart() {
        mInstalling = true;
        return importStart();
    }

    public Result installNext() {
        mInstalling = true;
        return importNext();
    }

    private String readFile(File file) {
        StringBuilder sbuf = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0) {
                    sbuf.append(line);
                    sbuf.append("\n");
                }
            }
        } catch (Exception ex) {
            error(ex.getMessage());
        }
        return sbuf.toString();
    }

    Result missingRequest(ArrayList<String> missing) {
        return badRequest2(missingString(missing));
    }

    String missingString(ArrayList<String> missing) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Missing fields:");
        boolean comma = false;
        for (String field : missing) {
            if (comma) {
                sbuf.append(", ");
            }
            sbuf.append(field);
            comma = true;
        }
        sbuf.append("\n");
        return sbuf.toString();
    }

    // endregion IMPORT

    Result badRequest2(String field) {
        error("ERROR: " + field);
        return badRequest(field);
    }

    private class DeleteAction {

        List<String> idsList;
        int totalRequested = 0;
        int count = 0;

        public DeleteAction(String idsLine) {
            String[] idsArray = idsLine.split(",");
            idsList = new ArrayList<String>(Arrays.asList(idsArray));
            totalRequested = idsArray.length;
        }

        public boolean deleteNext() {
            if (idsList.size() == 0) {
                return false;
            }
            String idsText = idsList.get(0);
            try {
                long id = Long.parseLong(idsText);
                deleteNext(id);
                idsList.remove(0);
            } catch (NumberFormatException ex) {
                error(ex.getMessage());
            }
            return true;
        }

        private boolean deleteNext(long id) {
            try {
                String host = request().host();
                debug("Deleting ENTRY ID " + id);
                EntryRecovery entry = EntryRecovery.find.byId(id);
                if (entry != null) {
                    entry.remove();
                    count++;
                    warn("Recovery entry has been deleted: " + id);
                } else {
                    error("Called deleteNext() with bad ID " + id);
                    return false;
                }
            } catch (NumberFormatException ex) {
                error(ex.getMessage());
                return false;
            }
            return true;
        }
    }

    private class ImportRecovery {
        JsonNode mJsonTop;
        Iterator<JsonNode> mIterator;
        int mCount = 0;
        int mProcessed = 0;
        int mTotal = 0;
        boolean mInstalling = false;

        ImportRecovery(boolean installing) {
            mInstalling = installing;
        }

        boolean initialize() {
            try {
                ObjectMapper mapper = new ObjectMapper();
                File recoveryFile = mAmazonHelper.getRecoveryFile();
                mJsonTop = mapper.readTree(readFile(recoveryFile));
                mTotal = mJsonTop.size();
                mIterator = mJsonTop.elements();
            } catch (Exception ex) {
                error(ex.getMessage());
                return false;
            }
            return true;
        }

        boolean hasNext() {
            return mIterator.hasNext();
        }

        boolean loadNext() {
            JsonNode ele = mIterator.next();
            if (ele.size() > 0) {
                mProcessed++;
                if (load(ele)) {
                    return true;
                }
            }
            return false;
        }

        String report() {
            StringBuffer sbuf = new StringBuffer();
            sbuf.append(mCount);
            sbuf.append("   ");
            sbuf.append(mProcessed);
            sbuf.append("/");
            sbuf.append(mTotal);
            sbuf.append("...");
            return sbuf.toString();
        }

        @Transactional
        private boolean load(JsonNode json) {
            debug("RECOVERY: " + json.toString());
            ParseResult result = EntryController.parse(json, false);
            if (result.errorMsg != null) {
                error(result.errorMsg);
                error("Will not create recovery entry");
                return false;
            }
            boolean fatal = result.fatal;
            boolean retServerId = result.retServerId;
            if (result.missing.size() > 0) {
                error(missingString(result.missing));
                if (fatal) {
                    error("Will not create recovery entry");
                    return false;
                }
            }
            long client_id = getClientId();
            Entry entry = result.entry;
            if (entry.project_id == 0) {
                Project recoveryProject = Project.getRecoveryProject();
                if (recoveryProject != null) {
                    error("USING RECOVERY PROJECT.");
                    entry.project_id = recoveryProject.id;
                } else {
                    error("NO RECOVERY PROJECT found.");
                    return false;
                }
            }
            if (entry.id != null && entry.id > 0) {
                Entry existing = Entry.find.byId(entry.id);
                if (existing != null) {
                    if (entry.isMatching(existing, client_id)) {
                        info("Recovered entry: " + entry.toString(client_id));
                        info("Existing entry : " + existing.toString(client_id));
                        if (mInstalling) {
                            info("Replacing existing entry with recovered entry");
                            entry.update();
                        } else {
                            info("Nothing done.");
                        }
                        return false;
                    }
                }
            }
            List<Entry> similar = Entry.getEntriesFromEntry(entry, client_id);
            if (similar.size() > 0) {
                warn("Matched some existing entries with recovery entry of " + entry.toString(client_id));
                for (Entry item : similar) {
                    warn("  Found " + item.toString(client_id));
                }
                if (mInstalling) {
                    info("Overwriting first one found with recovery entry.");
                    Entry item = similar.get(0);
                    entry.id = item.id;
                    entry.update();
                }
                return false;
            }
            EntryRecovery entryRecovery = new EntryRecovery();
            entryRecovery.copy(entry);
            JsonNode value = json.findValue("error");
            if (value != null) {
                entryRecovery.error = value.textValue();
            }
            List<EntryRecovery> existingRecords = EntryRecovery.findEntryForEntry(entryRecovery, client_id);
            if (existingRecords.size() > 0) {
                for (EntryRecovery existingRecord : existingRecords) {
                    info("Found existing record, will replace: " + entryRecovery.toString(client_id) + " [" + mCount + "]");
                    existingRecord.remove();
                    mCount--;
                }
            }
            try {
                entryRecovery.save();
                mCount++;
                debug("Created new recovery entry: " + entryRecovery.toString(client_id) + " [" + mCount + "]");
            } catch (Exception ex) {
                error(ex.getMessage());
                debug("Failed to create new recovery entry: " + entryRecovery.toString(client_id));
            }
            return true;
        }

    }

    // region Logger

    private void error(String msg) {
        Logger.error(msg);
    }

    private void warn(String msg) {
        Logger.warn(msg);
    }

    private void info(String msg) {
        Logger.info(msg);
    }

    private void debug(String msg) {
        Logger.debug(msg);
    }

    // endregion Logger

}


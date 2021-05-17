/**
 * Copyright 2018-2021, FleetTLC. All rights reserved
 */
package controllers;

import java.util.ArrayList;
import java.text.SimpleDateFormat;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import models.*;
import play.Logger;
import play.data.*;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.*;
import java.util.concurrent.*;
import modules.WorkerExecutionContext;
import play.libs.concurrent.HttpExecution;

import static play.data.Form.*;
import modules.StringHelper;
import views.formdata.InputSearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import play.db.ebean.Transactional;

/**
 * Manage a database of Daily After Action Reports
 */
public class DaarController extends Controller {

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'zzz";

    private FormFactory mFormFactory;
    private SimpleDateFormat mDateFormat;
    private DaarPagedList mLastEntryList;
    private WorkerExecutionContext mExecutionContext;

    @Inject
    public DaarController(FormFactory formFactory,
                          WorkerExecutionContext executionContext
    ) {
        this.mFormFactory = formFactory;
        mDateFormat = new SimpleDateFormat(DATE_FORMAT);
        mExecutionContext = executionContext;
    }

    @Security.Authenticated(Secured.class)
    public Result list2(int page, int pageSize, String sortBy, String order, String searchTerm, String searchField) {
        return list(page, pageSize, sortBy, order, searchTerm, searchField);
    }

    @Security.Authenticated(Secured.class)
    public Result list(int page, int pageSize, String sortBy, String order, String searchTerm, String searchField) {
        DaarPagedList list = new DaarPagedList();

        String decodedSearchTerm = StringHelper.decode(searchTerm);

        Logger.info("list(" + page + ", " + pageSize + ", " + sortBy + ", " + order + ", " + decodedSearchTerm + ", " + searchField + ")");

        Form<InputSearch> searchForm = mFormFactory.form(InputSearch.class);

        list.setSearch(decodedSearchTerm, searchField);
        list.setPage(page);
        list.setPageSize(pageSize);
        list.setSortBy(sortBy);
        list.setOrder(order);
        list.compute();

        mLastEntryList = list;

        InputSearch isearch = new InputSearch(decodedSearchTerm, searchField);
        searchForm.fill(isearch);

        return ok(views.html.daar_list.render(list, searchForm, Secured.getClient(ctx())));
    }

    @Security.Authenticated(Secured.class)
    public Result search(int page, int pageSize, String sortBy, String order) {
        Form<InputSearch> searchForm = mFormFactory.form(InputSearch.class).bindFromRequest();
        InputSearch isearch = searchForm.get();
        String searchTerm = isearch.searchTerm;
        String searchField = isearch.searchField;

        Logger.info("search(" + page + ", " + pageSize + ", " + sortBy + ", " + order + ", " + searchTerm + ", " + searchField + ")");

        DaarPagedList list = new DaarPagedList();
        list.setSearch(searchTerm, searchField);
        list.setPage(page);
        list.setPageSize(pageSize);
        list.setSortBy(sortBy);
        list.setOrder(order);
        list.compute();

        mLastEntryList = list;

        searchForm.fill(isearch);

        return ok(views.html.daar_list.render(list, searchForm, Secured.getClient(ctx())));
    }

    @Security.Authenticated(Secured.class)
    public Result searchClear() {
        DaarPagedList list = new DaarPagedList();
        list.compute();
        Form<InputSearch> searchForm = mFormFactory.form(InputSearch.class);

        mLastEntryList = list;

        return ok(views.html.daar_list.render(list, searchForm, Secured.getClient(ctx())));
    }

    @Transactional
    @Security.Authenticated(Secured.class)
    public Result view(Long entry_id) {
        Daar entry = Daar.find.byId(entry_id);
        if (entry == null) {
            return badRequest2("Could not find Daar entry with ID " + entry_id);
        }
        entry.viewed = true;
        entry.update();

        return ok(views.html.daar_view.render(entry, Secured.getClient(ctx())));
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

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public Result enter() {
        Daar entry = new Daar();
        ArrayList<String> missing = new ArrayList<String>();
        JsonNode json = request().body().asJson();
        Logger.debug("GOT: " + json.toString());
        boolean retServerId = false;
        JsonNode value;
        // TECH
        value = json.findValue("tech_id");
        if (value == null) {
            missing.add("tech_id");
        } else {
            entry.tech_id = value.intValue();
        }
        // DATE
        value = json.findValue("date_string");
        if (value != null) {
            String date_value = value.textValue();
            try {
                entry.entry_time = mDateFormat.parse(date_value);
                entry.time_zone = StringHelper.pickOutTimeZone(date_value, 'Z');
            } catch (Exception ex) {
                Logger.error("While parsing " + date_value + ":" + ex.getMessage());
            }
        } else {
            missing.add("date_string");
        }
        // START TIME
        value = json.findValue("start_time_tomorrow_string");
        if (value != null) {
            String date_value = value.textValue();
            try {
                entry.start_time = mDateFormat.parse(date_value);
            } catch (Exception ex) {
                Logger.error("While parsing " + date_value + ":" + ex.getMessage());
            }
        } else {
            missing.add("start_time_tomorrow_string");
        }
        // SERVER ID
        value = json.findValue("server_id");
        if (value != null) {
            entry.id = value.longValue();
            retServerId = true;
            Daar existing;
            if (entry.id > 0) {
                existing = Daar.find.byId(entry.id);
                if (existing == null) {
                    existing = Daar.findByDate(entry.tech_id, entry.entry_time);
                    if (existing != null) {
                        Logger.info("Could not find DAAR entry with ID " + entry.id + ", so located based on time=" + entry.entry_time);
                    }
                } else {
                    existing.entry_time = entry.entry_time;
                    existing.tech_id = entry.tech_id;
                }
            } else {
                existing = Daar.findByDate(entry.tech_id, entry.entry_time);
            }
            if (existing == null) {
                entry.id = 0L;
            } else {
                entry = existing;
            }
        }
        // FIELDS
        value = json.findValue("project_id");
        if (value != null) {
            entry.project_id =  value.longValue();
        }
        value = json.findValue("project_desc");
        if (value != null) {
            entry.project_desc =  value.textValue();
        }
        value = json.findValue("work_completed");
        if (value != null) {
            entry.work_completed_desc = value.textValue();
        }
        value = json.findValue("missed_units");
        if (value != null) {
            entry.missed_units_desc = value.textValue();
        }
        value = json.findValue("issues");
        if (value != null) {
            entry.issues_desc = value.textValue();
        }
        value = json.findValue("injuries");
        if (value != null) {
            entry.injuries_desc = value.textValue();
        }
        if (missing.size() > 0) {
            return missingRequest(missing);
        }
        if (entry.id != null && entry.id > 0) {
            entry.update();
            Logger.debug("Updated DAAR entry " + entry.id);
        } else {
            entry.save();
            Logger.debug("Created new DAAR entry " + entry.id);
        }
        long ret_id;
        if (retServerId) {
            ret_id = entry.id;
        } else {
            ret_id = 0;
        }
        return ok(Long.toString(ret_id));
    }

    Result missingRequest(ArrayList<String> missing) {
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
        return badRequest2(sbuf.toString());
    }

    Result badRequest2(String field) {
        Logger.error("ERROR: " + field);
        return badRequest(field);
    }

}
            

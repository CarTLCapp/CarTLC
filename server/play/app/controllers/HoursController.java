/**
 * Copyright 2021, FleetTLC. All rights reserved
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
 * Manage a database of Daily Hours Reports
 */
public class HoursController extends Controller {

    private static final String DATE_FORMAT = "yyyy-MM-dd'Z'zzz";

    private FormFactory mFormFactory;
    private SimpleDateFormat mDateFormat;
    private HoursPagedList mLastEntryList;
    private WorkerExecutionContext mExecutionContext;

    @Inject
    public HoursController(FormFactory formFactory,
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
        HoursPagedList list = new HoursPagedList();

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

        return ok(views.html.hours_list.render(list, searchForm, Secured.getClient(ctx())));
    }

    @Security.Authenticated(Secured.class)
    public Result search(int page, int pageSize, String sortBy, String order) {
        Form<InputSearch> searchForm = mFormFactory.form(InputSearch.class).bindFromRequest();
        InputSearch isearch = searchForm.get();
        String searchTerm = isearch.searchTerm;
        String searchField = isearch.searchField;

        Logger.info("search(" + page + ", " + pageSize + ", " + sortBy + ", " + order + ", " + searchTerm + ", " + searchField + ")");

        HoursPagedList list = new HoursPagedList();
        list.setSearch(searchTerm, searchField);
        list.setPage(page);
        list.setPageSize(pageSize);
        list.setSortBy(sortBy);
        list.setOrder(order);
        list.compute();

        mLastEntryList = list;

        searchForm.fill(isearch);

        return ok(views.html.hours_list.render(list, searchForm, Secured.getClient(ctx())));
    }

    @Security.Authenticated(Secured.class)
    public Result searchClear() {
        HoursPagedList list = new HoursPagedList();
        list.compute();
        Form<InputSearch> searchForm = mFormFactory.form(InputSearch.class);

        mLastEntryList = list;

        return ok(views.html.hours_list.render(list, searchForm, Secured.getClient(ctx())));
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
        Hours entry = new Hours();
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
        // SERVER ID
        value = json.findValue("server_id");
        if (value != null) {
            entry.id = value.longValue();
            retServerId = true;
            Hours existing;
            if (entry.id > 0) {
                existing = Hours.find.byId(entry.id);
                if (existing == null) {
                    existing = Hours.findByDate(entry.tech_id, entry.entry_time);
                    if (existing != null) {
                        Logger.info("Could not find HOURS entry with ID " + entry.id + ", so located based on time=" + entry.entry_time);
                    }
                } else {
                    existing.entry_time = entry.entry_time;
                    existing.tech_id = entry.tech_id;
                }
            } else {
                existing = Hours.findByDate(entry.tech_id, entry.entry_time);
            }
            if (existing == null) {
                entry.id = 0L;
            } else {
                entry = existing;
            }
        }
        // PROJECT
        value = json.findValue("project_id");
        if (value != null) {
            entry.project_id = value.longValue();
        }
        value = json.findValue("project_desc");
        if (value != null) {
            entry.project_desc = value.textValue();
        }
        // START TIME
        value = json.findValue("start_time");
        if (value != null) {
            entry.start_time = value.intValue();
        } else {
            missing.add("start_time");
        }
        // END TIME
        value = json.findValue("end_time");
        if (value != null) {
            entry.end_time = value.intValue();
        } else {
            missing.add("end_time");
        }
        // LUNCH TIME
        value = json.findValue("lunch_time");
        if (value != null) {
            entry.lunch_time = value.intValue();
        } else {
            missing.add("lunch_time");
        }
        // BREAK TIME
        value = json.findValue("break_time");
        if (value != null) {
            entry.break_time = value.intValue();
        } else {
            missing.add("break_time");
        }
        // DRIVE TIME
        value = json.findValue("drive_time");
        if (value != null) {
            entry.drive_time = value.intValue();
        } else {
            missing.add("break_time");
        }
        // PROJECT
        value = json.findValue("project_id");
        if (value != null) {
            entry.project_id = value.longValue();
        }
        // NOTES
        value = json.findValue("notes");
        if (value != null) {
            entry.notes = value.textValue();
        }

        if (missing.size() > 0) {
            return missingRequest(missing);
        }
        if (entry.id != null && entry.id > 0) {
            entry.update();
            Logger.debug("Updated HOURS entry " + entry.id);
        } else {
            entry.save();
            Logger.debug("Created new HOURS entry " + entry.id);
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
            

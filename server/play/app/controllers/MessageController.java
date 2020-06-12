/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import models.*;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.Date;
import java.util.Iterator;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.Logger;

public class MessageController extends Controller {

    private static final int PAGE_SIZE = 100;

    private MessageList mMessages;

    public Result list(int page, String sortBy, String order) {
        mMessages = new MessageList(page, sortBy, order);
        return ok(views.html.message_list.render(mMessages, sortBy, order, Secured.getClient(ctx())));
    }

    public Result LIST() {
        return Results.redirect(routes.MessageController.list(0, "log_time", "desc"));
    }

    /**
     * Display the trace on a separate screen since it can be kind of long.
     */
    public Result view(Long msg_id) {
        Message message = Message.find.byId(msg_id);
        if (message == null) {
            return badRequest2("Could not find message ID " + msg_id);
        }
        return ok(views.html.message_view.render(message));
    }

    @Security.Authenticated(Secured.class)
    public Result delete(Long msg_id) {
        Message message = Message.find.byId(msg_id);
        if (message != null) {
            message.delete();
            flash("success", "Message has been deleted");
        }
        return LIST();
    }

    @Security.Authenticated(Secured.class)
    public Result deleteAll() {
        for (Message message : Message.find.findList()) {
            message.delete();
        }
        return LIST();
    }

    @Security.Authenticated(Secured.class)
    public Result deletePage() {
        for (Message message : mMessages.getList()) {
            message.delete();
        }
        return LIST();
    }
    
    public Result pageSize(String size) {
        try {
            int pageSize = Integer.parseInt(size);
            mMessages.setPageSize(pageSize);
        } catch (NumberFormatException ex) {
            Logger.error(ex.getMessage());
        }
        return ok("Done");
    }

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public Result message() {
        Message message = new Message();
        ArrayList<String> missing = new ArrayList<String>();
        JsonNode json = request().body().asJson();
        Logger.debug("GOT MSG: " + json.toString());
        JsonNode value = json.findValue("tech_id");
        if (value == null) {
            missing.add("tech_id");
        } else {
            message.tech_id = value.intValue();
        }
        value = json.findValue("date");
        if (value == null) {
            missing.add("date");
        } else {
            message.log_time = new Date(value.longValue());
        }
        value = json.findValue("code");
        if (value != null) {
            message.code = value.intValue();
        }
        value = json.findValue("message");
        if (value != null) {
            message.message = value.textValue();
        }
        value = json.findValue("trace");
        if (value != null) {
            message.trace = value.textValue();
        }
        value = json.findValue("app_version");
        if (value != null) {
            message.app_version = value.textValue();
        }
        if (missing.size() > 0) {
            return missingRequest(missing);
        }
        message.save();
        // TODO: This needs to be a redirect with a message pass.
        return ok(Integer.toString(0));
    }

    Result missingRequest(ArrayList<String> missing) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Missing fields:");
        for (String field : missing)
        {
            sbuf.append(" ");
            sbuf.append(field);
        }
        sbuf.append("\n");
        return badRequest2(sbuf.toString());
    }

    Result badRequest2(String field) {
        Logger.error("ERROR: " + field);
        return badRequest(field);
    }

}
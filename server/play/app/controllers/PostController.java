package controllers;

import javax.inject.*;

import play.*;
import play.mvc.*;

import java.util.ArrayList;

import play.libs.Json;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import play.db.ebean.Transactional;
import models.Technician;
import models.Version;
import models.Truck;

import java.lang.System;
import java.util.Date;
import java.util.List;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class PostController extends Controller {
    @Inject
    public PostController() {
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result register() {
        JsonNode json = request().body().asJson();
        ArrayList<String> missing = new ArrayList<String>();
        String first_name = json.findPath("first_name").textValue();
        if (first_name == null) {
            missing.add("first_name");
        }
        String last_name = json.findPath("last_name").textValue();
        if (last_name == null) {
            missing.add("last_name");
        }
        String device_id = json.findPath("device_id").textValue();
        if (device_id == null) {
            missing.add("device_id");
        }
        if (missing.size() > 0) {
            return missingRequest(missing);
        }
        Transaction txn = Ebean.beginTransaction();
        Result res;
        try {
            Technician tech = Technician.findByName(first_name, last_name, device_id);
            if (tech == null) {
                tech = new Technician();
            }
            tech.first_name = first_name;
            tech.last_name = last_name;
            tech.device_id = device_id;
            tech.save();
            txn.commit();
            res = ok(Long.toString(tech.id));
        } catch (Exception ex) {
            res = badRequest(ex.getMessage());
        } finally {
            txn.end();
        }
        return res;
    }

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public Result ping() {
        ArrayList<String> missing = new ArrayList<String>();
        JsonNode json = request().body().asJson();
        JsonNode node = json.findValue("device_id");
        String deviceId;
        if (node == null) {
            missing.add("device_id");
            deviceId = ""; // to get rid of warning
        } else {
            deviceId = node.textValue();
        }
        node = json.findValue("tech_id");
        int tech_id;
        if (node == null) {
            missing.add("tech_id");
            tech_id = 0; // to get rid of warning
        } else {
            tech_id = node.intValue();
        }
        if (missing.size() > 0) {
            return missingRequest(missing);
        }
        String app_version;
        node = json.findValue("app_version");
        if (node != null) {
            app_version = node.textValue();
        } else {
            app_version = null;
        }
        ObjectNode result = Json.newObject();
        result.put(Version.VERSION_PROJECT, Version.get(Version.VERSION_PROJECT));
        result.put(Version.VERSION_COMPANY, Version.get(Version.VERSION_COMPANY));
        result.put(Version.VERSION_EQUIPMENT, Version.get(Version.VERSION_EQUIPMENT));
        result.put(Version.VERSION_NOTE, Version.get(Version.VERSION_NOTE));
        result.put(Version.VERSION_TRUCK, Version.get(Version.VERSION_TRUCK));
        Technician tech = Technician.find.byId((long) tech_id);
        if (tech != null) {
            if (tech.reset_upload) {
                result.put("reset_upload", true);
                tech.reset_upload = false;
            }
            tech.last_ping = new Date(System.currentTimeMillis());
            tech.app_version = app_version;
            tech.save();
        } else {
            Logger.error("ping(): could not find technician with ID " + tech_id);
            result.put("re-register", true);
            result.put("reset_upload", true);
        }
        return ok(result);
    }

    Result missingRequest(ArrayList<String> missing) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Missing fields:");
        for (String field : missing) {
            sbuf.append(" ");
            sbuf.append(field);
        }
        sbuf.append("\n");
        return badRequest(sbuf.toString());
    }

}

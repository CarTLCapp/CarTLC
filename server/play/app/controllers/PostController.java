/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
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

    class FindTechnician {

        String mErrorMessage;

        public Technician find(JsonNode codeNode, String first_name, String last_name) {
            Technician tech = null;
            mErrorMessage = null;
            if (codeNode != null) {
                int code;
                try {
                    code = Integer.valueOf(codeNode.textValue());
                } catch (NumberFormatException ex) {
                    mErrorMessage = "Bad code string value: " + ex.getMessage();
                    return null;
                }
                List<Technician> found = Technician.listWithCode(code);
                if (found.size() == 0) {
                    mErrorMessage = String.format("No such technician with code: %04d", code);
                    return null;
                }
                tech = found.get(0);
            } else if (first_name != null && last_name != null) {
                tech = Technician.findByName(first_name, last_name);
                if (tech == null) {
                    mErrorMessage = "No such technician with name: " + first_name + " " + last_name;
                    return null;
                }
            } else {
                mErrorMessage = "Malformed request";
            }
            return tech;
        }
    }

    @Inject
    public PostController() {
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result register() {
        JsonNode json = request().body().asJson();
        ArrayList<String> missing = new ArrayList<String>();
        String first_name = json.findPath("first_name").textValue();
        String last_name = json.findPath("last_name").textValue();
        JsonNode codeNode = json.findValue("code");
        String device_id = json.findPath("device_id").textValue();
        if (device_id == null) {
            missing.add("device_id");
        }
        if (missing.size() > 0) {
            return missingRequest(missing);
        }
        FindTechnician findTechnician = new FindTechnician();
        Technician tech = findTechnician.find(codeNode, first_name, last_name);
        if (tech == null) {
            return badRequest(findTechnician.mErrorMessage);
        }
        // An old APK query:
        if (codeNode == null) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(tech.id);

            long secondary_tech_id = 0;
            first_name = json.findPath("secondary_first_name").textValue();
            last_name = json.findPath("secondary_last_name").textValue();
            codeNode = json.findValue("secondary_code");

            if (codeNode != null || (first_name != null && !first_name.isEmpty() && last_name != null && !last_name.isEmpty())) {
                tech = findTechnician.find(codeNode, first_name, last_name);
                if (tech == null) {
                    return badRequest(findTechnician.mErrorMessage);
                }
                sbuf.append(":");
                sbuf.append(tech.id);
            }
            return ok(sbuf.toString());
        }
        ObjectNode result = Json.newObject();
        result.put("tech_id", tech.id);
        result.put("tech_first_name", tech.first_name);
        result.put("tech_last_name", tech.last_name);

        Technician secondaryTech = null;
        first_name = json.findPath("secondary_first_name").textValue();
        last_name = json.findPath("secondary_last_name").textValue();
        codeNode = json.findValue("secondary_code");
        if (codeNode != null || (first_name != null && !first_name.isEmpty() && last_name != null && !last_name.isEmpty())) {
            secondaryTech = findTechnician.find(codeNode, first_name, last_name);
            if (secondaryTech == null) {
                return badRequest(findTechnician.mErrorMessage);
            } else {
                result.put("secondary_tech_id", secondaryTech.id);
                result.put("secondary_tech_first_name", secondaryTech.first_name);
                result.put("secondary_tech_last_name", secondaryTech.last_name);
            }
        }
        return ok(result);
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
        result.put(Version.VERSION_VEHICLE_NAMES, Version.get(Version.VERSION_VEHICLE_NAMES));
        result.put(Version.VERSION_FLOW, Version.get(Version.VERSION_FLOW));
        Technician tech = Technician.find.byId((long) tech_id);
        if (tech != null) {
            if (tech.reset_upload) {
                result.put("reset_upload", true);
                tech.reset_upload = false;
            }
            if (tech.reload_code != null && !tech.reload_code.isEmpty()) {
                result.put("reload_code", tech.reload_code);
                tech.reload_code = null;
            }
            tech.device_id = deviceId;
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

/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import models.InputLines;
import models.Strings;
import models.Vehicle;
import models.VehicleName;
import models.Version;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

/**
 * Manage a database of trucks
 */
public class VehicleController extends Controller {

    private static final int PAGE_SIZE = 100;

    private SimpleDateFormat mDateFormat;
    private static final String DATE_FORMAT = EntryController.DATE_FORMAT;

    private FormFactory formFactory;

    @Inject
    public VehicleController(FormFactory formFactory) {
        this.formFactory = formFactory;
        mDateFormat = new SimpleDateFormat(DATE_FORMAT);
    }

    public Result list() {
        return list(0, "entry_time", "desc");
    }

    /**
     * Display the list of trucks.
     */
    public Result list(int page, String sortBy, String order) {
        return ok(views.html.vehicle_list.render(Vehicle.list(page, PAGE_SIZE, sortBy, order), sortBy, order, Secured.getClient(ctx())));
    }

    @Security.Authenticated(Secured.class)
    public Result names() {
        List<VehicleName> list = VehicleName.list();
        return ok(views.html.vehicle_names_list.render(list, Secured.getClient(ctx())));
    }

    @Security.Authenticated(Secured.class)
    public Result editNames() {
        if (Secured.isAdmin(ctx())) {
            Form<InputLines> linesForm = formFactory.form(InputLines.class).fill(new InputLines(VehicleName.getLines()));
            return ok(views.html.vehicle_names_edit.render(linesForm, Secured.getClient(ctx())));
        } else {
            return HomeController.PROBLEM("Only administrators can edit vehicle names");
        }
    }

    /**
     * Create many projects at once.
     */
    @Transactional
    @Security.Authenticated(Secured.class)
    public Result saveNames() {
        Form<InputLines> linesForm = formFactory.form(InputLines.class).bindFromRequest();
        if (linesForm.hasErrors() || !Secured.isAdmin(ctx())) {
            if (linesForm.hasErrors()) {
                Logger.error("Had errors");
            }
            if (!Secured.isAdmin(ctx())) {
                Logger.error("Not admin");
            }
            return badRequest(views.html.vehicle_names_edit.render(linesForm, Secured.getClient(ctx())));
        }
        String[] lines = linesForm.get().getLines();
        VehicleName.setLines(lines);
        Version.inc(Version.VERSION_VEHICLE_NAMES);
        return names();
    }

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public Result enter() {
        Vehicle vehicle = new Vehicle();
        ArrayList<String> missing = new ArrayList<String>();
        JsonNode json = request().body().asJson();
        Logger.debug("VGOT: " + json.toString());
        JsonNode value;
        value = json.findValue("tech_id");
        if (value == null) {
            missing.add("tech_id");
        } else {
            vehicle.tech_id = value.intValue();
        }
        value = json.findValue("date_string");
        String date_value = value.textValue();
        try {
            vehicle.entry_time = mDateFormat.parse(date_value);
            vehicle.time_zone = EntryController.pickOutTimeZone(date_value, 'Z');
        } catch (Exception ex) {
            Logger.error("While parsing " + date_value + ":" + ex.getMessage());
        }
        value = json.findValue("server_id");
        if (value != null) {
            vehicle.id = value.longValue();
            Vehicle existing;
            if (vehicle.id > 0) {
                existing = Vehicle.find.byId(vehicle.id);
                if (existing != null) {
                    existing.entry_time = vehicle.entry_time;
                    existing.tech_id = vehicle.tech_id;
                    vehicle = existing;
                } else {
                    vehicle.id = 0L;
                }
            }
        }
        value = json.findValue("inspecting");
        if (value != null) {
            String inspecting = value.textValue();
            vehicle.inspecting = Strings.get(inspecting);
        } else {
            missing.add("inspecting");
        }
        value = json.findValue("type_of_inspection");
        if (value != null) {
            String typeOfInspection = value.textValue();
            vehicle.type_of_inspection = Strings.get(typeOfInspection);
        } else {
            missing.add("type_of_inspection");
        }
        value = json.findValue("mileage");
        if (value != null) {
            vehicle.mileage = value.intValue();
        } else {
            missing.add("mileage");
        }
        value = json.findValue("head_lights");
        if (value != null) {
            String mash = value.textValue();
            vehicle.head_lights = parseMash(mash);
        } else {
            missing.add("head_lights");
        }
        value = json.findValue("tail_lights");
        if (value != null) {
            String mash = value.textValue();
            vehicle.tail_lights = parseMash(mash);
        } else {
            missing.add("tail_lights");
        }
        value = json.findValue("exterior_light_issues");
        if (value != null) {
            vehicle.exterior_light_issues = value.textValue();
        } else {
            missing.add("exterior_light_issues");
        }
        value = json.findValue("fluid_checks");
        if (value != null) {
            String mash = value.textValue();
            vehicle.fluid_checks = parseMash(mash);
        } else {
            missing.add("fluid_checks");
        }
        value = json.findValue("fluid_problems_detected");
        if (value != null) {
            vehicle.fluid_problems_detected = value.textValue();
        } else {
            missing.add("fluid_problems_detected");
        }
        value = json.findValue("tire_inspection");
        if (value != null) {
            String mash = value.textValue();
            vehicle.tire_inspection = parseMash(mash);
        } else {
            missing.add("tire_inspection");
        }
        value = json.findValue("exterior_damage");
        if (value != null) {
            vehicle.exterior_damage = value.textValue();
        } else {
            missing.add("exterior_damage");
        }
        value = json.findValue("other");
        if (value != null) {
            vehicle.other = value.textValue();
        } else {
            missing.add("other");
        }
        if (missing.size() > 0) {
            return missingRequest(missing);
        }
        if (vehicle.id != null && vehicle.id > 0) {
            vehicle.update();
            Logger.debug("Updated vehicle " + vehicle.id);
        } else {
            vehicle.save();
            Logger.debug("Created new vehicle " + vehicle.id);
        }
        long ret_id;
        ret_id = vehicle.id;
        return ok(Long.toString(ret_id));
    }

    public Result strings() {
        ObjectNode top = Json.newObject();
        ArrayNode array = top.putArray("strings");
        List<Strings> items = Strings.list();
        for (Strings item : items) {
            ObjectNode node = array.addObject();
            node.put("id", item.id);
            node.put("value", item.string_value);
        }
        return ok(top);
    }

    public Result queryNames() {
        ObjectNode top = Json.newObject();
        ArrayNode array = top.putArray("names");
        List<VehicleName> items = VehicleName.list();
        for (VehicleName name : items) {
            ObjectNode node = array.addObject();
            node.put("id", name.id);
            node.put("name", name.name);
            node.put("number", name.number);
        }
        return ok(top);
    }

    /**
     * @param mash Expected incoming string is a comma separated list of integers or strings.
     *             If integers, then verify they are ready to go server_id's into the strings table.
     *             If strings, then enter these strings into the strings table and use the string id.
     * @return Comma separated list of string id's into the strings table.
     **/
    private String parseMash(String mash) {
        String[] eles = mash.split(",");
        try {
            for (String ele : eles) {
                Long.parseLong(ele);
            }
            return mash;
        } catch (NumberFormatException ex) {
            StringBuilder sbuf = new StringBuilder();
            boolean comma = false;
            for (String ele : eles) {
                if (comma) {
                    sbuf.append(",");
                } else {
                    comma = true;
                }
                sbuf.append(Strings.get(ele));
            }
            return sbuf.toString();
        }
    }

    private Result missingRequest(ArrayList<String> missing) {
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

    private Result badRequest2(String field) {
        Logger.error("ERROR: " + field);
        return badRequest(field);
    }

    public Result delete(Long vehicle_id) {
        Vehicle vehicle = Vehicle.find.byId(vehicle_id);
        if (vehicle == null) {
            return badRequest2("Could not find vehicle ID " + vehicle_id);
        }
        vehicle.delete();
        return list();
    }
}
            

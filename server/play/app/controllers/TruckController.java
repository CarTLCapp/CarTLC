/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import play.db.ebean.Transactional;

import play.mvc.*;
import play.data.*;

import static play.data.Form.*;

import models.*;
import views.formdata.InputTruck;

import java.util.*;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;

import play.Logger;

/**
 * Manage a database of trucks
 */
public class TruckController extends Controller {

    private static final String FILTER_SHOW = "Enable Filter";
    private static final String FILTER_HIDE = "Disable Filter";

    private FormFactory mFormFactory;
    private boolean mFilter;

    @Inject
    public TruckController(FormFactory formFactory) {
        this.mFormFactory = formFactory;
    }

    /**
     * Display the list of trucks.
     */
    public Result list(int page) {
        if (mFilter) {
            return ok(views.html.truck_list.render(Truck.listFiltered(page), Secured.getClient(ctx()), FILTER_HIDE));
        } else {
            return ok(views.html.truck_list.render(Truck.listPaged(page), Secured.getClient(ctx()), FILTER_SHOW));
        }
    }

    public Result LIST() {
        return Results.redirect(routes.TruckController.list(0));
    }

    @Security.Authenticated(Secured.class)
    public Result cleanup() {
        Truck.cleanup();
        return LIST();
    }

    public Result toggleFilter() {
        mFilter = !mFilter;
        return LIST();
    }

    /**
     * Handle truck deletion
     */
    @Security.Authenticated(Secured.class)
    public Result delete(Long id) {
        Truck truck = Truck.find.byId(id);
        if (truck == null) {
            String message = "Invalid truck ID: " + id;
            flash(message);
            return ok(message);
        }
        if (truck.countEntries() > 0) {
            String message = "Cannot delete this truck, it is being used in an entry.";
            flash(message);
            return ok(message);
        }
        if (truck.countWorkOrders() > 0) {
            String message = "Cannot delete this truck, it is being used in a work order.";
            flash(message);
            return ok(message);
        }
        Truck.find.byId(id).delete();
        return LIST();
    }

    public Result query() {
        JsonNode json = request().body().asJson();
        JsonNode value = json.findValue("tech_id");
        if (value == null) {
            return badRequest("missing field: tech_id");
        }
        value = json.findValue("page");
        int page;
        if (value != null) {
            page = value.intValue();
        } else {
            page = -1;
        }
        int pageSize;
        value = json.findValue("page_size");
        if (value != null) {
            pageSize = value.intValue();
        } else {
            pageSize = -1;
        }
        return query(page, pageSize);
    }

    private Result query(int page, int pageSize) {
        List<Truck> list = Truck.list();
        List<Truck> subList;
        int numPages = list.size() / pageSize + ((list.size() % pageSize) == 0 ? 0 : 1);
        if (pageSize > 0 && page >= 0) {
            try {
                int fromIndex = page * pageSize;
                int toIndex = fromIndex + pageSize;
                if (toIndex > list.size()) {
                    toIndex = list.size();
                }
                subList = list.subList(fromIndex, toIndex);
            } catch (IndexOutOfBoundsException ex) {
                Logger.error(ex.getMessage());
                subList = new ArrayList<Truck>();
            }
        } else {
            subList = list;
        }
        ObjectNode top = Json.newObject();
        ArrayNode array = top.putArray("trucks");
        for (Truck item : subList) {
            ObjectNode node = array.addObject();
            node.put("id", item.id);
            if (item.truck_number != null) {
                node.put("truck_number_string", item.truck_number);
                // Backwards capability
                try {
                    int trucknum = Integer.valueOf(item.truck_number);
                    node.put("truck_number", trucknum);
                } catch (NumberFormatException ex) {
                }
            }
            if (item.license_plate != null) {
                node.put("license_plate", item.license_plate);
            }
            if (item.project_id > 0) {
                node.put("project_id", item.project_id);
            }
            String companyName = item.getCompanyNameNullOkay();
            if (companyName != null) {
                node.put("company_name", companyName);
            }
            int count = item.countEntries();
            node.put("has_entries", (count > 0));
        }
        top.put("numPages", numPages);
        top.put("page", page);

        return ok(top);
    }

    /**
     * Display the 'edit form' of an existing Technician.
     *
     * @param id Id of the user to edit
     */
    @Security.Authenticated(Secured.class)
    public Result edit(Long id) {
        Form<InputTruck> truckForm = mFormFactory.form(InputTruck.class).fill(new InputTruck(Truck.get(id)));
        if (Secured.isAdmin(ctx())) {
            return ok(views.html.truck_editForm.render(id, truckForm));
        } else {
            return HomeController.PROBLEM("Non administrators cannot change truck entries.");
        }
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the truck to edit
     */
    @Transactional
    @Security.Authenticated(Secured.class)
    public Result update(Long id) throws PersistenceException {
        Form<InputTruck> truckForm = mFormFactory.form(InputTruck.class).bindFromRequest();
        if (truckForm.hasErrors()) {
            return badRequest(views.html.truck_editForm.render(id, truckForm));
        }
        InputTruck updateTruck = truckForm.get();
        Truck truck = Truck.get(id);
        if (truck == null) {
            return badRequest("Cannot find truck");
        }
        truck.truck_number = updateTruck.truck_number;
        truck.license_plate = updateTruck.license_plate;

        Project project = Project.findByName(updateTruck.root_project_name, updateTruck.sub_project_name);
        if (project == null) {
            return badRequest("Cannot find project named: " + updateTruck.root_project_name + " - " + updateTruck.sub_project_name);
        }
        truck.project_id = project.id;

        if (updateTruck.company_name.trim().isEmpty()) {
            truck.company_name_id = 0;
        } else {
            truck.company_name_id = CompanyName.save(updateTruck.company_name);
        }
        truck.update();

        Logger.info("Truck updated: " + truck.toString());
        Version.inc(Version.VERSION_TRUCK);

        return LIST();
    }

}
            

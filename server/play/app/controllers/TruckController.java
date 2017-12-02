package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import play.db.ebean.Transactional;

import play.mvc.*;
import play.data.*;

import static play.data.Form.*;

import models.*;
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

    private FormFactory formFactory;

    @Inject
    public TruckController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    /**
     * Display the list of trucks.
     */
    public Result list() {
        return ok(views.html.truck_list.render(Truck.list(), Secured.getClient(ctx())));
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
        return list();
    }

    public Result query() {
        JsonNode json = request().body().asJson();
        JsonNode value = json.findValue("tech_id");
        if (value == null) {
            return badRequest("missing field: tech_id");
        }
        int tech_id = value.intValue();
        ObjectNode top = Json.newObject();
        ArrayNode array = top.putArray("trucks");
        List<Truck> trucks = Truck.list();
        for (Truck item : trucks) {
            ObjectNode node = array.addObject();
            node.put("id", item.id);
            if (item.truck_number > 0) {
                node.put("truck_number", item.truck_number);
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
        }
        return ok(top);
    }

    /**
     * Display the 'edit form' of an existing Technician.
     *
     * @param id Id of the user to edit
     */
    @Security.Authenticated(Secured.class)
    public Result edit(Long id) {
        Form<InputTruck> truckForm = formFactory.form(InputTruck.class).fill(new InputTruck(Truck.get(id)));
        if (Secured.isAdmin(ctx())) {
            return ok(views.html.truck_editForm.render(id, truckForm));
        } else {
            truckForm.reject("adminstrator", "Non administrators cannot change truck entries.");
            return badRequest(views.html.home.render(Secured.getClient(ctx())));
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
        Form<InputTruck> truckForm = formFactory.form(InputTruck.class).bindFromRequest();
        if (truckForm.hasErrors()) {
            return badRequest(views.html.truck_editForm.render(id, truckForm));
        }
        InputTruck updateTruck = truckForm.get();
        Truck truck = Truck.get(id);
        if (truck == null) {
            return badRequest("Cannot find truck");
        }
        if (updateTruck.truck_number.trim().isEmpty()) {
            truck.truck_number = 0;
        } else {
            try {
                truck.truck_number = Integer.parseInt(updateTruck.truck_number);
            } catch (NumberFormatException ex) {
                return badRequest(ex.getMessage());
            }
        }
        truck.license_plate = updateTruck.license_plate;
        if (updateTruck.project_name.trim().isEmpty()) {
            truck.project_id = 0;
        } else {
            Project project = Project.findByName(updateTruck.project_name);
            if (project == null) {
                return badRequest("Cannot find project named: " + updateTruck.project_name);
            }
            truck.project_id = project.id;
        }
        if (updateTruck.company_name.trim().isEmpty()) {
            truck.company_name_id = 0;
        } else {
            truck.company_name_id = CompanyName.save(updateTruck.company_name);
        }
        truck.update();

        Logger.info("Truck updated: " + truck.toString());
        Version.inc(Version.VERSION_TRUCK);

        return list();
    }

}
            

package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import play.mvc.*;
import play.data.*;

import static play.data.Form.*;

import models.*;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

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

}
            

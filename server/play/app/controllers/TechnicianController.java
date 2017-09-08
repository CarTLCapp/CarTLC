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
 * Manage a database of users
 */
public class TechnicianController extends Controller {

    private FormFactory formFactory;

    @Inject
    public TechnicianController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    /**
     * Display the list of users.
     */
    public Result list() {
        return ok(views.html.technician_list.render(Technician.list(), Secured.getClient(ctx())));
    }

    /**
     * Display the 'edit form' of an existing Technician.
     *
     * @param id Id of the user to edit
     */
    @Security.Authenticated(Secured.class)
    public Result edit(Long id) {
        if (Secured.isAdmin(ctx())) {
            Form<Technician> technicianForm = formFactory.form(Technician.class).fill(Technician.find.byId(id));
            return ok(views.html.technician_editForm.render(id, technicianForm));
        } else {
            return badRequest(views.html.home.render(Secured.getClient(ctx())));
        }
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the user to edit
     */
    public Result update(Long id) throws PersistenceException {
        Form<Technician> technicianForm = formFactory.form(Technician.class).bindFromRequest();
        if (technicianForm.hasErrors()) {
            return badRequest(views.html.technician_editForm.render(id, technicianForm));
        }
        Transaction txn = Ebean.beginTransaction();
        try {
            Technician savedTechnician = Technician.find.byId(id);
            if (savedTechnician != null) {
                Technician newTechnicianData = technicianForm.get();
                savedTechnician.first_name = newTechnicianData.first_name;
                savedTechnician.last_name = newTechnicianData.last_name;
                savedTechnician.update();
                txn.commit();
            }
        } finally {
            txn.end();
        }
        return list();
    }

    /**
     * Handle user deletion
     */
    @Security.Authenticated(Secured.class)
    public Result delete(Long id) {
        if (Technician.canDelete(id)) {
            Technician.find.byId(id).delete();
            return list();
        } else {
            String message = "Cannot delete this technician, it is being used.";
            flash(message);
            return ok(message);
        }
    }

}
            

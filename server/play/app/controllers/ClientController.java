package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import play.db.ebean.Transactional;

import play.mvc.*;
import play.data.*;

import static play.data.Form.*;

import models.*;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import java.util.List;
import java.util.ArrayList;

/**
 * Manage a database of users
 */
public class ClientController extends Controller {

    private FormFactory formFactory;

    @Inject
    public ClientController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    /**
     * Display the list of users.
     */
    public Result list() {
        return ok(views.html.client_list.render(Client.list(), Secured.getClient(ctx())));
    }

    /**
     * Display the 'edit form' of an existing Technician.
     *
     * @param id Id of the user to edit
     */
    @Security.Authenticated(Secured.class)
    public Result edit(Long id) {
        if (Secured.isAdmin(ctx())) {
            Form<InputClient> clientForm = formFactory.form(InputClient.class).fill(InputClient.find(id));
            return ok(views.html.client_editForm.render(id, clientForm));
        } else {
            return badRequest(views.html.home.render(Secured.getClient(ctx())));
        }
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the user to edit
     */
    @Transactional
    @Security.Authenticated(Secured.class)
    public Result update(Long id) throws PersistenceException {
        Form<InputClient> clientForm = formFactory.form(InputClient.class).bindFromRequest();
        if (clientForm.hasErrors()) {
            return badRequest(views.html.client_editForm.render(id, clientForm));
        }
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            clientForm.reject("adminstrator", "Non administrators cannot change clients.");
            return badRequest(views.html.client_editForm.render(id, clientForm));
        }
        Transaction txn = Ebean.beginTransaction();
        try {
            Client savedClient = Client.find.byId(id);
            if (savedClient == null) {
                clientForm.reject("adminstrator", "Bad id : " + id);
                return badRequest(views.html.client_editForm.render(id, clientForm));
            }
            InputClient newClientData = clientForm.get();
            List<Project> projects;
            try {
                projects = newClientData.getProjectsFromLine();
            } catch (DataErrorException ex) {
                return badRequest(views.html.client_editForm.render(id, clientForm));
            }
            savedClient.name = newClientData.name;
            savedClient.password = newClientData.password;
            savedClient.update();

            ClientProjectAssociation.addNew(id, projects);

            flash("success", "Client " + savedClient.name + " has been updated");
            txn.commit();
        } finally {
            txn.end();
        }
        return list();
    }

    /**
     * Display the 'new user form'.
     */
    @Security.Authenticated(Secured.class)
    public Result create() {
        Form<InputClient> clientForm = formFactory.form(InputClient.class);
        return ok(views.html.client_createForm.render(clientForm));
    }

    /**
     * Handle the 'new user form' submission
     */
    @Security.Authenticated(Secured.class)
    @Transactional
    public Result save() {
        Form<InputClient> clientForm = formFactory.form(InputClient.class).bindFromRequest();
        if (clientForm.hasErrors()) {
            return badRequest(views.html.client_createForm.render(clientForm));
        }
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            clientForm.reject("adminstrator", "Non administrators cannot create clients.");
            return badRequest(views.html.client_createForm.render(clientForm));
        }
        InputClient input = clientForm.get();
        List<Project> projects;
        try {
            projects = input.getProjectsFromLine();
        } catch (DataErrorException ex) {
            clientForm.reject("projects", ex.getMessage());
            return badRequest(views.html.client_createForm.render(clientForm));
        }
        Client newClient = new Client();
        newClient.name = input.name;
        newClient.password = input.password;
        newClient.save();
        ClientProjectAssociation.addNew(newClient.id, projects);
        flash("success", "Client " + newClient.name + " has been created");
        return list();
    }

    /**
     * Handle user deletion
     */
    @Security.Authenticated(Secured.class)
    @Transactional
    public Result delete(Long id) {
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            Form<InputClient> clientForm = formFactory.form(InputClient.class).bindFromRequest();
            clientForm.reject("adminstrator", "Non administrators cannot delete clients.");
            return badRequest(views.html.client_editForm.render(id, clientForm));
        }
        // TODO: If the client is in the database, mark it as disabled instead.
        Client.find.ref(id).delete();
        flash("success", "Client has been deleted");
        return list();
    }

}
            

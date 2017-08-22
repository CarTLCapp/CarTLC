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
        Form<Client> clientForm = formFactory.form(Client.class).fill(Client.find.byId(id));
        if (Secured.isAdmin(ctx())) {
            return ok(views.html.client_editForm.render(id, clientForm));
        } else {
            clientForm.reject("adminstrator", "Non administrators cannot change clients.");
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
        Form<Client> clientForm = formFactory.form(Client.class).bindFromRequest();
        if (clientForm.hasErrors()) {
            return badRequest(views.html.client_editForm.render(id, clientForm));
        }
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            clientForm.reject("adminstrator", "Non administrators cannot change clients.");
            return badRequest(views.html.client_editForm.render(id, clientForm));
        }
        clientForm.get().save();
        flash("success", "Client " + clientForm.get().name + " has been updated");

        return list();
    }

    @Transactional
    @Security.Authenticated(Secured.class)
    public Result addProject(long client_id, long project_id) {
        ClientProjectAssociation.addEntry(client_id, project_id);
        return edit(client_id);
    }

    @Transactional
    @Security.Authenticated(Secured.class)
    public Result removeProject(long client_id, long project_id) {
        ClientProjectAssociation.deleteEntry(client_id, project_id);
        return edit(client_id);
    }

    @Transactional
    @Security.Authenticated(Secured.class)
    public Result addProjectCreate(long project_id) {
        Form<InputClient> clientForm = formFactory.form(InputClient.class).bindFromRequest();
        clientForm.get().addProject(project_id);
        return ok(views.html.client_createForm.render(clientForm, clientForm.get()));
    }

    @Transactional
    @Security.Authenticated(Secured.class)
    public Result removeProjectCreate(long project_id) {
        Form<InputClient> clientForm = formFactory.form(InputClient.class).bindFromRequest();
        clientForm.get().removeProject(project_id);
        return ok(views.html.client_createForm.render(clientForm, clientForm.get()));
    }

    /**
     * Display the 'new user form'.
     */
    @Security.Authenticated(Secured.class)
    public Result create() {
        Form<InputClient> clientForm = formFactory.form(InputClient.class);
        return ok(views.html.client_createForm.render(clientForm, clientForm.get()));
    }

    /**
     * Handle the 'new user form' submission
     */
    @Security.Authenticated(Secured.class)
    @Transactional
    public Result save() {
        Form<InputClient> clientForm = formFactory.form(InputClient.class).bindFromRequest();
        if (clientForm.hasErrors()) {
            return badRequest(views.html.client_createForm.render(clientForm, clientForm.get()));
        }
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            clientForm.reject("adminstrator", "Non administrators cannot create clients.");
            return badRequest(views.html.client_createForm.render(clientForm, clientForm.get()));
        }
        InputClient input = clientForm.get();
        Client newClient = new Client();
        newClient.name = input.name;
        newClient.password = input.password;
        newClient.save();
        ClientProjectAssociation.addNew(newClient.id, input.getProjects());
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
            Form<Client> clientForm = formFactory.form(Client.class).bindFromRequest();
            clientForm.reject("adminstrator", "Non administrators cannot delete clients.");
            return badRequest(views.html.client_editForm.render(id, clientForm));
        }
        // TODO: If the client is in the database, mark it as disabled instead.
        Client.find.ref(id).delete();
        flash("success", "Client has been deleted");
        return list();
    }

}
            

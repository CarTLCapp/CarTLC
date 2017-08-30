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
import play.Logger;

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
     * @param id Id of the client to edit
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
        Client updateClient = clientForm.get();
        if (Client.hasClientWithName(updateClient.name, id.intValue())) {
            return badRequest("Already a client with name: " + updateClient.name);
        }
        Client client = Client.find.byId(id);
        client.name = updateClient.name;
        client.password = updateClient.password;
        client.update();

        ClientProjectAssociation.addNew(id, getCheckedProjects(clientForm));
        flash("success", "Client " + client.name + " has been updated");

        return list();
    }

    /**
     * Display the 'new user form'.
     */
    @Security.Authenticated(Secured.class)
    public Result create() {
        Form<Client> clientForm = formFactory.form(Client.class).fill(new Client());
        return ok(views.html.client_createForm.render(clientForm));
    }

    /**
     * Handle the 'new user form' submission
     */
    @Security.Authenticated(Secured.class)
    @Transactional
    public Result save() {
        Form<Client> clientForm = formFactory.form(Client.class).bindFromRequest();
        if (clientForm.hasErrors()) {
            return badRequest(views.html.client_createForm.render(clientForm));
        }
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            clientForm.reject("adminstrator", "Non administrators cannot create clients.");
            return badRequest(views.html.client_createForm.render(clientForm));
        }
        Client newClient = clientForm.get();
        if (Client.getUser(newClient.name) != null) {
            return badRequest("Already have a client named: " + newClient.name);
        }
        newClient.save();
        ClientProjectAssociation.addNew(newClient.id, getCheckedProjects(clientForm));
        flash("success", "Client " + newClient.name + " has been created");
        return list();
    }

    List<Project> getCheckedProjects(Form<Client> clientForm) {
        List<Project> projects = new ArrayList<Project>();
        for (Project project : Project.list()) {
            if (clientForm.field(project.name) != null &&
                    clientForm.field(project.name).value() != null &&
                    clientForm.field(project.name).value().equals("true")) {
                projects.add(project);
            }
        }
        return projects;
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
        ClientProjectAssociation.deleteEntries(id);
        flash("success", "Client has been deleted");
        return list();
    }

}
            

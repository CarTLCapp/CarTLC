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
        return ok(views.html.client_list.render(Client.list()));
    }
    
    /**
     * Display the 'edit form' of an existing Client.
     *
     * @param id Id of the user to edit
     */
    @Security.Authenticated(Secured.class)
    public Result edit(Long id) {
        Form<Client> clientForm = formFactory.form(Client.class).fill(Client.find.byId(id));
        return ok(views.html.client_editForm.render(id, clientForm));
    }
    
    /**
     * Handle the 'edit form' submission 
     *
     * @param id Id of the user to edit
     */
    public Result update(Long id) throws PersistenceException {
        Form<Client> clientForm = formFactory.form(Client.class).bindFromRequest();
        if (clientForm.hasErrors()) {
            return badRequest(views.html.client_editForm.render(id, clientForm));
        }
        Transaction txn = Ebean.beginTransaction();
        try {
            Client savedClient = Client.find.byId(id);
            if (savedClient != null) {
                Client newClientData = clientForm.get();
                savedClient.first_name = newClientData.first_name;
                savedClient.last_name = newClientData.last_name;
                savedClient.update();
                flash("success", "Client " + clientForm.get().fullName() + " has been updated");
                txn.commit();
            }
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
        Form<Client> clientForm = formFactory.form(Client.class);
        return ok(views.html.client_createForm.render(clientForm));
    }
    
    /**
     * Handle the 'new user form' submission
     */
    public Result save() {
        Form<Client> clientForm = formFactory.form(Client.class).bindFromRequest();
        if(clientForm.hasErrors()) {
            return badRequest(views.html.client_createForm.render(clientForm));
        }
        clientForm.get().save();
        flash("success", "Client " + clientForm.get().fullName() + " has been created");
        return list();
    }
    
    /**
     * Handle user deletion
     */
    @Security.Authenticated(Secured.class)
    public Result delete(Long id) {
        // TODO: If the client is in the database, mark it as disabled instead.
        Client.find.ref(id).delete();
        flash("success", "Client has been deleted");
        return list();
    }

}
            

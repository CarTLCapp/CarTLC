package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import play.mvc.*;
import play.data.*;

import static play.data.Form.*;

import views.formdata.LoginFormData;
import play.mvc.Security;

import models.*;
import modules.Globals;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

public class HomeController extends Controller {

    private FormFactory mFormFactory;
    private Globals mGlobals;

    @Inject
    public HomeController(FormFactory formFactory, Globals globals) {
        this.mFormFactory = formFactory;
        this.mGlobals = globals;
    }

    @Security.Authenticated(Secured.class)
    public Result index() {
        mGlobals.setClearSearch(true);
        return ok(views.html.home.render(Secured.getClient(ctx())));
    }

    public static Result HOME() {
        return Results.redirect(routes.HomeController.index());
    }

    /**
     * Provides the Login page (only to unauthenticated users).
     *
     * @return The Login page.
     */
    public Result login() {
        Client.initClient();
        Form<LoginFormData> formData = mFormFactory.form(LoginFormData.class).bindFromRequest();
        return ok(views.html.login.render("Login", formData));
    }

    /**
     * Processes a login form submission from an unauthenticated user.
     * First we bind the HTTP POST data to an instance of LoginFormData.
     * The binding process will invoke the LoginFormData.validate() method.
     * If errors are found, re-render the page, displaying the error data.
     * If errors not found, render the page with the good data.
     *
     * @return The index page with the results of validation.
     */
    public Result postLogin() {
        Form<LoginFormData> formData = mFormFactory.form(LoginFormData.class).bindFromRequest();
        if (formData.hasErrors()) {
            flash("error", "Login credentials not valid.");
            return badRequest(views.html.login.render("Login", formData));
        } else {
            session().clear();
            session("username", formData.get().username);
            return index();
        }
    }

    /**
     * Logs out (only for authenticated users) and returns them to the Index page.
     *
     * @return A redirect to the Index page.
     */
    @Security.Authenticated(Secured.class)
    public Result logout() {
        session().clear();
        return login();
    }
}
            

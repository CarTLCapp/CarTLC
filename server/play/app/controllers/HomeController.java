/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package controllers;

import modules.AmazonHelper;
import modules.WorkerExecutionContext;
import play.libs.concurrent.HttpExecution;
import play.mvc.*;
import play.data.*;

import com.typesafe.config.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import views.formdata.LoginFormData;
import play.mvc.Security;

import models.*;
import modules.Globals;

import javax.inject.Inject;

import play.db.ebean.Transactional;

public class HomeController extends Controller {

    private FormFactory mFormFactory;
    private Globals mGlobals;
    private String mVersion;
    private Daily mDaily;

    @Inject
    public HomeController(
            FormFactory formFactory,
            Globals globals,
            Config config) {
        mFormFactory = formFactory;
        mGlobals = globals;
        mVersion = config.getString("app.version");
    }

    @Security.Authenticated(Secured.class)
    public Result index() {
        mGlobals.checkInit();
        mGlobals.setClearSearch(true);
        return ok(views.html.home.render(Secured.getClient(ctx()), mVersion, "", daily()));
    }

    @Security.Authenticated(Secured.class)
    public Result daily(long date) {
        daily().resetTo(date);
        return HOME();
    }

    public static Result HOME() {
        return Results.redirect(routes.HomeController.index());
    }

    @Security.Authenticated(Secured.class)
    public Result problem(String msg) {
        return badRequest(views.html.home.render(Secured.getClient(ctx()), mVersion, msg, daily()));
    }

    public static Result PROBLEM(String msg) {
        return Results.redirect(routes.HomeController.problem(msg));
    }

    private Daily daily() {
        if (mDaily == null) {
            mDaily = new Daily();
        }
        return mDaily;
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
            return badRequest(
                    views.html.login.render("Login", formData)
            );
        } else {
            session().clear();
            session("username", formData.get().username);
            return HOME();
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

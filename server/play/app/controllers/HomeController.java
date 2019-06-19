/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package controllers;

import modules.AmazonHelper;
import modules.WorkerExecutionContext;
import play.Logger;
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

public class HomeController extends Controller {

    private AmazonHelper mAmazonHelper;
    private FormFactory mFormFactory;
    private Globals mGlobals;
    private String mVersion;
    private Daily mDaily = new Daily();

    @Inject
    public HomeController(
            AmazonHelper amazonHelper,
            FormFactory formFactory,
            Globals globals,
            Config config) {
        mAmazonHelper = amazonHelper;
        mFormFactory = formFactory;
        mGlobals = globals;
        mVersion = config.getString("app.version");
    }

    @Security.Authenticated(Secured.class)
    public Result index() {
        mGlobals.checkInit();
        mGlobals.setClearSearch(true);
        return ok(views.html.home.render(Secured.getClient(ctx()), mVersion, "", mDaily));
    }

    @Security.Authenticated(Secured.class)
    public Result daily(long date) {
        mDaily.resetTo(date);
        return index();
    }

    public static Result HOME() {
        return Results.redirect(routes.HomeController.index());
    }

    public Result problem(String msg) {
        return badRequest(views.html.home.render(Secured.getClient(ctx()), mVersion, msg, mDaily));
    }

    public static Result PROBLEM(String msg) {
        return Results.redirect(routes.HomeController.problem(msg));
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

    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> pictureCleanup() {
        String host = request().host();
        CompletableFuture<Result> completableFuture = new CompletableFuture<>();
        Executors.newCachedThreadPool().submit(() -> {
            mAmazonHelper.list(host, (keys) -> pictureCleanup1(host, completableFuture, keys));
            return null;
        });
        return completableFuture;
    }

    private void pictureCleanup1(String host, CompletableFuture<Result> completableFuture, List<String> keys) {
        Logger.warn("Checking for orphaned pictures from " + keys.size() + " keys");
        ArrayList<String> missing = new ArrayList<>();
        for (String key : keys) {
            if (!Entry.hasEntryForPicture(key)) {
                missing.add(key);
            }
        }
        Logger.warn("ORPHANED REMOTE PICTURES=" + missing.size());
        mAmazonHelper.deleteAction().host(host).deleteLocalFile(true).listener((deleted, errors) -> {
            pictureCleanup2(host, completableFuture, deleted);
        }).delete(missing);

        Logger.warn("DONE");
    }

    private void pictureCleanup2(String host, CompletableFuture<Result> completableFuture, int deletedRemote) {
        List<String> orphanedDb = pictureCollectionIdCleanup(host);
        mAmazonHelper.deleteAction()
                .host(host)
                .deleteLocalFile(true)
                .listener((deleted, errors) -> {
                    pictureCleanup3(host, completableFuture, deletedRemote, deleted);
                })
                .delete(orphanedDb);
    }

    private void pictureCleanup3(String host, CompletableFuture<Result> completableFuture, int deletedRemote, int deletedDb) {
        int orphanedLocal = pictureLocalFileCleanup();
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("REMOTE FILES DELETED=");
        sbuf.append(deletedRemote);
        sbuf.append(", ORPHANED NODES=");
        sbuf.append(deletedDb);
        sbuf.append(", LOCAL DELETE=");
        sbuf.append(orphanedLocal);
        completableFuture.complete(ok(sbuf.toString()));
        Logger.warn(sbuf.toString());
    }

    private List<String> pictureCollectionIdCleanup(String host) {
        Logger.warn("pictureCollectionIdCleanup()");
        ArrayList<String> orphaned = new ArrayList<>();
        List<PictureCollection> list = PictureCollection.findNoEntries();
        for (PictureCollection collection : list) {
            Logger.warn("Orphaned collection: " + collection.toString());
            orphaned.add(collection.picture);
            collection.delete();
        }
        return orphaned;
    }

    private int pictureLocalFileCleanup() {
        File dir = mAmazonHelper.getLocalDirectory();
        ArrayList<File> orphaned = new ArrayList<>();
        for (File file : dir.listFiles()) {
            if (!Entry.hasEntryForPicture(file.getName())) {
                orphaned.add(file);
            }
        }
        for (File file : orphaned) {
            Logger.warn("ORPHANED LOCAL FILE DELETED: " + file.getName());
            file.delete();
        }
        return orphaned.size();
    }
}
            

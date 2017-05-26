package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import models.*;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

public class HomeController  extends Controller {

    public Result index() {
        return ok(views.html.home.render());
    }

    public static Result HOME() { return Results.redirect(routes.HomeController.index()); }
}
            

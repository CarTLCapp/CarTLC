package controllers;

import models.UserInfo;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.Http.Context;

/**
 * Implement authorization for this system.
 * getUserName() and onUnauthorized override superclass methods to restrict
 * access to the profile() page to logged in users.
 *
 * getUser(), isLoggedIn(), and getUserInfo() provide static helper methods so that controllers
 * can know if there is a logged in user.
 */
public class Secured extends Security.Authenticator {

    /**
     * Used by authentication annotation to determine if user is logged in.
     * @param ctx The context.
     * @return The email address of the logged in user, or null if not logged in.
     */
    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("username");
    }

    /**
     * Instruct authenticator to automatically redirect to login page if unauthorized.
     * @param context The context.
     * @return The login page.
     */
    @Override
    public Result onUnauthorized(Context context) {
        return redirect(routes.HomeController.login());
    }

    /**
     * Return the username of the logged in user, or null if no logged in user.
     *
     * @param ctx the context containing the session
     * @return The email of the logged in user, or null if user is not logged in.
     */
    public static String getUser(Context ctx) {
        return ctx.session().get("username");
    }

    /**
     * True if there is a logged in user, false otherwise.
     */
    public static boolean isLoggedIn(Context ctx) {
        return (getUser(ctx) != null);
    }

    /**
     * True if there is the logged in user is an adminstrator, false otherwise.
     */
    public static boolean isAdmin(Context ctx) {
        UserInfo info = getUserInfo(ctx);
        if (info != null) {
            return info.is_admin;
        }
        return false;
    }

    /**
     * Return the UserInfo of the logged in user, or null if no user is logged in.
     */
    public static UserInfo getUserInfo(Context ctx) {
        try {
            return (isLoggedIn(ctx) ? UserInfo.getUser(getUser(ctx)) : null);
        } catch (Exception ex) {
            return null;
        }
    }

}
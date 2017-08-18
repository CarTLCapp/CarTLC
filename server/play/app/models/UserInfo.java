package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import play.Logger;
/**
 * User entity managed by Ebean
 */
@Entity 
public class UserInfo extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;
    
    @Constraints.Required
    public String name;

    @Constraints.Required
    public String password;

    @Constraints.Required
    public boolean is_admin;

    public static Finder<Long,UserInfo> find = new Finder<Long,UserInfo>(UserInfo.class);

    public boolean isValid() {
        return id != 0 && name != null;
    }

    @Transactional
    public static UserInfo getUser(String username) {
        if (username == null) {
            return null;
        }
        List<UserInfo> items = find.where()
                .eq("name", username)
                .findList();
        if (items.size() == 0) {
            return null;
        }
        if (items.size() > 1) {
            Logger.error("Found more than one user with name: " + username);
        }
        return items.get(0);
    }
    /**
     * Returns true if username and password are valid credentials.
     */
    public static boolean isValid(String username, String password) {
        try {
            UserInfo userInfo = getUser(username);
            if (userInfo != null) {
                if (userInfo.password == null) {
                    return (password == null);
                }
                return userInfo.password.equals(password);
            }
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
        }
        return false;
    }

    /**
     * Adds the specified user to the UserInfoDB.
     * @param name Their name.
     * @param email Their email.
     * @param password Their password.
     */
    @Transactional
    public static void addUserInfo(String name, String password, boolean isAdmin) {
        if (getUser(name) == null) {
            UserInfo userInfo = new UserInfo();
            userInfo.name = name;
            userInfo.password = password;
            userInfo.is_admin = isAdmin;
            userInfo.save();
        }
    }

    public static void initUserInfo() {
        UserInfo.addUserInfo("admin", "admintlc", true);
        UserInfo.addUserInfo("guest", "tlc", false);
    }
}


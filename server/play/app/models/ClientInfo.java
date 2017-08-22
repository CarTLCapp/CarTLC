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
public class ClientInfo extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;
    
    @Constraints.Required
    public String name;

    @Constraints.Required
    public String password;

    @Constraints.Required
    public boolean is_admin;

    public static Finder<Long,ClientInfo> find = new Finder<Long,ClientInfo>(ClientInfo.class);

    public boolean isValid() {
        return id != 0 && name != null;
    }

    @Transactional
    public static ClientInfo getUser(String username) {
        if (username == null) {
            return null;
        }
        List<ClientInfo> items = find.where()
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
            ClientInfo clientInfo = getUser(username);
            if (clientInfo != null) {
                if (clientInfo.password == null) {
                    return (password == null);
                }
                return clientInfo.password.equals(password);
            }
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
        }
        return false;
    }

    /**
     * Adds the specified user to the ClientInfoDB.
     * @param name Their name.
     * @param email Their email.
     * @param password Their password.
     */
    @Transactional
    public static void addClientInfo(String name, String password, boolean isAdmin) {
        if (getUser(name) == null) {
            ClientInfo clientInfo = new ClientInfo();
            clientInfo.name = name;
            clientInfo.password = password;
            clientInfo.is_admin = isAdmin;
            clientInfo.save();
        }
    }

    public static void initClientInfo() {
        ClientInfo.addClientInfo("admin", "admintlc", true);
        ClientInfo.addClientInfo("guest", "tlc", false);
    }
}


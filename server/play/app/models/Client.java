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
public class Client extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String password;

    @Constraints.Required
    public boolean is_admin;

    public static Finder<Long, Client> find = new Finder<Long, Client>(Client.class);

    public boolean isValid() {
        return id != null && id != 0 && name != null;
    }

    public static Client get(long id) {
        if (id > 0) {
            return find.ref(id);
        }
        return null;
    }

    public static List<Client> list() {
        return find.all();
    }

    @Transactional
    public static Client getUser(String username) {
        if (username == null) {
            return null;
        }
        List<Client> items = find.where()
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
            Client clientInfo = getUser(username);
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
     * Adds the specified user to the DB.
     *
     * @param name     Their name.
     * @param email    Their email.
     * @param password Their password.
     */
    @Transactional
    public static void addClient(String name, String password, boolean isAdmin) {
        if (getUser(name) == null) {
            Client client = new Client();
            client.name = name;
            client.password = password;
            client.is_admin = isAdmin;
            client.save();
        }
    }

    public static void initClient() {
        Client.addClient("admin", "admintlc", true);
        Client.addClient("guest", "tlc", false);
    }

    public String getProjectsLine() {
        return getProjects(", ");
    }

    public List<Project> getProjects() {
        return ClientProjectAssociation.findProjects(id);
    }
    public String getProjects(String split) {
        StringBuilder sbuf = new StringBuilder();
        List<Project> projects = getProjects();
        for (Project project : projects) {
            if (sbuf.length() > 0) {
                sbuf.append(split);
            }
            sbuf.append(project.name);
        }
        return sbuf.toString();
    }

    public static boolean hasProject(long client_id, long project_id) {
        Client client = find.byId(client_id);
        if (client != null) {
            return client.hasProject(project_id);
        }
        return false;
    }

    public boolean hasProject(long project_id) {
        return ClientProjectAssociation.hasProject(id, project_id);
    }

}


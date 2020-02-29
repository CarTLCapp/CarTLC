/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
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

    private static final String NAME_ADMIN = "admin";
    private static final String NAME_GUEST = "guest";

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String password;

    @Constraints.Required
    public boolean is_admin;

    @Constraints.Required
    public boolean disabled;

    public static Finder<Long, Client> find = new Finder<Long, Client>(Client.class);

    public boolean isValid() {
        return id != null && id != 0 && name != null;
    }

    public static Client get(long id) {
        if (id > 0) {
            return find.byId(id);
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

    public static boolean hasClientWithName(String username, int ignoreId) {
        List<Client> items = find.where()
                .eq("name", username)
                .ne("id", ignoreId)
                .findList();
        return items.size() > 0;
    }

    /**
     * Returns true if username and password are valid credentials.
     */
    public static boolean isValid(String username, String password) {
        try {
            Client clientInfo = getUser(username);
            if (clientInfo != null && !clientInfo.disabled) {
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
        Client.addClient(NAME_ADMIN, "admintlc", true);
        Client.addClient(NAME_GUEST, "tlc", false);
    }

    public static Client getAdmin() {
        return getUser(NAME_ADMIN);
    }

    public String getProjectsLine() {
        return getProjects(", ");
    }

    public List<Project> getProjects() {
        if (id == null) {
            return null;
        }
        return ClientProjectAssociation.findProjects(id);
    }

    public String getProjects(String split) {
        StringBuilder sbuf = new StringBuilder();
        List<Project> projects = getProjects();
        for (Project project : projects) {
            if (sbuf.length() > 0) {
                sbuf.append(split);
            }
            sbuf.append(project.getFullProjectName());
        }
        return sbuf.toString();
    }

    // HAS PROJECT

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

    // COMPANY NAMES

    public String getCompanyLine() {
        return ClientCompanyNameAssociation.getCompanyLine(id);
    }

    public List<String> getCompanyNames() {
        if (id == null) {
            return new ArrayList<>();
        }
        return ClientCompanyNameAssociation.findCompaniesFor(id);
    }

    // VISIBLES

    public static boolean canViewPictures(long client_id) {
        return isAdmin(client_id) || ClientAssociation.hasShowPictures(client_id);
    }

    public static boolean canViewPictures(Client client) {
        return client.id != null && canViewPictures(client.id);
    }

    public static boolean canViewTrucks(long client_id) {
        return isAdmin(client_id) || ClientAssociation.hasShowTrucks(client_id);
    }

    public static boolean canViewTrucks(Client client) {
        return client.id != null && canViewTrucks(client.id);
    }

    public static boolean isAdmin(long client_id) {
        Client client = find.byId(client_id);
        return client != null && client.is_admin;
    }

    public static boolean canViewAllNotes(long client_id) {
        return client_id == 0 || isAdmin(client_id) || ClientAssociation.hasShowAllNotes(client_id);
    }

    public static boolean canViewAllEquipments(long client_id) {
        return client_id == 0 || isAdmin(client_id) || ClientAssociation.hasShowAllEquipments(client_id);
    }

    public String getCanViewPictures() {
        return is_admin || ClientAssociation.hasShowPictures(id) ? "True" : "False";
    }

    public String getCanViewTrucks() {
        return is_admin || ClientAssociation.hasShowTrucks(id) ? "True" : "False";
    }

    public String getViewableNotes() {
        if (canViewAllNotes(id)) {
            return "ALL";
        } else {
            StringBuilder sbuf = new StringBuilder();
            List<Note> notes = ClientNoteAssociation.getNotes(id);
            boolean first = true;
            for (Note note : notes) {
                if (first) {
                    first = false;
                } else {
                    sbuf.append(", ");
                }
                sbuf.append(note.name);
            }
            if (sbuf.length() == 0) {
                return "NONE";
            }
            return sbuf.toString();
        }
    }

    public String getViewableEquipments() {
        if (canViewAllEquipments(id)) {
            return "ALL";
        } else {
            StringBuilder sbuf = new StringBuilder();
            List<Equipment> items = ClientEquipmentAssociation.getEquipments(id);
            boolean first = true;
            for (Equipment equipment : items) {
                if (first) {
                    first = false;
                } else {
                    sbuf.append(", ");
                }
                sbuf.append(equipment.name);
            }
            if (sbuf.length() == 0) {
                return "NONE";
            }
            return sbuf.toString();
        }
    }

}


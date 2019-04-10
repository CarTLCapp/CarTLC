/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.format.*;
import play.data.validation.*;
import play.Logger;

import com.avaje.ebean.*;
import play.db.ebean.Transactional;

/**
 * Project entity managed by Ebean
 */
@Entity
public class ClientProjectAssociation extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public Long client_id;

    @Constraints.Required
    public Long project_id;

    public static Finder<Long, ClientProjectAssociation> find = new Finder<Long, ClientProjectAssociation>(ClientProjectAssociation.class);

    public static List<ClientProjectAssociation> list() {
        return find.all();
    }

    public static List<Project> findProjects(long client_id) {
        List<ClientProjectAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        List<Project> list = new ArrayList<Project>();
        for (ClientProjectAssociation item : items) {
            Project project = Project.find.byId(item.project_id);
            if (project == null) {
                Logger.error("Could not locate project ID " + item.project_id);
            } else {
                list.add(project);
            }
        }
        return list;
    }

    public static List<String> findRootProjectNames(long client_id) {
        List<Project> projects = findProjects(client_id);
        ArrayList<String> names = new ArrayList<String>();
        for (Project project : projects) {
            String name = project.getRootProjectName();
            if (!names.contains(name)) {
                names.add(name);
            }
        }
        if (names.size() == 0) {
            return RootProject.listNames();
        }
        return names;
    }

    public static boolean hasProject(long client_id, long project_id) {
        List<ClientProjectAssociation> items = find.where()
                .eq("client_id", client_id)
                .eq("project_id", project_id)
                .findList();
        return items.size() > 0;
    }

    @Transactional
    public static void addEntry(long client_id, long project_id) {
        if (!hasProject(client_id, project_id)) {
            ClientProjectAssociation entry = new ClientProjectAssociation();
            entry.client_id = client_id;
            entry.project_id = project_id;
            entry.save();
        }
    }

    @Transactional
    public static void deleteEntry(long client_id, long project_id) {
        List<ClientProjectAssociation> items = find.where()
                .eq("client_id", client_id)
                .eq("project_id", project_id)
                .findList();
        for (ClientProjectAssociation item : items) {
            item.delete();
        }
    }

    public static void deleteEntries(long client_id) {
        List<ClientProjectAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        for (ClientProjectAssociation item : items) {
            item.delete();
        }
    }

    public static void addNew(long client_id, List<Project> projects) {
        deleteEntries(client_id);
        for (Project project : projects) {
            ClientProjectAssociation entry = new ClientProjectAssociation();
            entry.client_id = client_id;
            entry.project_id = project.id;
            entry.save();
        }
    }

}


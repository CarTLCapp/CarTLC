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

/**
 * Project entity managed by Ebean
 */
@Entity
public class RootProject extends Model implements Comparable<RootProject> {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public boolean disabled;

    public static Finder<Long, RootProject> find = new Finder<Long, RootProject>(RootProject.class);

    public static RootProject get(long id) {
        if (id > 0) {
            return find.byId(id);
        }
        return null;
    }

    public static List<RootProject> list() {
        return list(false);
    }

    public static List<RootProject> list(boolean disabled) {
        return find.where()
                .eq("disabled", disabled)
                .orderBy("name asc").findList();
    }

    public static ArrayList<String> listNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (RootProject project : list()) {
            names.add(project.name);
        }
        return names;
    }

    public static ArrayList<String> listNamesWithBlank() {
        ArrayList<String> names = listNames();
        names.add(0, "");
        return names;
    }

    public static RootProject findByName(String name) {
        if (name == null || name.length() == 0) {
            return null;
        }
        List<RootProject> projects = find.where()
                .eq("name", name)
                .findList();
        if (projects.size() == 1) {
            return projects.get(0);
        } else if (projects.size() > 1) {
            error("Too many projects named: " + name);
        }
        return null;
    }

    public static boolean isValid(String name) {
        return findByName(name) != null;
    }

    @Override
    public int compareTo(RootProject project) {
        return name.compareTo(project.name);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof RootProject) {
            return equals((RootProject) other);
        }
        return super.equals(other);
    }

    public boolean equals(RootProject other) {
        return name.equals(other.name);
    }

    public static boolean hasDisabled() {
        return list(true).size() > 0;
    }

    public static boolean isDisabled(long id) {
        RootProject project = find.byId(id);
        if (project == null) {
            return false;
        }
        return project.disabled;
    }

    public static List<Long> findMatches(String name) {
        List<RootProject> projects = find.where()
                .eq("name", name)
                .findList();
        List<Long> result = new ArrayList<Long>();
        for (RootProject project : projects) {
            result.add(project.id);
        }
        return result;
    }

    // region Logger

    private static void error(String msg) {
        Logger.error(msg);
    }

    private static void warn(String msg) {
        Logger.warn(msg);
    }

    private static void info(String msg) {
        Logger.info(msg);
    }

    private static void debug(String msg) {
        Logger.debug(msg);
    }

    // endregion Logger

}
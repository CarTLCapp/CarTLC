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
public class Project extends Model implements Comparable<Project> {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public boolean disabled;

    @Constraints.Required
    public Long root_project_id;

    public static Finder<Long, Project> find = new Finder<Long, Project>(Project.class);

    public static Project get(long id) {
        if (id > 0) {
            return find.byId(id);
        }
        return null;
    }

    public static List<Project> list() {
        return list(false);
    }

    public static List<Project> list(boolean disabled) {
        List<Project> projects = find.where()
                .eq("disabled", disabled)
                .findList();
        Collections.sort(projects);
        return projects;
    }

    public static List<Project> listWithRoot(long root_project_id) {
        return find.where()
                .eq("root_project_id", root_project_id)
                .findList();
    }

    public static List<String> listSubProjectNamesWithBlank(String rootName) {
        ArrayList<String> names = listSubProjectNames(rootName);
        names.add(0, "");
        return names;
    }

    public static ArrayList<String> listSubProjectNames(String rootName) {
        List<Project> projects = listSubProjects(rootName);
        ArrayList<String> names = new ArrayList<String>();
        for (Project project : projects) {
            names.add(project.name);
        }
        Collections.sort(names);
        return names;
    }

    public static List<Project> listSubProjects(String rootName) {
        List<Project> projects = new ArrayList<Project>();
        RootProject rootProject;
        if (rootName == null) {
            rootProject = null;
        } else {
            rootProject = RootProject.findByName(rootName);
        }
        if (rootProject != null) {
            projects = find
                    .where()
                    .eq("disabled", false)
                    .eq("root_project_id", rootProject.id)
                    .findList();
        } else {
            projects = find
                    .where()
                    .eq("disabled", false)
                    .eq("root_project_id", null)
                    .orderBy("name asc")
                    .findList();
        }
        return projects;
    }

    public static Project findByName(String name) {
        String[] names = split(name);
        if (names.length > 1) {
            return findByName(names[0], names[1]);
        }
        if (names.length == 0) {
            return null;
        }
        return findByName(names[0]);
    }

    public static Project findByName(String root, String subproject) {
        RootProject rootProject = RootProject.findByName(root);
        String name;
        if (rootProject == null) {
            String projectName;
            if (root != null && root.length() > 0) {
                if (subproject != null && subproject.length() > 0) {
                    Logger.error("Project.findByName(): Could not find project: " + root + " - " + subproject);
                    return null;
                }
                projectName = root;
            } else if (subproject != null && subproject.length() > 0) {
                projectName = subproject;
            } else {
                Logger.error("Project.findByName(): Could not find NULL project");
                return null;
            }
            List<Project> projects = find.where()
                    .eq("name", projectName)
                    .findList();
            if (projects.size() == 1) {
                return projects.get(0);
            } else if (projects.size() > 1) {
                Logger.error("Project.findByName(): Too many projects named: " + projectName);
            }
            return null;
        }
        List<Project> projects = find.where()
                .eq("name", subproject)
                .eq("root_project_id", rootProject.id)
                .findList();
        if (projects.size() == 1) {
            return projects.get(0);
        } else if (projects.size() > 1) {
            Logger.error("Project.findByName(): Too many projects named: " + root + " - " + subproject);
        }
        return null;
    }

    // Split the line into a root project and sub project separated by a dash.
    // For example: Alamo - AMC
    public static String[] split(String line) {
        int pos = line.indexOf("-");
        if (pos >= 0) {
            String root = line.substring(0, pos).trim();
            String sub = line.substring(pos + 1).trim();
            return new String[]{root, sub};
        }
        return new String[]{line};
    }

    public static boolean isValid(String name) {
        return findByName(name) != null;
    }

    public static List<Long> findMatches(String name) {
        List<Project> projects = find.where()
                .ilike("name", "%" + name + "%")
                .findList();
        List<Long> result = new ArrayList<Long>();
        for (Project project : projects) {
            result.add(project.id);
        }
        return result;
    }

    public static List<Long> findWithRootProjectId(Long rootProjectId) {
        List<Project> projects = find.where()
                .eq("root_project_id", rootProjectId)
                .findList();
        List<Long> result = new ArrayList<Long>();
        for (Project project : projects) {
            result.add(project.id);
        }
        return result;
    }

    public String getEquipmentsLine() {
        List<Equipment> items = ProjectEquipmentCollection.findEquipments(id);
        Collections.sort(items);
        StringBuilder sbuf = new StringBuilder();
        for (Equipment item : items) {
            if (sbuf.length() > 0) {
                sbuf.append(", ");
            }
            sbuf.append(item.name);
        }
        return sbuf.toString();
    }

    public String getNotesLine() {
        List<Note> items = ProjectNoteCollection.findNotes(id);
        Collections.sort(items);
        StringBuilder sbuf = new StringBuilder();
        for (Note item : items) {
            if (sbuf.length() > 0) {
                sbuf.append(", ");
            }
            sbuf.append(item.name);
        }
        return sbuf.toString();
    }

    public Boolean isValid(boolean withRoot) {
        if (withRoot) {
            return root_project_id != null && root_project_id > 0;
        } else {
            return root_project_id == null || root_project_id == 0;
        }
    }

    public static Boolean IsValid(long projectId, boolean withRoot) {
        Project project = get(projectId);
        if (project == null) {
            return false;
        }
        return project.isValid(withRoot);
    }

    public static boolean hasDisabled() {
        return find.where()
                .eq("disabled", true)
                .findRowCount() > 0;
    }

    public int countEntries() {
        return Entry.countEntriesForProject(id);
    }

    public static boolean isDisabled(long id) {
        Project project = find.byId(id);
        if (project == null) {
            return false;
        }
        return project.disabled;
    }

    // region NAME & COMPARE

    @Transient
    private String savedRootProjectName;
    @Transient
    private Long savedRootProjectId = 0L;

    public String getRootProjectName() {
        if (root_project_id == null) {
            return null;
        }
        if (savedRootProjectId != root_project_id || savedRootProjectName == null) {
            RootProject rootProject = RootProject.find.byId(root_project_id);
            if (rootProject == null) {
                return null;
            }
            savedRootProjectId = root_project_id;
            savedRootProjectName = rootProject.name;
        }
        return savedRootProjectName;
    }

    @Override
    public int compareTo(Project project) {
        return getFullProjectName().compareTo(project.getFullProjectName());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Project) {
            return equals((Project) other);
        }
        return super.equals(other);
    }

    public boolean equals(Project other) {
        return getFullProjectName().equals(other.getFullProjectName());
    }

    public String getProjectNameOrDash() {
        if (name == null) {
            return "-";
        }
        return name;
    }

    public String getFullProjectName() {
        String root = getRootProjectName();
        if (root != null && root.length() > 0) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(root);
            sbuf.append(" - ");
            sbuf.append(name);
            return sbuf.toString();
        }
        return getProjectNameOrDash();
    }

    // endregion NAME & COMPARE

}


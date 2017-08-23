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

    public static Finder<Long,Project> find = new Finder<Long,Project>(Project.class);

    public static List<Project> list() {
        return list(false);
    }

    public static List<Project> list(boolean disabled) {
        return find.where()
                .eq("disabled", disabled)
                .orderBy("name asc").findList();
    }

    public static Project findByName(String name) {
        List<Project> projects = find.where()
                .eq("name", name)
                .findList();
        if (projects.size() == 1) {
            return projects.get(0);
        } else if (projects.size() > 1) {
            Logger.error("Too many projects named: " + name);
        }
        return null;
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

    @Override
    public int compareTo(Project project) {
        return name.compareTo(project.name);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Project) {
            return equals((Project) other);
        }
        return super.equals(other);
    }

    public boolean equals(Project other) {
        return name.equals(other.name);
    }

    public static boolean hasDisabledProjects() {
        return list(true).size() > 0;
    }

    public int countEntries() {
        return Entry.countEntriesForProject(id);
    }

    public static boolean isDisabled(long id) {
        Project project = find.ref(id);
        if (project == null) {
            return false;
        }
        return project.disabled;
    }

}


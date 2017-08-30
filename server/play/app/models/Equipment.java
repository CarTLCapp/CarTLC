package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;
import play.Logger;

/**
 * Equipment entity managed by Ebean
 */
@Entity 
public class Equipment extends Model implements Comparable<Equipment> {

    public static class MalformedFieldException extends Exception {
        public MalformedFieldException(String message) {
            super(message);
        }
    }

    private static final long serialVersionUID = 1L;

	@Id
    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public int created_by;

    @Constraints.Required
    public boolean disabled;

    @Constraints.Required
    public boolean created_by_client;
    /**
     * Generic query helper for entity Computer with id Long
     */
    public static Finder<Long,Equipment> find = new Finder<Long,Equipment>(Equipment.class);

    public static List<Equipment> list() { return list("name", "asc"); }

    public static List<Equipment> list(String sortBy, String order) {
        return find.where()
                        .orderBy(sortBy + " " + order)
                        .findList();
    }

    public static List<Equipment> appList(int tech_id) {
        List<Equipment> items = find.where().eq("disabled", false).findList();
        List<Equipment> result = new ArrayList<Equipment>();
        for (Equipment item : items) {
            if (item.created_by == 0 || item.created_by == tech_id) {
                result.add(item);
            } else if (Entry.hasEntryForEquipment(tech_id, item.id)) {
                result.add(item);
            }
        }
        return result;
    }

    public static List<Equipment> findByName(String name) {
       return find.where().eq("name", name).findList();
    }

    public List<Project> getProjects() {
        List<Project> list = ProjectEquipmentCollection.findProjects(id);
        if (created_by != 0) {
            for (Project project : Project.find.all()) {
                if (Entry.hasEquipment(project.id, id)) {
                    if (!list.contains(project)) {
                        list.add(project);
                    }
                }
            }
        }
        return list;
    }

    public String getProjectsLine() {
        List<Project> items = getProjects();
        Collections.sort(items);
        StringBuilder sbuf = new StringBuilder();
        for (Project project : items) {
            if (sbuf.length() > 0) {
                sbuf.append(", ");
            }
            sbuf.append(project.name);
        }
        return sbuf.toString();
    }

    public String getCreatedBy() {
        if (created_by != 0) {
            if (created_by_client) {
                Client client = Client.find.byId((long) created_by);
                if (client != null) {
                    return client.name;
                }
            } else {
                Technician tech = Technician.find.byId((long) created_by);
                if (tech != null) {
                    return tech.fullName();
                }
            }
        }
        return "";
    }

    public static boolean hasProject(long equipment_id, long project_id) {
        Equipment equipment = find.byId(equipment_id);
        if (equipment != null) {
            return equipment.hasProject(project_id);
        }
        return false;
    }

    public boolean hasProject(long project_id) {
        for (Project project : getProjects()) {
            if (project.id == project_id) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasEquipmentWithName(String name, long ignoreId) {
        List<Equipment> items = find.where()
                .eq("name", name)
                .ne("id", ignoreId)
                .findList();
        return items.size() > 0;
    }

    @Override
    public int compareTo(Equipment item) {
        return name.compareTo(item.name);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Equipment) {
            return name.equals(((Equipment) other).name);
        }
        if (other instanceof Long) {
            return id == ((Long) other);
        }
        return super.equals(other);
    }
}


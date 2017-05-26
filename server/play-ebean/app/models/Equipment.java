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
    public boolean disabled;

    /**
     * Generic query helper for entity Computer with id Long
     */
    public static Finder<Long,Equipment> find = new Finder<Long,Equipment>(Equipment.class);

    public static List<Equipment> list() { return list("name", "asc"); }

    public static List<Equipment> list(String sortBy, String order) {
        return
                find.where()
                        .orderBy(sortBy + " " + order)
                        .findList();
    }

    public static Equipment findByName(String name) {
        List<Equipment> items = find.where()
                .eq("name", name)
                .findList();
        if (items.size() == 1) {
            return items.get(0);
        } else if (items.size() > 1) {
            Logger.error("Too many equipments named: " + name);
        }
        return null;
    }

    public String getProjectsLine() {
        List<Project> items = ProjectEquipmentCollection.findProjects(id);
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

    @Override
    public int compareTo(Equipment item) {
        return name.compareTo(item.name);
    }
}


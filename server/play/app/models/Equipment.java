/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;
import java.lang.StringBuilder;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.format.*;
import play.data.validation.*;
import play.data.Form;

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
    private static final int  PAGE_SIZE        = 25;

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
    public static Finder<Long, Equipment> find = new Finder<Long, Equipment>(Equipment.class);

    public static List<Equipment> list() {
        return list(false);
    }

    public static List<Equipment> list(boolean disabled) {
        return list("name", "asc", disabled);
    }

    public static List<Equipment> list(String sortBy, String order, boolean disabled) {
        return find.where()
                .eq("disabled", disabled)
                .orderBy(sortBy + " " + order)
                .findList();
    }

    public static PagedList<Equipment> list(int page, boolean disabled) {
        return find.where()
                .eq("disabled", disabled)
                .findPagedList(page, PAGE_SIZE);
    }

    public static List<Equipment> all() {
        return find.where().eq("disabled", false).findList();
    }

    public static List<Equipment> appList(int tech_id) {
        List<Equipment> items = find.where().eq("disabled", false).findList();
        List<Equipment> result = new ArrayList<Equipment>();
        for (Equipment item : items) {
            if (item.created_by_client) {
                result.add(item);
            } else if (item.created_by == 0 || item.created_by == tech_id) {
                result.add(item);
            } else if (Entry.hasEntryForEquipment(tech_id, item.id)) {
                result.add(item);
            }
        }
        return result;
    }

    public static List<Equipment> getCreatedByClient(int client_id) {
        return find.where()
                .eq("created_by", client_id)
                .eq("created_by_client", true)
                .findList();
    }

    public static Equipment get(long id) {
        if (id > 0) {
            return find.ref(id);
        }
        return null;
    }

    public static List<Equipment> findByName(String name) {
        return find.where().eq("name", name).findList();
    }

    public List<Project> getProjects() {
        return ProjectEquipmentCollection.findProjects(id);
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

    public int getNumEntries() {
        return Entry.countEntriesForEquipment(id);
    }

    private String getTag(String key) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(key);
        sbuf.append(id);
        return sbuf.toString();
    }

    public String getTagNumEntries() {
        return getTag("num_entries");
    }

    public String getTagProjectsLine() {
        return getTag("projects");
    }

    public String getCreatedBy() {
        StringBuilder sbuf = new StringBuilder();
        if (created_by != 0) {
            if (created_by_client) {
                Client client = Client.find.byId((long) created_by);
                if (client != null) {
                    sbuf.append(client.name);
                }
            } else {
                Technician tech = Technician.find.byId((long) created_by);
                if (tech != null) {
                    sbuf.append(tech.fullName());
                }
            }
        }
        if (disabled) {
            sbuf.append(" [DISABLED]");
        }
        return sbuf.toString();
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

    public static boolean isDisabled(Long id) {
        Equipment equipment = find.ref(id);
        if (equipment == null) {
            return false;
        }
        return equipment.disabled;
    }

    public static boolean hasDisabled() {
        return find.where().eq("disabled", true).findList().size() > 0;
    }

    public boolean isOther() {
        return name.startsWith("Other");
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

    public void remove() {
        ProjectEquipmentCollection.deleteByEquipmentId(id);
        delete();
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(id);
        sbuf.append(":");
        sbuf.append(name);
        sbuf.append(",");
        sbuf.append(disabled);
        sbuf.append(",");
        sbuf.append(getCreatedBy());
        return sbuf.toString();
    }

    public static List<Long> findMatches(String name) {
        List<Equipment> equipments = find.where()
                .ilike("name", "%" + name + "%")
                .findList();
        List<Long> result = new ArrayList<Long>();
        for (Equipment equip : equipments) {
            result.add(equip.id);
        }
        return result;
    }

    public static List<Equipment> getChecked(Form entryForm) {
        List<Equipment> equipments = new ArrayList<Equipment>();
        for (Equipment equipment : Equipment.list()) {
            try {
                if (entryForm.field(equipment.name).getValue().get().equals("true")) {
                    equipments.add(equipment);
                }
            } catch (Exception ex) {
            }
        }
        return equipments;
    }

}


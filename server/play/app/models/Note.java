/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.format.*;
import play.data.validation.*;
import play.data.Form;
import play.db.ebean.Transactional;

import com.avaje.ebean.*;

import play.Logger;

/**
 * Note entity managed by Ebean
 */
@Entity
public class Note extends Model implements Comparable<Note> {

    public enum Type {
        TEXT("Text"),
        NUMERIC("Numeric"),
        ALPHANUMERIC("Alphanumeric"),
        NUMERIC_WITH_SPACES("Numeric_with_spaces"),
        MULTILINE("Multiline");

        public String display;

        Type(String display) {
            this.display = display;
        }

        public static Type from(int ord) {
            for (Type value : values()) {
                if (value.ordinal() == ord) {
                    return value;
                }
            }
            return Type.TEXT;
        }

        public static Type from(String item) {
            String search = item.toLowerCase();
            for (Type value : values()) {
                if (value.toString().toLowerCase().equals(search)) {
                    return value;
                }
            }
            return null;
        }
    }

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
    public Type type = Type.TEXT;

    @Constraints.Required
    public int created_by;

    @Constraints.Required
    public boolean disabled;

    @Constraints.Required
    public boolean created_by_client;

    @Constraints.Required
    public short num_digits;

    /**
     * General purpose notes
     */

    /**
     * NOTE_TRUCK_NUMBER_NAME and NOTE_TRUCK_DAMAGE_NAME identifies the textual portion of these values.
     * They are used along with the pictures which are tracked as part of a flow. See EntryController
     * "truck_number" and "truck_damage".
     */
    public static final String NOTE_TRUCK_NUMBER_NAME = "Truck Number";
    public static final String NOTE_TRUCK_DAMAGE_NAME = "Truck Damage";
    /**
     * NOTE_PARTIAL_INSTALL_REASON identifies the note associated with the reason for the partial install.
     */
    public static final String NOTE_PARTIAL_INSTALL_REASON = "Partial Install";

    @Transactional
    public static void initGeneralPurpose() {
        final boolean hasNumber = hasNoteWithName(NOTE_TRUCK_NUMBER_NAME);
        final boolean hasDamage = hasNoteWithName(NOTE_TRUCK_DAMAGE_NAME);
        final boolean hasPartialInstall = hasNoteWithName(NOTE_PARTIAL_INSTALL_REASON);
        if (!hasNumber || !hasDamage || !hasPartialInstall) {
            final int client_id = Client.getAdmin().id.intValue();
            if (!hasNumber) {
                Note note = new Note();
                note.created_by = client_id;
                note.created_by_client = false;
                note.disabled = false;
                note.name = NOTE_TRUCK_NUMBER_NAME;
                note.type = Type.ALPHANUMERIC;
                note.save();
                Logger.info("ADDED " + note.name);
            }
            if (!hasDamage) {
                Note note = new Note();
                note.created_by = client_id;
                note.created_by_client = false;
                note.disabled = false;
                note.name = NOTE_TRUCK_DAMAGE_NAME;
                note.type = Type.TEXT;
                note.save();
                Logger.info("ADDED " + note.name);
            }
            if (!hasPartialInstall) {
                Note note = new Note();
                note.created_by = client_id;
                note.created_by_client = false;
                note.disabled = false;
                note.name = NOTE_PARTIAL_INSTALL_REASON;
                note.type = Type.TEXT;
                note.save();
                Logger.info("ADDED " + note.name);
            }
            Version.inc(Version.VERSION_NOTE);
        }
    }

    /**
     * Generic query helper for entity Computer with id Long
     */
    public static Finder<Long, Note> find = new Finder<Long, Note>(Note.class);

    public static List<Note> list() {
        return list(false);
    }

    public static List<Note> list(boolean disabled) {
        return list("name", "asc", disabled);
    }

    public static List<Note> list(String sortBy, String order, boolean disabled) {
        return find.where()
                .eq("disabled", disabled)
                .orderBy(sortBy + " " + order)
                .findList();
    }

    public static Note get(long id) {
        return find.byId(id);
    }

    public static boolean isDisabled(long note_id) {
        Note note = find.byId(note_id);
        if (note == null) {
            return true;
        }
        return note.disabled;
    }

    public static List<Note> findByName(String name) {
        return find.where().eq("name", name).findList();
    }

    public static List<Note> appList() {
        return find.where().eq("disabled", false).findList();
    }

    public static List<Note> getCreatedByClient(int client_id) {
        return find.where()
                .eq("created_by", client_id)
                .eq("created_by_client", true)
                .findList();
    }

    public List<Project> getProjects() {
        return ProjectNoteCollection.findProjects(id);
    }

    public String getProjectsLine() {
        List<Project> items = getProjects();
        Collections.sort(items);
        StringBuilder sbuf = new StringBuilder();
        for (Project project : items) {
            if (sbuf.length() > 0) {
                sbuf.append(", ");
            }
            sbuf.append(project.getFullProjectName());
        }
        return sbuf.toString();
    }

    public String getTypeString() {
        return type.display;
    }

    public String getNumDigits() {
        if (num_digits > 0) {
            return Integer.toString(num_digits);
        }
        return "";
    }

    public String getNumEntries() {
        return Integer.toString(Entry.countEntriesForNote(id));
    }

    public static boolean hasProject(long note_id, long project_id) {
        Note note = find.byId(note_id);
        if (note != null) {
            return note.hasProject(project_id);
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

    public static boolean isDisabled(Long id) {
        Note note = find.byId(id);
        if (note == null) {
            return false;
        }
        return note.disabled;
    }

    public static boolean hasDisabled() {
        return find.where().eq("disabled", true).findRowCount() > 0;
    }

    public static boolean hasNoteWithName(String name, long ignoreId) {
        return find.where()
                .eq("name", name)
                .ne("id", ignoreId)
                .findRowCount() > 0;
    }

    public static boolean hasNoteWithName(String name) {
        return find.where()
                .eq("name", name)
                .findRowCount() > 0;
    }

    public static Map<String, String> options() {
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        for (Type t : Type.values()) {
            options.put(t.toString(), t.display);
        }
        return options;
    }

    public String getCreatedBy() {
        StringBuilder sbuf = new StringBuilder();
        if (created_by != 0) {
            if (created_by_client) {
                Client client = Client.find.byId((long) created_by);
                if (client != null) {
                    sbuf.append(client.name);
                } else {
                    sbuf.append(Technician.RIP);
                }
            } else {
                Technician tech = Technician.find.byId((long) created_by);
                if (tech != null) {
                    sbuf.append(tech.fullName());
                } else {
                    sbuf.append(Technician.RIP);
                }
            }
        }
        if (disabled) {
            sbuf.append(" [DISABLED]");
        }
        return sbuf.toString();
    }

    @Override
    public int compareTo(Note item) {
        return name.compareTo(item.name);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Note) {
            return name.equals(((Note) other).name);
        }
        if (other instanceof Long) {
            return id == ((Long) other);
        }
        return super.equals(other);
    }

    public String idString() {
        return "N" + id;
    }

    public String idValueString() {
        return "VALUE" + id;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(id);
        sbuf.append(":");
        sbuf.append(name);
        sbuf.append(",");
        sbuf.append(getTypeString());
        sbuf.append(",");
        sbuf.append(num_digits);
        sbuf.append(",");
        sbuf.append(disabled);
        sbuf.append(",");
        sbuf.append(getCreatedBy());
        return sbuf.toString();
    }

    public static List<Note> getChecked(Form entryForm) {
        List<Note> notes = new ArrayList<Note>();
        for (Note note : Note.list()) {
            if (ClientAssociation.isTrue(entryForm, note.idString())) {
                notes.add(note);
            }
        }
        return notes;
    }

}


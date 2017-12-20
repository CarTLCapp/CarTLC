package models;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.format.*;
import play.data.validation.*;

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
        NUMERIC_WITH_SPACES("Numeric w/ Spaces"),
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
            return Type.TEXT;
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
     * Generic query helper for entity Computer with id Long
     */
    public static Finder<Long, Note> find = new Finder<Long, Note>(Note.class);

    public static List<Note> list() {
        return list(false );
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

    public static boolean isDisabled(long note_id) {
        Note note = find.ref(note_id);
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
            sbuf.append(project.name);
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
        Note note = find.ref(id);
        if (note == null) {
            return false;
        }
        return note.disabled;
    }

    public static boolean hasDisabled() {
        return find.where().eq("disabled", true).findList().size() > 0;
    }

    public static boolean hasNoteWithName(String name, long ignoreId) {
        List<Note> notes = find.where()
                .eq("name", name)
                .ne("id", ignoreId)
                .findList();
        return notes.size() > 0;
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
}


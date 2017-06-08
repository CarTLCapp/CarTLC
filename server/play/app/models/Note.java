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
    /**
     * Generic query helper for entity Computer with id Long
     */
    public static Finder<Long,Note> find = new Finder<Long,Note>(Note.class);

    public static List<Note> list() { return list("name", "asc"); }

    public static List<Note> list(String sortBy, String order) {
        return find.where()
                .orderBy(sortBy + " " + order)
                .findList();
    }

    public static Note findByName(String name) {
        List<Note> items = find.where()
                .eq("name", name)
                .findList();
        if (items.size() == 1) {
            return items.get(0);
        } else if (items.size() > 1) {
            Logger.error("Too many notes named: " + name);
        }
        return null;
    }

    public static List<Note> appList(int tech_id) {
        List<Note> items = find.where().eq("disabled", false).findList();
        List<Note> result = new ArrayList<Note>();
        for (Note item : items) {
            if (item.created_by == 0 || item.created_by == tech_id) {
                result.add(item);
            } else if (Entry.hasEntryForNote(tech_id, item.id)) {
                result.add(item);
            }
        }
        return result;
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

    public String getTypeString() { return type.display; }

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

    public static Map<String,String> options() {
        LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
        for(Type t: Type.values()) {
            options.put(t.toString(), t.display);
        }
        return options;
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
}


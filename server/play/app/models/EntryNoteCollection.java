package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;
import play.Logger;

import com.avaje.ebean.*;

/**
 * Entry note collection entity managed by Ebean
 */
@Entity 
public class EntryNoteCollection extends Model {

    private static final long serialVersionUID = 1L;

	@Id
    public Long id;
    
    @Constraints.Required
    public Long collection_id;

    @Constraints.Required
    public Long note_id;

    @Constraints.Required
    public String note_value;

    public static Finder<Long,EntryNoteCollection> find = new Finder<Long,EntryNoteCollection>(EntryNoteCollection.class);

    public String getName() {
        Note note = Note.find.byId(note_id);
        if (note == null) {
            return "INVALID ID " + note_id;
        }
        return note.name;
    }

    public static List<EntryNoteCollection> findByCollectionId(long collection_id) {
        return find.where()
                .eq("collection_id", collection_id)
                .findList();
    }

    public static int countNotes(long note_id) {
        return find.where().eq("note_id", note_id).findList().size();
    }

    public static void deleteByCollectionId(long collection_id) {
        List<EntryNoteCollection> list = find.where()
                .eq("collection_id", collection_id)
                .findList();
        for (EntryNoteCollection item : list) {
            item.delete();
        }
    }

    public static void removeUnused() {
        List<EntryNoteCollection> items = find.where().findList();
        ArrayList<EntryNoteCollection> unused = new ArrayList<EntryNoteCollection>();
        for (EntryNoteCollection item : items) {
            if (!Entry.hasEntryForNoteCollectionId(item.collection_id)) {
                unused.add(item);
            }
        }
        for (EntryNoteCollection item : unused) {
            Logger.debug("MYDEBUG: UNUSED NOTE: " + item.id + ", " + item.collection_id + ", " + item.note_id);
        }
    }
}


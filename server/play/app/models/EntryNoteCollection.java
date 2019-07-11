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

    @Constraints.Required
    public Long picture_collection_id; // NOTE: This references PictureCollection.id NOT PictureCollection.collection_id

    public static Finder<Long,EntryNoteCollection> find = new Finder<Long,EntryNoteCollection>(EntryNoteCollection.class);

    public String getName() {
        if (note_id == null) {
            return "BAD ID";
        }
        Note note = Note.find.byId(note_id);
        if (note == null) {
            return "INVALID ID " + note_id;
        }
        return note.name;
    }

    public String getValue() {
        return note_value;
    }

    public void setValue(String value) {
        note_value = value;
        update();
    }

    public static List<EntryNoteCollection> findByCollectionId(long collection_id) {
        return find.where()
                .eq("collection_id", collection_id)
                .findList();
    }

    public static List<EntryNoteCollection> findByNoteId(long note_id) {
        return find.where()
                .eq("note_id", note_id)
                .findList();
    }

    public static List<Note> findNotes(long collection_id) {
        List<EntryNoteCollection> items = find.where()
                .eq("collection_id", collection_id)
                .findList();
        List<Note> list = new ArrayList<>();
        for (EntryNoteCollection item : items) {
            Note note = Note.find.byId(item.note_id);
            if (note == null) {
                Logger.error("Could not locate note ID " + item.note_id);
            } else {
                list.add(note);
            }
        }
        return list;
    }

    public static int countNotes(long note_id) {
        return find.where().eq("note_id", note_id).findRowCount();
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
        if (unused.size() > 0) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("Unused note-entries were removed. To recover execute the following SQL commands:\n");
            for (EntryNoteCollection item : unused) {
                sbuf.append("INSERT INTO `entry_note_collection` VALUES(" + item.id + "," + item.collection_id + "," + item.note_id + ",'" + item.note_value + "');\n");
                item.delete();
            }
            Logger.info(sbuf.toString());
        }
    }

    public static void replace(long collection_id, List<Note> notes) {
        deleteByCollectionId(collection_id);
        for (Note note : notes) {
            EntryNoteCollection item = new EntryNoteCollection();
            item.collection_id = collection_id;
            item.note_id = note.id;
            item.save();
        }
    }
}


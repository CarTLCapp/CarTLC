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
}

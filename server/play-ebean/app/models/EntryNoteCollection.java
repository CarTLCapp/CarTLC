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
}


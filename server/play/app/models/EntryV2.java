package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import com.avaje.ebean.*;
import modules.AmazonHelper;
import play.Logger;

/**
 * Entry entity managed by Ebean
 */
@Entity 
public class EntryV2 extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public int tech_id;

    @Formats.DateTime(pattern="yyyy-MM-dd kk:mm")
    public Date entry_time;

    @Constraints.Required
    public long project_id;

    @Constraints.Required
    public long address_id;

    @Constraints.Required
    public long equipment_collection_id;

    @Constraints.Required
    public long picture_collection_id;

    @Constraints.Required
    public long note_collection_id;

    @Constraints.Required
    private int truck_number;

    @Constraints.Required
    private String license_plate;

    public static Finder<Long,EntryV2> find = new Finder<Long,EntryV2>(EntryV2.class);

    // TODO: Once data has been transfered, this code can be removed
    // and the database can be cleaned up by removing this table.
    public static void transfer() {
        List<EntryV2> list = find.findList();
        for (EntryV2 entry2 : list) {
            Entry entry = new Entry();
            entry.tech_id = entry2.tech_id;
            entry.entry_time = entry2.entry_time;
            entry.project_id = entry2.project_id;
            entry.company_id = entry2.address_id;
            entry.equipment_collection_id = entry2.equipment_collection_id;
            entry.picture_collection_id = entry2.picture_collection_id;
            entry.note_collection_id = entry2.note_collection_id;
            if (entry2.truck_number != 0 || entry2.license_plate != null) {
                Truck truck = Truck.add(entry2.project_id, entry2.address_id, entry2.truck_number, entry2.license_plate, entry.tech_id);
                entry.truck_id = truck.id;
            }
            entry.save();
            entry2.delete();
        }
    }
}


package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;
import play.Logger;

import com.avaje.ebean.*;

/**
 * Entry equipment collection entity managed by Ebean
 */
@Entity 
public class EntryEquipmentCollection extends Model {

    private static final long serialVersionUID = 1L;

	@Id
    public Long id;
    
    @Constraints.Required
    public Long collection_id;

    @Constraints.Required
    public Long equipment_id;

    public static Finder<Long,EntryEquipmentCollection> find = new Finder<Long,EntryEquipmentCollection>(EntryEquipmentCollection.class);

    public static List<EntryEquipmentCollection> list() { return find.all(); }

    public static List<Equipment> findEquipments(long collection_id) {
        List<EntryEquipmentCollection> items = find.where()
                .eq("collection_id", collection_id)
                .findList();
        List<Equipment> list = new ArrayList<Equipment>();
        for (EntryEquipmentCollection item : items) {
            Equipment equipment = Equipment.find.byId(item.equipment_id);
            if (equipment == null) {
                Logger.error("Could not locate equipment ID " + item.equipment_id);
            } else {
                list.add(equipment);
            }
        }
        return list;
    }

    public static int countEquipments(long equipment_id) {
        return find.where().eq("equipment_id", equipment_id).findList().size();
    }

    public static boolean has(EntryEquipmentCollection collection) {
        List<EntryEquipmentCollection> items =
                find.where()
                        .eq("collection_id", collection.collection_id)
                        .eq("equipment_id", collection.equipment_id)
                        .findList();
        return items.size() > 0;
    }

    public static void deleteByCollectionId(long collection_id) {
        List<EntryEquipmentCollection> items = find.where()
                .eq("collection_id", collection_id)
                .findList();
        for (EntryEquipmentCollection item : items) {
            item.delete();
        }
    }

    public static void removeUnused() {
        List<EntryEquipmentCollection> items = list();
        ArrayList<EntryEquipmentCollection> unused = new ArrayList<EntryEquipmentCollection>();
        for (EntryEquipmentCollection item : items) {
            if (!Entry.hasEntryForEquipmentCollectionId(item.collection_id)) {
                unused.add(item);
            }
        }
        if (unused.size() > 0) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("Unused entry-equipment items were removed. To recover execute the following SQL commands:\n");
            for (EntryEquipmentCollection item : unused) {
                sbuf.append("INSERT INTO `entry_equipment_collection` VALUES(" + item.id + "," + item.collection_id + "," + item.equipment_id + ");\n");
                item.delete();
            }
            Logger.info(sbuf.toString());
        }
    }
}


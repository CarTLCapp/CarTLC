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
public class Entry extends com.avaje.ebean.Model {

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

    @Constraints.Required
    public long truck_id;

    public static Finder<Long,Entry> find = new Finder<Long,Entry>(Entry.class);

    public static PagedList<Entry> list(int page, int pageSize, String sortBy, String order) {
        return find.where()
                .orderBy(sortBy + " " + order)
                .findPagedList(page, pageSize);
    }

    // TODO: Once data has been transfered, this code can be removed
    // and the database can be cleaned up by removing the truck_number and license_plate columns.
    public static void convertTruckEntries() {
        List<Entry> list;
        list = find.where().ne("truck_number", 0).findList();
        if (list.size() > 0) {
            transferTruckValues(list);
        }
        list = find.where().ne("license_plate", null).findList();
        if (list.size() > 0) {
            transferTruckValues(list);
        }
    }

    static void transferTruckValues(List<Entry> list) {
        Truck truck;
        for (Entry entry : list) {
            if (entry.truck_number == 0 || entry.license_plate == null) {
                Logger.error("Invalid: entry has already been transfer. Should not have arrived here.");
                continue;
            }
            truck = Truck.add(entry.truck_number, entry.license_plate);
            entry.license_plate = null;
            entry.truck_number = 0;
            entry.truck_id = truck.id;
            entry.update();
        }
    }

    public String getTechName() {
        Technician tech = Technician.find.ref((long) tech_id);
        if (tech == null) {
            return "NOT FOUND: " + tech_id;
        }
        return tech.fullName();
    }

    public String getProjectLine() {
        Project project = Project.find.ref(project_id);
        if (project == null) {
            return "NOT FOUND: " + project_id;
        }
        return project.name;
    }

    public String getAddressLine() {
        Company company = Company.get(address_id);
        if (company == null) {
            return "NOT FOUND: " + address_id;
        }
        return company.getLine();
    }

    public String getCompany() {
        Company company = Company.get(address_id);
        if (company == null) {
            return "NOT FOUND: " + address_id;
        }
        return company.name;
    }

    public String getStreet() {
        Company company = Company.get(address_id);
        if (company == null) {
            return "NOT FOUND: " + address_id;
        }
        return company.street;
    }

    public String getState() {
        Company company = Company.get(address_id);
        if (company == null) {
            return "NOT FOUND: " + address_id;
        }
        return company.state;
    }

    public String getCity() {
        Company company = Company.get(address_id);
        if (company == null) {
            return "NOT FOUND: " + address_id;
        }
        return company.city;
    }

    public String getZipCode() {
        Company company = Company.get(address_id);
        if (company == null) {
            return "NOT FOUND: " + address_id;
        }
        return company.zipcode;
    }

    public String getTruckLine() {
        Truck truck = Truck.find.ref(truck_id);
        if (truck == null) {
            return null;
        }
        if (truck.license_plate != null) {
            return truck.license_plate;
        }
        return Integer.toString(truck.truck_number);
    }

    public String getEquipmentLine() {
        List<Equipment> equipments = EntryEquipmentCollection.findEquipments(equipment_collection_id);
        StringBuilder sbuf = new StringBuilder();
        for (Equipment equipment : equipments) {
            if (sbuf.length() > 0) {
                sbuf.append(", ");
            }
            sbuf.append(equipment.name);
        }
        return sbuf.toString();
    }

    public List<PictureCollection> getPictures() {
        return PictureCollection.findByCollectionId(picture_collection_id);
    }

    public List<EntryNoteCollection> getNotes() {
        return EntryNoteCollection.findByCollectionId(note_collection_id);
    }

    static List<Entry> findByProjectId(long project_id) {
        return find.where().eq("project_id", project_id).findList();
    }

    public static boolean hasEquipment(long project_id, long equipment_id) {
        for (Entry entry : findByProjectId(project_id)) {
            if (entry.hasEquipment(equipment_id)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEquipment(long equipment_id) {
        List<Equipment> items = EntryEquipmentCollection.findEquipments(equipment_collection_id);
        for (Equipment item : items) {
            if (item.id == equipment_id) {
                return true;
            }
        }
        return false;
    }

    public void remove(AmazonHelper amazonHelper) {
        EntryEquipmentCollection.deleteEntries(equipment_collection_id);
        PictureCollection.deleteByCollectionId(picture_collection_id, amazonHelper);
        EntryNoteCollection.deleteByCollectionId(note_collection_id);
        delete();
    }

   public static int countEntriesForProject(long project_id) {
        return find.where().eq("project_id", project_id).findList().size();
    }

    public static int countEntriesForCompany(long company_id) {
        return find.where().eq("address_id", company_id).findList().size();
    }

    public static boolean hasEntryForProject(long project_id) {
        return countEntriesForProject(project_id) > 0;
    }

    public static boolean hasEntryForCompany(final int tech_id, final long address_id) {
        List<Entry> items = find.where()
                    .eq("tech_id", tech_id)
                    .eq("address_id", address_id).findList();
        return items.size() > 0;
    }

    public static boolean hasEntryForCompany(final long address_id) {
        List<Entry> items = find.where()
                .eq("address_id", address_id).findList();
        return items.size() > 0;
    }

    public static boolean hasEntryForEquipment(final int tech_id, final long equipment_id) {
        List<Entry> items = find.where()
                .eq("tech_id", tech_id)
                .findList();
        for (Entry entry : items) {
            List<Equipment> collection = EntryEquipmentCollection.findEquipments(entry.equipment_collection_id);
            for (Equipment equip : collection) {
                if (equip.id == equipment_id) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasEntryForNote(final int tech_id, final long note_id) {
        List<Entry> items = find.where()
                .eq("tech_id", tech_id)
                .findList();
        for (Entry entry : items) {
            List<EntryNoteCollection> collection = EntryNoteCollection.findByCollectionId(entry.note_collection_id);
            for (EntryNoteCollection note : collection) {
                if (note.id == note_id) {
                    return true;
                }
            }
        }
        return false;
    }
}


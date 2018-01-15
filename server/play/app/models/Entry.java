package models;

import java.util.*;
import java.text.SimpleDateFormat;
import java.io.File;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import com.avaje.ebean.*;

import modules.AmazonHelper;
import modules.AmazonHelper.OnDownloadComplete;
import play.Logger;

/**
 * Entry entity managed by Ebean
 */
@Entity
public class Entry extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    public enum Status {
        // Warning: this string version must match the APP side.
        COMPLETE("Complete"),
        PARTIAL("Partial Install"),
        NEEDS_REPAIR("Needs Repair"),
        UNKNOWN("Unknown");

        final String name;

        Status(String name) {
            this.name = name;
        }

        public static Status from(int ord) {
            for (Status value : values()) {
                if (value.ordinal() == ord) {
                    return value;
                }
            }
            return Status.UNKNOWN;
        }

        public static Status from(String match) {
            if (match == null) {
                return Status.UNKNOWN;
            }
            for (Status value : values()) {
                if (value.toString().compareToIgnoreCase(match) == 0) {
                    return value;
                }
            }
            return Status.UNKNOWN;
        }

        public String getCellColor() {
            if (this == COMPLETE) {
                return "#00ff00";
            } else if (this == PARTIAL) {
                return "#ff6b4b";
            } else if (this == NEEDS_REPAIR) {
                return "#ff01ff";
            }
            return "";
        }

        public String getName() {
            return name;
        }
    }

    @Id
    public Long id;

    @Constraints.Required
    public int tech_id;

    @Formats.DateTime(pattern = "yyyy-MM-dd kk:mm zzz")
    public Date entry_time;

    @Constraints.Required
    public long project_id;

    @Constraints.Required
    public long company_id;

    @Constraints.Required
    public long equipment_collection_id;

    @Constraints.Required
    public long picture_collection_id;

    @Constraints.Required
    public long note_collection_id;

    @Constraints.Required
    public long truck_id;

    @Constraints.Required
    public Status status;

    public static Finder<Long, Entry> find = new Finder<Long, Entry>(Entry.class);

    public static PagedList<Entry> list(int page, int pageSize, String sortBy, String order) {
        return find.where()
                .orderBy(sortBy + " " + order)
                .findPagedList(page, pageSize);
    }

    public boolean match(List<String> terms) {
        for (String term : terms) {
            if (!match(term)) {
                return false;
            }
        }
        return true;
    }

    public boolean match(String search) {
        if (getProjectLine().contains(search)) {
            return true;
        }
        if (getCity().contains(search)) {
            return true;
        }
        if (getCompany().contains(search)) {
            return true;
        }
        if (getEquipmentLine().contains(search)) {
            return true;
        }
        if (getTruckLine().contains(search)) {
            return true;
        }
        if (getZipCode().contains(search)) {
            return true;
        }
        if (getTechName().contains(search)) {
            return true;
        }
        if (getAddressLine().contains(search)) {
            return true;
        }
        if (getState().contains(search)) {
            return true;
        }
        if (getStatus().contains(search)) {
            return true;
        }
        return false;
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
        if (project.name == null) {
            return "";
        }
        return project.name;
    }

    public String getAddressLine() {
        Company company = Company.get(company_id);
        if (company == null) {
            return "NOT FOUND: " + company_id;
        }
        return company.getLine();
    }

    public String getCompany() {
        Company company = Company.get(company_id);
        if (company == null) {
            return "NOT FOUND: " + company_id;
        }
        if (company.getName() == null) {
            return "";
        }
        return company.getName();
    }

    public String getStreet() {
        Company company = Company.get(company_id);
        if (company == null) {
            return "NOT FOUND: " + company_id;
        }
        if (company.street == null) {
            return "";
        }
        return company.street;
    }

    public String getState() {
        Company company = Company.get(company_id);
        if (company == null) {
            return "NOT FOUND: " + company_id;
        }
        if (company.state == null) {
            return "";
        }
        return company.state;
    }

    public String getCity() {
        Company company = Company.get(company_id);
        if (company == null) {
            return "NOT FOUND: " + company_id;
        }
        if (company.city == null) {
            return "";
        }
        return company.city;
    }

    public String getZipCode() {
        Company company = Company.get(company_id);
        if (company == null) {
            return "NOT FOUND: " + company_id;
        }
        if (company.zipcode == null) {
            return "";
        }
        return company.zipcode;
    }

    public String getStatus() {
        if (status == null) {
            return "";
        }
        return status.getName();
    }

    public String getCellColor() {
        if (status == null) {
            return "";
        }
        return status.getCellColor();
    }

    public String getDate() {
        return new SimpleDateFormat("yyyy-MM-dd kk:mm zzz").format(entry_time);
    }

    public String getTruckLine() {
        Truck truck = Truck.find.ref(truck_id);
        if (truck == null) {
            return null;
        }
        StringBuilder sbuf = new StringBuilder();
        if (truck.truck_number != null && !truck.truck_number.isEmpty()) {
            sbuf.append(truck.truck_number);
        }
        if (truck.license_plate != null && !truck.license_plate.isEmpty()) {
            if (sbuf.length() > 0) {
                sbuf.append(" : ");
            }
            sbuf.append(truck.license_plate);
        }
        return sbuf.toString();
    }

    public int truckCompareTo(Entry other) {
        Truck truck = Truck.find.ref(truck_id);
        Truck otruck = Truck.find.ref(other.truck_id);
        if (truck == null || otruck == null) {
            return 0;
        }
        if (truck.truck_number != null) {
            return truck.truck_number.compareTo(otruck.truck_number);
        }
        if (truck.license_plate != null) {
            return truck.license_plate.compareTo(otruck.license_plate);
        }
        if (otruck.license_plate == null) {
            return 0;
        }
        return -1;
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

    public String getPictureAddendum() {
        List<PictureCollection> pictures = getPictures();
        int num_pictures = pictures.size();
        int num_notes = 0;
        for (PictureCollection c : pictures) {
            if (c.hasNote()) {
                num_notes++;
            }
        }
        StringBuilder sbuf = new StringBuilder();
        if (num_pictures > 0) {
            sbuf.append("P#");
            sbuf.append(num_pictures);
            if (num_notes > 0) {
                sbuf.append(" N#");
                sbuf.append(num_notes);
            }
        }
        return sbuf.toString();
    }

    public String getNoteAddendum() {
        int num_notes = getNotes().size();
        StringBuilder sbuf = new StringBuilder();
        if (num_notes > 0) {
            sbuf.append("N#");
            sbuf.append(num_notes);
        }
        return sbuf.toString();
    }

    public boolean hasPictures() {
        return getPictures().size() > 0;
    }

    public boolean hasNotes() {
        return getNotes().size() > 0;
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

    public static Entry findByDate(int tech_id, Date date) {
        List<Entry> list = find.where()
                .eq("tech_id", tech_id)
                .eq("entry_time", date)
                .findList();
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
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
        EntryEquipmentCollection.deleteByCollectionId(equipment_collection_id);
        PictureCollection.deleteByCollectionId(picture_collection_id, amazonHelper);
        EntryNoteCollection.deleteByCollectionId(note_collection_id);
        delete();
    }

    public static int countEntriesForProject(long project_id) {
        return find.where().eq("project_id", project_id).findList().size();
    }

    public static int countEntriesForCompany(long company_id) {
        return find.where().eq("company_id", company_id).findList().size();
    }

    public static int countEntriesForTechnician(long tech_id) {
        return find.where().eq("tech_id", tech_id).findList().size();
    }

    public static int countEntriesForTruck(long truck_id) {
        return find.where().eq("truck_id", truck_id).findList().size();
    }

    public static int countEntriesForNote(long note_id) {
        return EntryNoteCollection.countNotes(note_id);
    }

    public static int countEntriesForEquipment(long equipment_id) {
        return EntryEquipmentCollection.countEquipments(equipment_id);
    }

    public static boolean hasEntryForProject(long project_id) {
        return countEntriesForProject(project_id) > 0;
    }

    public static boolean hasEntryForCompany(final int tech_id, final long company_id) {
        List<Entry> items = find.where()
                .eq("tech_id", tech_id)
                .eq("company_id", company_id).findList();
        return items.size() > 0;
    }

    public static boolean hasEntryForCompany(final long company_id) {
        List<Entry> items = find.where()
                .eq("company_id", company_id).findList();
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

    public static boolean hasEntryForEquipment(final long equipment_id) {
        List<Entry> items = find.where().findList();
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

    public static boolean hasEntryForEquipmentCollectionId(final long collection_id) {
        return find.where().eq("equipment_collection_id", collection_id).findList().size() > 0;
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

    public static boolean hasEntryForNote(final long note_id) {
        List<Entry> items = find.where().findList();
        for (Entry entry : items) {
            List<EntryNoteCollection> collection = EntryNoteCollection.findByCollectionId(entry.note_collection_id);
            for (EntryNoteCollection note : collection) {
                if (note.note_id == note_id) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasEntryForNoteCollectionId(final long collection_id) {
        return find.where().eq("note_collection_id", collection_id).findList().size() > 0;
    }

    public static boolean hasEntryForTruck(final long truck_id) {
        List<Entry> items = find.where()
                .eq("truck_id", truck_id).findList();
        return items.size() > 0;
    }

    public static Entry getFulfilledBy(WorkOrder order) {
        List<Entry> list = find.where()
                .eq("company_id", order.company_id)
                .eq("project_id", order.project_id)
                .eq("truck_id", order.truck_id)
                .findList();
        if (list.size() == 0) {
            return null;
        }
        if (list.size() > 1) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("More than one entry found to fulfill workorder");
            for (Entry entry : list) {
                sbuf.append("\n");
                sbuf.append(entry.toString());
            }
            Logger.error(sbuf.toString());
        }
        return list.get(0);
    }

    public void loadPictures(String host, AmazonHelper amazonHelper) {
        List<PictureCollection> pictures = getPictures();
        for (PictureCollection picture : pictures) {
            File localFile = amazonHelper.getLocalFile(picture.picture);
            if (!localFile.exists()) {
                try {
                    amazonHelper.download(host, picture.picture, new OnDownloadComplete() {
                        public void onDownloadComplete(File file) {
                            Logger.info("COMPLETED: " + file.getAbsolutePath());
                        }
                    });
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                }
            }
        }
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(id);
        sbuf.append(":");
        sbuf.append(getDate());
        sbuf.append(",");
        sbuf.append(getAddressLine());
        sbuf.append(",");
        sbuf.append(getTruckLine());
        sbuf.append(",");
        sbuf.append(getTechName());
        return sbuf.toString();
    }

}


/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.lang.Math;

import javax.persistence.*;

import play.data.validation.*;
import play.data.format.*;
import play.data.Form;
import play.db.ebean.Transactional;

import com.avaje.ebean.*;
import play.db.ebean.*;

import modules.AmazonHelper;
import modules.AmazonHelper.OnDownloadComplete;
import modules.TimeHelper;
import modules.Status;
import models.flow.*;
import play.Logger;

/**
 * Entry entity managed by Ebean
 */
@Entity
public class Entry extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;
    private static final String DATE_FORMAT = "yyyy-MM-dd kk:mm zzz";
    private static final Boolean VERBOSE = EntryRecovery.VERBOSE;
    private static final long IS_THE_SAME_WINDOW_MS = TimeUnit.DAYS.toMillis(30 * 6);

    @Id
    public Long id;

    @Constraints.Required
    public int tech_id;

    @Formats.DateTime(pattern = DATE_FORMAT)
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
    public long truck_id; // If unused, then it is expected to have a picture with an associated note id'ing the truck

    @Constraints.Required
    public Status status;

    @Constraints.Required
    public String time_zone;

    public static Finder<Long, Entry> find = new Finder<Long, Entry>(Entry.class);

    public static Entry get(long id) {
        return find.byId(id);
    }

    public static PagedList<Entry> list(int page, int pageSize, String sortBy, String order) {
        return find.where()
                .orderBy(sortBy + " " + order)
                .findPagedList(page, pageSize);
    }

    public boolean match(List<String> terms, long client_id) {
        for (String term : terms) {
            if (!match(term, client_id)) {
                return false;
            }
        }
        return true;
    }

    public boolean match(String search, long client_id) {
        if (getRootProjectName().toLowerCase().contains(search)) {
            return true;
        }
        if (getSubProjectName().toLowerCase().contains(search)) {
            return true;
        }
        if (getCity().toLowerCase().contains(search)) {
            return true;
        }
        if (getCompany().toLowerCase().contains(search)) {
            return true;
        }
        if (getEquipmentLine(client_id).toLowerCase().contains(search)) {
            return true;
        }
        if (client_id == 0 || ClientAssociation.hasShowTrucks(client_id)) {
            if (getTruckLine().toLowerCase().contains(search)) {
                return true;
            }
        }
        if (getZipCode().toLowerCase().contains(search)) {
            return true;
        }
        if (getTechName().toLowerCase().contains(search)) {
            return true;
        }
        if (getAddressLine().toLowerCase().contains(search)) {
            return true;
        }
        if (getState().toLowerCase().contains(search)) {
            return true;
        }
        if (getStatus().toLowerCase().contains(search)) {
            return true;
        }
        return false;
    }

    public String getTechName() {
        Technician tech = null;
        try {
            tech = Technician.find.byId((long) tech_id);
        } catch (Exception ex) {
        }
        if (tech == null) {
            return Technician.RIP;
        }
        Technician tech2 = SecondaryTechnician.findSecondaryTechByEntryId(id);
        if (tech2 == null) {
            return tech.fullName();
        }
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(tech.fullName());
        sbuf.append(" & ");
        try {
            sbuf.append(tech2.fullName());
        } catch (Exception ex) {
            sbuf.append(Technician.RIP);
        }
        return sbuf.toString();
    }

    public String getSubProjectName() {
        Project project = Project.find.byId(project_id);
        if (project == null) {
            return "NOT FOUND: " + project_id;
        }
        if (project.name == null) {
            return "";
        }
        return project.name;
    }

    public String getRootProjectName() {
        Project project = Project.find.byId(project_id);
        if (project == null) {
            return "NOT FOUND: " + project_id;
        }
        return project.getRootProjectName();
    }

    public Flow getFlow() {
        return Flow.getByProjectId(project_id);
    }

    public List<FlowElement> getFlowElements() {
        Flow flow = getFlow();
        if (flow != null) {
            List<FlowElement> list = flow.getFlowElements();
            Collections.sort(list);
            return list;
        }
        return new ArrayList<FlowElement>();
    }

    public List<FlowElement> getPictureFlowElements() {
        ArrayList<FlowElement> result = new ArrayList<FlowElement>();
        FlowElement dialogElement = null;
        for (FlowElement element : getFlowElements()) {
            if (element.isDialog()) {
                dialogElement = element;
            } else if (element.isConfirm()) {
                if (dialogElement != null) {
                    result.add(dialogElement);
                    dialogElement = null;
                }
                result.add(element);
            } else if (element.getNumImages() > 0) {
                dialogElement = null;
                result.add(element);
            } else {
                dialogElement = null;
            }
        }
        return result;
    }

    public ArrayList<EntryNoteCollection> getNoteValuesForElement(long client_id, long element_id) {
        ArrayList<EntryNoteCollection> notes = new ArrayList<EntryNoteCollection>();
        for (EntryNoteCollection note : getNotes(client_id)) {
            if (FlowNoteCollection.hasNote(element_id, note.note_id)) {
                notes.add(note);
            }
        }
        return notes;
    }

    public boolean hasTruckNumberPictureValue() {
        return getTruckNumberPictureValue() != null;
    }

    public String getTruckNumberPictureValue() {
        List<PictureCollection> list = PictureCollection.locate(picture_collection_id, PictureCollection.FLOW_TRUCK_NUMBER_ID);
        if (list.size() > 1) {
            error("More than one truck number picture found. Need a fixup for entry: " + toString());
        }
        if (list.size() > 0) {
            return list.get(list.size() - 1).picture;
        }
        return null;
    }

    public boolean hasTruckDamagePictureValue() {
        return getTruckDamagePictureValue() != null;
    }

    public String getTruckDamagePictureValue() {
        List<PictureCollection> list = PictureCollection.locate(picture_collection_id, PictureCollection.FLOW_TRUCK_DAMAGE_ID);
        if (list.size() > 1) {
            error("More than one truck damage picture found. Need a fixup for entry: " + toString());
        }
        if (list.size() > 0) {
            return list.get(list.size() - 1).picture;
        }
        return null;
    }

    public boolean isFlowEntry() {
        if (getFlow() == null) {
            return false;
        }
        List<PictureCollection> pictures = getPictures();
        if (pictures.size() > 0) {
            for (PictureCollection collection : pictures) {
                if (collection.flow_element_id != null && collection.flow_element_id > 0) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public String getStreetAddress() {
        Company company = Company.get(company_id);
        if (company == null) {
            return "NOT FOUND: " + company_id;
        }
        return company.getStreetAddress();
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

    public String getStatus2() {
        if (wasRepaired()) {
            return "Repaired";
        }
        return getStatus();
    }

    public String getCellColor2() {
        if (wasRepaired()) {
            return "#ff0167";
        }
        return getCellColor();
    }

    public boolean wasRepaired() {
        return Repaired.wasRepaired(id);
    }

    public List<Entry> getRelatedEntries() {
        List<Repaired> meList = Repaired.findByEntryId(id);
        ArrayList<Integer> instanceIds = new ArrayList<Integer>();
        for (Repaired repaired : meList) {
            int instanceId = repaired.instance_id;
            if (!instanceIds.contains(instanceId)) {
                instanceIds.add(instanceId);
            }
        }
        ArrayList<Entry> relatedEntries = new ArrayList<Entry>();
        for (int instanceId : instanceIds) {
            List<Repaired> items = Repaired.findByInstanceId(instanceId);
            for (Repaired item : items) {
                if (!item.entry_id.equals(id)) {
                    Entry related = Entry.get(item.entry_id);
                    if (related != null) {
                        relatedEntries.add(related);
                    }
                }
            }
        }
        return relatedEntries;
    }

    public String getDate() {
        return new TimeHelper().getDate(entry_time);
    }

    public String getTime() {
        return new TimeHelper().getTime(entry_time, time_zone);
    }

    public String getDateTime() {
        return new TimeHelper().getDateTime(entry_time, time_zone);
    }

    public Truck getTruck() {
        return Truck.find.byId(truck_id);
    }

    public String getTruckLine() {
        Truck truck = Truck.find.byId(truck_id);
        if (truck == null) {
            return null;
        }
        return truck.getID();
    }

    public int truckCompareTo(Entry other) {
        Truck truck = Truck.find.byId(truck_id);
        Truck otruck = Truck.find.byId(other.truck_id);
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

    public String getEquipmentLine(Client client) {
        if (client.id != null) {
            return getEquipmentLine(client.id);
        } else {
            return "";
        }
    }

    public String getEquipmentLine(long client_id) {
        boolean is_admin = Client.isAdmin(client_id);
        List<Equipment> equipments = EntryEquipmentCollection.findEquipments(equipment_collection_id);
        StringBuilder sbuf = new StringBuilder();
        for (Equipment equipment : equipments) {
            if (client_id == 0 || is_admin || ClientEquipmentAssociation.hasEquipment(client_id, equipment.id)) {
                if (sbuf.length() > 0) {
                    sbuf.append(", ");
                }
                sbuf.append(equipment.name);
            }
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

    public String getNoteAddendum(Client client) {
        if (client.id != null) {
            return getNoteAddendum(client.id);
        }
        return "";
    }

    public String getNoteAddendum(long client_id) {
        int num_notes = getNotes(client_id).size();
        StringBuilder sbuf = new StringBuilder();
        if (num_notes > 0) {
            sbuf.append("N#");
            sbuf.append(num_notes);
        }
        return sbuf.toString();
    }

    public List<EntryNoteCollection> getNotes(long client_id) {
        if (Client.canViewAllNotes(client_id)) {
            return EntryNoteCollection.findByCollectionId(note_collection_id);
        } else {
            ArrayList<EntryNoteCollection> notes = new ArrayList<EntryNoteCollection>();
            for (EntryNoteCollection item : EntryNoteCollection.findByCollectionId(note_collection_id)) {
                if (ClientNoteAssociation.hasNote(client_id, item.note_id)) {
                    notes.add(item);
                }
            }
            return notes;
        }
    }

    public HashMap<String, Object> getNoteValues(long client_id) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (EntryNoteCollection note : getNotes(client_id)) {
            map.put(note.idValueString(), note.getValue());
        }
        return map;
    }

    public HashMap<String, String> getNoteValues2(long client_id) {
        HashMap<String, String> map = new HashMap<String, String>();
        for (EntryNoteCollection note : getNotes(client_id)) {
            map.put(note.idValueString(), note.getValue());
        }
        return map;
    }

    static public List<EntryNoteCollection> getNotesForId(long client_id, long id) {
        Entry entry = find.byId(id);
        if (entry == null) {
            return new ArrayList<EntryNoteCollection>();
        }
        return entry.getNotes(client_id);
    }

    public void applyToNotes(long client_id, Form entryForm) {
        for (EntryNoteCollection note : getNotes(client_id)) {
            String valueName = note.idValueString();
            Optional<String> value = entryForm.field(valueName).getValue();
            if (value.isPresent()) {
                note.setValue(value.get());
            }
        }
    }

    public boolean hasPictures() {
        return getPictures().size() > 0;
    }

    public boolean hasNotes(Client client) {
        return client.id != null && hasNotes(client.id);
    }

    public boolean hasNotes(long client_id) {
        return getNotes(client_id).size() > 0;
    }

    public List<PictureCollection> getPictures() {
        return PictureCollection.findByCollectionId(picture_collection_id);
    }

    public List<PictureCollection> getFlowPictures(long element_id) {
        List<PictureCollection> items = PictureCollection.locate(picture_collection_id, element_id);
        FlowElement flowElement = FlowElement.get(element_id);
        int expected = flowElement.getNumImages();
        if (expected != items.size()) {
            error("Unexpected number of pictures found for flow element " + flowElement.id + ", EXPECTED=" + expected + " != ACTUAL " + items.size());
        }
        if (items.size() > expected) {
            return items.subList(items.size() - expected, items.size());
        }
        return items;
    }

    public static List<Entry> findAllBefore(Date date) {
        return find.where().lt("entry_time", date).findList();
    }

    public static int countAllBefore(Date date) {
        return find.where().lt("entry_time", date).findRowCount();
    }

    static List<Entry> findByProjectId(long project_id) {
        return find.where().eq("project_id", project_id).findList();
    }

    public static List<Entry> findByTruckId(long truck_id) {
        return find.where().eq("truck_id", truck_id).findList();
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

    public static boolean hasEquipmentByProject(long project_id, long equipment_id) {
        for (Entry entry : findByProjectId(project_id)) {
            if (entry.hasEquipment(equipment_id)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasEquipment(long entry_id, long equipment_id) {
        Entry entry = find.byId(entry_id);
        if (entry != null) {
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

    public static boolean hasNote(long entry_id, long note_id) {
        Entry entry = find.byId(entry_id);
        if (entry != null) {
            if (entry.hasNote(note_id)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasNote(long note_id) {
        List<Note> items = EntryNoteCollection.findNotes(note_collection_id);
        for (Note item : items) {
            if (item.id == note_id) {
                return true;
            }
        }
        return false;
    }

    public void remove(AmazonHelper.DeleteAction amazonAction) {
        /**
         * Also check to ensure collection not used in recovery entries. Shouldn't be, but if it is, leave it.
         */
        if (!EntryRecovery.hasEntryForEquipment(equipment_collection_id)) {
            if (countEntryForEquipment(equipment_collection_id) <= 1) {
                EntryEquipmentCollection.deleteByCollectionId(equipment_collection_id);
            }
        }
        if (!EntryRecovery.hasEntryForPictureCollectionId(picture_collection_id)) {
            if (countEntryForPictureCollectionId(picture_collection_id) <= 1) {
                PictureCollection.deleteByCollectionId(picture_collection_id, amazonAction);
            }
        }
        if (!EntryRecovery.hasEntryForNote(note_collection_id)) {
            if (countEntryForNote(note_collection_id) <= 1) {
                EntryNoteCollection.deleteByCollectionId(note_collection_id);
            }
        }
        Repaired.deleteMatchingEntryId(id);
        delete();
    }

    public static int countEntriesForRootProject(long root_project_id) {
        List<Project> projects = Project.listWithRoot(root_project_id);
        int count = 0;
        for (Project project : projects) {
            count += find.where()
                    .eq("project_id", project.id).findRowCount();
        }
        return count;
    }

    public static int countEntriesForProject(long project_id) {
        return find.where().eq("project_id", project_id).findRowCount();
    }

    public static int countEntriesForProjectWithinRange(long project_id, long start_time, long end_time) {
        return find.where()
                .eq("project_id", project_id)
                .ge("entry_time", new Date(start_time))
                .le("entry_time", new Date(end_time))
                .findRowCount();
    }

    public static List<Entry> findEntiesForProjectWithinRange(long project_id, long start_time, long end_time) {
        return find.where()
                .eq("project_id", project_id)
                .ge("entry_time", new Date(start_time))
                .le("entry_time", new Date(end_time))
                .findList();
    }

    public static int countEntriesForCompanies(List<Company> companies) {
        int count = 0;
        for (Company company : companies) {
            count += find.where().eq("company_id", company.id).findRowCount();
        }
        return count;
    }

    public static int countEntriesForCompany(long company_id) {
        return find.where().eq("company_id", company_id).findRowCount();
    }

    public static int countEntriesForTechnician(long tech_id) {
        return find.where().eq("tech_id", tech_id).findRowCount();
    }

    public static int countEntriesForTruck(long truck_id) {
        return find.where().eq("truck_id", truck_id).findRowCount();
    }

    public static int countEntriesForNote(long note_id) {
        return EntryNoteCollection.countNotes(note_id);
    }

    public static int countEntriesForEquipment(long equipment_id) {
        return EntryEquipmentCollection.countEquipments(equipment_id);
    }

    public static boolean hasEntryForRootProject(long root_project_id) {
        List<Project> projects = Project.listWithRoot(root_project_id);
        for (Project project : projects) {
            if (find.where()
                    .eq("project_id", project.id).findRowCount() > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasEntryForProject(long project_id) {
        return countEntriesForProject(project_id) > 0;
    }

    public static boolean hasEntryForCompany(final int tech_id, final long company_id) {
        return find.where()
                .eq("tech_id", tech_id)
                .eq("company_id", company_id).findRowCount() > 0;
    }

    public static boolean hasEntryForCompany(final long company_id) {
        return find.where()
                .eq("company_id", company_id).findRowCount() > 0;
    }

    public static boolean hasEntryForEquipment(final int tech_id, final long equipment_id) {
        List<EntryEquipmentCollection> collections = EntryEquipmentCollection.findCollectionsFor(equipment_id);
        for (EntryEquipmentCollection collection : collections) {
            if (find.where()
                    .eq("tech_id", tech_id)
                    .eq("equipment_collection_id", collection.collection_id)
                    .findRowCount() > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasEntryForEquipment(final long equipment_id) {
        return countEntryForEquipment(equipment_id) > 0;
    }

    private static int countEntryForEquipment(final long equipment_id) {
        List<EntryEquipmentCollection> collections = EntryEquipmentCollection.findCollectionsFor(equipment_id);
        for (EntryEquipmentCollection collection : collections) {
            return find.where()
                    .eq("equipment_collection_id", collection.collection_id)
                    .findRowCount();
        }
        return 0;
    }

    public static boolean hasEntryForEquipmentCollectionId(final long collection_id) {
        return find.where().eq("equipment_collection_id", collection_id).findRowCount() > 0;
    }

    public static boolean hasEntryForNote(final int tech_id, final long note_id) {
        List<EntryNoteCollection> collections = EntryNoteCollection.findByNoteId(note_id);
        for (EntryNoteCollection collection : collections) {
            if (find.where()
                    .eq("tech_id", tech_id)
                    .eq("note_collection_id", collection.collection_id)
                    .findRowCount() > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasEntryForNote(final long note_id) {
        return countEntryForNote(note_id) > 0;
    }

    public static int countEntryForNote(final long note_id) {
        List<EntryNoteCollection> collections = EntryNoteCollection.findByNoteId(note_id);
        for (EntryNoteCollection collection : collections) {
            return find.where()
                    .eq("note_collection_id", collection.collection_id)
                    .findRowCount();
        }
        return 0;
    }

    public static boolean hasEntryForNoteCollectionId(final long collection_id) {
        return find.where().eq("note_collection_id", collection_id).findRowCount() > 0;
    }

    public static boolean hasEntryForTruck(final long truck_id) {
        return find.where()
                .eq("truck_id", truck_id)
                .findRowCount() > 0;
    }

    /**
     * @return The entries that have the same truck value as the passed in entry and has a near enough time value.
     */
    public static List<Entry> getEntriesFromEntry(Entry entry, long client_id) {

        List<Truck> trucks = Truck.findMatching(entry.getTruck());

        StringBuilder query = new StringBuilder();
        query.append("SELECT e.id, e.tech_id, e.entry_time, e.project_id, e.company_id");
        query.append(", e.equipment_collection_id");
        query.append(", e.picture_collection_id");
        query.append(", e.note_collection_id");
        query.append(", e.truck_id, e.status, e.time_zone");
        query.append(" FROM entry AS e");
        query.append(" WHERE ");
        query.append("e.truck_id IN (");
        boolean first = true;
        for (Truck truck : trucks) {
            if (first) {
                first = false;
            } else {
                query.append(", ");
            }
            query.append(truck.id);
        }
        query.append(")");

        List<SqlRow> rows = Ebean.createSqlQuery(query.toString()).findList();
        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (SqlRow row : rows) {
            entries.add(parseEntry(row));
        }
        ArrayList<Entry> result = new ArrayList<Entry>();
        for (Entry item : entries) {
            if (entry.nearInTime(item)) {
                result.add(item);
            }
        }
        return result;
    }

    public static Entry parseEntry(SqlRow row) {
        Entry entry = new Entry();
        entry.id = row.getLong("id");
        entry.tech_id = row.getInteger("tech_id");
        entry.entry_time = row.getDate("entry_time");
        entry.time_zone = row.getString("time_zone");
        entry.project_id = row.getLong("project_id");
        entry.company_id = row.getLong("company_id");
        entry.equipment_collection_id = row.getLong("equipment_collection_id");
        entry.picture_collection_id = row.getLong("picture_collection_id");
        entry.note_collection_id = row.getLong("note_collection_id");
        entry.truck_id = row.getLong("truck_id");
        if (row.get("status") != null) { // WHY DO I NEED THIS?
            entry.status = Status.from(getInteger(row, "status"));
        }
        return entry;
    }

    private static Integer getInteger(SqlRow row, String column) {
        if (row.get(column) == null) {
            return null;
        }
        return row.getInteger(column);
    }

    private boolean nearInTime(Entry other) {
        long diffTime = Math.abs(other.entry_time.getTime() - entry_time.getTime());
        return diffTime <= IS_THE_SAME_WINDOW_MS;
    }

    /**
     * @return true if there is the passed in entry matches the same same company_id/truck_id as the current entry.
     */
    public boolean isMatching(Entry entry, long client_id) {
        if (company_id == entry.company_id && truck_id == entry.truck_id) {
            if (VERBOSE) {
                warn("isMatching(): TRUE: " + toString() + " matched " + entry.toString());
            }
            return true;
        }
        Truck entryTruck = getTruck();
        if (entryTruck == null) {
            return false;
        }
        Truck itemTruck = entry.getTruck();
        if (itemTruck == null) {
            return false;
        }
        if (itemTruck.isTheSame(entryTruck)) {
            if (nearInTime(entry)) {
                if (VERBOSE) {
                    warn("isMatching() TRUE by truck: " + toString(client_id) + " matched " + entry.toString(client_id));
                }
                return true;
            } else {
                if (VERBOSE) {
                    warn("isMatching() TRUE, but returning false because too distant: " + toString(client_id) + " matched " + entry.toString(client_id));
                }
            }
        }
        return false;
    }

    public boolean fixTruckCompanyName() {
        Truck entryTruck = getTruck();
        if (entryTruck == null) {
            return false;
        }
        if (entryTruck.company_name_id != 0) {
            return false;
        }
        Company company = Company.get(company_id);
        if (company.name == null || company.name.length() == 0) {
            return false;
        }
        entryTruck.setCompanyName(CompanyName.save(company.name));
        return true;
    }

    public static boolean hasEntryForPicture(String filename) {
        List<PictureCollection> pictures = PictureCollection.findByPictureName(filename);
        if (pictures.isEmpty()) {
            return false;
        }
        for (PictureCollection collection : pictures) {
            List<Entry> list = find.where()
                    .eq("picture_collection_id", collection.collection_id)
                    .findList();
            if (!list.isEmpty()) {
                return true;
            }
            // This is very interesting. A collection ID without any entries? Sounds like something to delete.
            warn("ALERT! No entries for picture collection ID: " + collection.collection_id + ", triggered from: " + filename);
        }
        return false;
    }

    public static boolean hasEntryForPictureCollectionId(long picture_collection_id) {
        return countEntryForPictureCollectionId(picture_collection_id) > 0;
    }

    private static int countEntryForPictureCollectionId(long picture_collection_id) {
        return find.where()
                .eq("picture_collection_id", picture_collection_id)
                .findRowCount();
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
            error(sbuf.toString());
        }
        return list.get(0);
    }

    public boolean loadPictures(String host, AmazonHelper amazonHelper, OnDownloadComplete listener) {
        List<PictureCollection> pictures = getPictures();
        List<File> files = new ArrayList<File>();
        for (PictureCollection picture : pictures) {
            File localFile = amazonHelper.getLocalFile(picture.picture);
            if (!localFile.exists()) {
                files.add(localFile);
            }
        }
        if (files.size() > 0) {
            amazonHelper.download(host, files, listener);
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Entry(");
        if (id != null) {
            sbuf.append(id);
            sbuf.append(", ");
        }
        sbuf.append(getDate());
        sbuf.append(", tech=");
        sbuf.append(tech_id);
        sbuf.append(", project-id=");
        sbuf.append(project_id);
        sbuf.append(", company_id=");
        sbuf.append(company_id);
        sbuf.append(", truck_id=");
        sbuf.append(truck_id);
        sbuf.append(", note=");
        sbuf.append(note_collection_id);
        sbuf.append(", equip=");
        sbuf.append(equipment_collection_id);
        sbuf.append(", picture-");
        sbuf.append(picture_collection_id);
        sbuf.append(")");
        return sbuf.toString();
    }

    public String toString(long client_id) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Entry(");
        if (id != null) {
            sbuf.append(id);
            sbuf.append(", ");
        }
        sbuf.append("date=");
        sbuf.append(getDateTime());
        sbuf.append(", project_id=");
        sbuf.append(project_id);
        sbuf.append(", tech=");
        sbuf.append(getTechName());
        sbuf.append(", address=");
        sbuf.append(",\"");
        sbuf.append(getAddressLine());
        sbuf.append("\", truck=");
        sbuf.append(getTruckLine());
        sbuf.append(", eq='");
        sbuf.append(getEquipmentLine(client_id));
        sbuf.append("', ");
        sbuf.append(" notes=");
        sbuf.append(note_collection_id);
        if (hasNotes(client_id)) {
            sbuf.append(" TRUE");
        }
        sbuf.append(", eqline=");
        sbuf.append(equipment_collection_id);
        sbuf.append(", pictures=");
        sbuf.append(picture_collection_id);
        sbuf.append(")");
        return sbuf.toString();
    }

    public String getLine(long client_id) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(id);
        sbuf.append(" : ");
        sbuf.append(getDate());
        sbuf.append(" : ");
        sbuf.append(getTechName());
        sbuf.append(" : ");
        String rootProject = getRootProjectName();
        if (rootProject != null) {
            sbuf.append(getRootProjectName());
            sbuf.append("/");
        }
        sbuf.append(getSubProjectName());
        sbuf.append(" : ");
        sbuf.append(getAddressLine());
        sbuf.append(" : TRUCK=");
        sbuf.append(getTruckLine());
        sbuf.append(" : #Pictures=");
        sbuf.append(getPictures().size());
        sbuf.append(" : EQUIP=");
        sbuf.append(getEquipmentLine(client_id));
        return sbuf.toString();
    }

    public static Map<String, String> optionsTech() {
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        for (Technician tech : Technician.listEnabled()) {
            options.put(tech.fullName(), tech.fullName());
        }
        return options;
    }

    public static Map<String, String> optionsRootProject() {
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        for (RootProject proj : RootProject.list()) {
            options.put(proj.name, proj.name);
        }
        options.put("", "");
        return options;
    }

    public static Map<String, String> optionsCompanyName() {
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        for (CompanyName companyName : CompanyName.list()) {
            options.put(companyName.name, companyName.name);
        }
        options.put("", "");
        return options;
    }

    public static Map<String, String> optionsStatus() {
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        for (Status status : Status.values()) {
            if (status != Status.UNKNOWN) {
                options.put(status.name, status.name);
            }
        }
        return options;
    }

    public static Status findStatus(String match) {
        for (Status status : Status.values()) {
            if (status.name.equals(match)) {
                return status;
            }
        }
        return null;
    }

    public static boolean isValidStatus(String match) {
        return findStatus(match) != null;
    }


    // region Logger

    private static void error(String msg) {
        Logger.error(msg);
    }

    private static void warn(String msg) {
        Logger.warn(msg);
    }

    private static void info(String msg) {
        Logger.info(msg);
    }

    private static void debug(String msg) {
        Logger.debug(msg);
    }

    // endregion Logger
}


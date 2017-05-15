package com.cartlc.tracker.data;

import android.content.Context;
import android.text.TextUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by dug on 4/17/17.
 */

public class PrefHelper extends PrefHelperBase {

    static PrefHelper sInstance;

    public static PrefHelper getInstance() {
        return sInstance;
    }

    public static void Init(Context ctx) {
        new PrefHelper(ctx);
    }

    static public final String KEY_PROJECT = "project";
    static public final String KEY_COMPANY = "company";
    static public final String KEY_STREET = "street";
    static public final String KEY_STATE = "state";
    static public final String KEY_CITY = "city";
    static final String KEY_CURRENT_PROJECT_GROUP_ID = "current_project_group_id";
    static final String KEY_FIRST_NAME = "first_name";
    static final String KEY_LAST_NAME = "last_name";
    static final String KEY_TRUCK_NUMBER = "truck_number";
    static final String KEY_NOTES = "notes";
    static final String KEY_LAST_NOTES_ID = "notes_id";
    static final String KEY_EQUIPMENT_COLLECTION_ID = "equipment_collection_id";
    static final String KEY_NEXT_EQUIPMENT_COLLECTION_ID = "next_equipment_collection_id";

    PrefHelper(Context ctx) {
        super(ctx);
        sInstance = this;
    }

    public String getStreet() {
        return getString(KEY_STREET, null);
    }

    public void setStreet(String value) {
        setString(KEY_STREET, value);
    }

    public String getState() {
        return getString(KEY_STATE, null);
    }

    public void setState(String value) {
        setString(KEY_STATE, value);
    }

    public String getCompany() {
        return getString(KEY_COMPANY, null);
    }

    public void setCompany(String value) {
        setString(KEY_COMPANY, value);
    }

    public String getCity() {
        return getString(KEY_CITY, null);
    }

    public void setCity(String value) {
        setString(KEY_CITY, value);
    }

    public String getProject() {
        return getString(KEY_PROJECT, null);
    }

    public Long getProjectId() {
        long projectId = TableProjects.getInstance().query(getProject());
        if (projectId >= 0) {
            return projectId;
        }
        return null;
    }

    public void setProject(String value) {
        setString(KEY_PROJECT, value);
    }

    public long getCurrentProjectGroupId() {
        return getLong(KEY_CURRENT_PROJECT_GROUP_ID, -1L);
    }

    public void setCurrentProjectGroupId(long id) {
        setLong(KEY_CURRENT_PROJECT_GROUP_ID, id);
    }

    public void setFirstName(String name) {
        setString(KEY_FIRST_NAME, name);
    }

    public String getFirstName() {
        return getString(KEY_FIRST_NAME, null);
    }

    public void setLastName(String name) {
        setString(KEY_LAST_NAME, name);
    }

    public String getLastName() {
        return getString(KEY_LAST_NAME, null);
    }

    public long getTruckNumber() {
        return getLong(KEY_TRUCK_NUMBER, 0);
    }

    public void setTruckNumber(long id) {
        setLong(KEY_TRUCK_NUMBER, id);
    }

    public String getNotes() {
        return getString(KEY_NOTES, "");
    }

    public void setNotes(String notes) {
        setString(KEY_NOTES, notes);
    }

    public long getLastNotesId() {
        return getLong(KEY_LAST_NOTES_ID, -1L);
    }

    public void setLastNotesId(long id) {
        setLong(KEY_LAST_NOTES_ID, id);
    }

    public List<String> addState(List<String> list) {
        return addIfNotFound(list, getState());
    }

    public List<String> addCity(List<String> list) {
        return addIfNotFound(list, getCity());
    }

    public List<String> addCompany(List<String> list) {
        return addIfNotFound(list, getCompany());
    }

    public List<String> addIfNotFound(List<String> list, String element) {
        if (element != null && !list.contains(element)) {
            list.add(element);
            Collections.sort(list);
        }
        return list;
    }

    public DataProjectGroup getCurrentProjectGroup() {
        long projectGroupId = getCurrentProjectGroupId();
        return TableProjectGroups.getInstance().query(projectGroupId);
    }

    public void setupInit() {
        DataProjectGroup projectGroup = getCurrentProjectGroup();
        if (projectGroup != null) {
            setProject(projectGroup.getProjectName());
            DataAddress address = projectGroup.getAddress();
            if (address != null) {
                setState(address.state);
                setCity(address.city);
                setCompany(address.company);
                setStreet(address.street);
            }
        }
    }

    public void clearCurProject() {
        setState(null);
        setCity(null);
        setCompany(null);
        setStreet(null);
        setProject(null);
        setCurrentProjectGroupId(-1L);
        setLastNotesId(-1L);
        setNotes(null);
        setTruckNumber(0);
    }

    public void clearLastEntry() {
        setTruckNumber(0);
        setNotes(null);
        setLastNotesId(-1L);
        TableEquipment.getInstance().clearChecked();
    }

    public boolean hasCurProject() {
        long projectGroupId = getCurrentProjectGroupId();
        if (projectGroupId < 0) {
            return false;
        }
        DataProjectGroup projectGroup = TableProjectGroups.getInstance().query(projectGroupId);
        if (projectGroup == null) {
            return false;
        }
        return true;
    }

    public boolean saveNewProjectIfNeeded() {
        String state = getState();
        String street = getStreet();
        String city = getCity();
        String company = getCompany();
        String project = getProject();
        if (TextUtils.isEmpty(project) || TextUtils.isEmpty(state) || TextUtils.isDigitsOnly(street) || TextUtils.isEmpty(city) || TextUtils.isEmpty(company)) {
            return false;
        }
        long addressId = TableAddress.getInstance().queryAddressId(company, street, city, state);
        if (addressId < 0) {
            DataAddress address = new DataAddress(company, street, city, state);
            addressId = TableAddress.getInstance().add(address);
        }
        long projectId = TableProjects.getInstance().query(project);
        if (addressId >= 0 && projectId >= 0) {
            long projectGroupId = TableProjectGroups.getInstance().queryProjectGroupId(projectId, addressId);
            if (projectGroupId < 0) {
                projectGroupId = TableProjectGroups.getInstance().add(new DataProjectGroup(projectId, addressId));
            }
            setCurrentProjectGroupId(projectGroupId);
            return true;
        }
        return false;
    }

    public void setCurrentProjectGroup(DataProjectGroup group) {
        setCurrentProjectGroupId(group.id);
        setProject(group.getProjectName());
        setAddress(group.getAddress());
    }

    void setAddress(DataAddress address) {
        setCompany(address.company);
        setStreet(address.street);
        setCity(address.city);
        setState(address.state);
    }

    public long getNextEquipmentCollectionID() {
        return getLong(KEY_NEXT_EQUIPMENT_COLLECTION_ID, 0L);
    }

    public void incNextEquipmentCollectionID() {
        setLong(KEY_NEXT_EQUIPMENT_COLLECTION_ID, getNextEquipmentCollectionID() + 1);

    }

    public DataEntry createEntry() {
        long projectGroupId = getCurrentProjectGroupId();
        if (projectGroupId < 0) {
            return null;
        }
        DataProjectGroup projectGroup = TableProjectGroups.getInstance().query(projectGroupId);
        if (projectGroup == null) {
            return null;
        }
        DataEntry entry = new DataEntry();
        entry.projectNameId = projectGroup.projectNameId;
        entry.equipmentCollection = new DataEquipmentCollection(getNextEquipmentCollectionID(), entry.projectNameId);
        entry.equipmentCollection.addChecked();
        entry.addressId = projectGroup.addressId;
        entry.truckNumber = getTruckNumber();
        entry.notesId = getLastNotesId();
        entry.date = System.currentTimeMillis();
        return entry;
    }

}

package com.cartlc.trackbattery.data;

import android.content.Context;

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
    static final String KEY_CURRENT_ADDRESS_ID = "current_address_id";
    static final String KEY_CURRENT_PROJECT_GROUP_ID = "current_project_group_id";
    static final String KEY_FIRST_NAME = "first_name";
    static final String KEY_LAST_NAME = "last_name";
    static final String KEY_LAST_TRUCK_ID = "last_truck_id";
    static final String KEY_LAST_EQUIPMENT_COLLECTION_ID = "last_equipment_collection_id";
    static final String KEY_LAST_NOTES_ID = "last_notes_id";
    static final String KEY_LAST_PICTURE_COLLECTION_ID = "last_picture_collection_id";
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

    public void setProject(String value) {
        getString(KEY_PROJECT, value);
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

    public long genNextEquipmentCollectionId() {
        long nextId = getLong(KEY_NEXT_EQUIPMENT_COLLECTION_ID, 0L);
        setLong(KEY_NEXT_EQUIPMENT_COLLECTION_ID, nextId + 1);
        return nextId;
    }

    public void setupInit() {
        long projectGroupId = getCurrentProjectGroupId();
        DataProjectGroup projectGroup = TableProjectGroups.getInstance().query(projectGroupId);
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

    public void setupSaveNew() {
        long addressId = TableAddress.getInstance().queryAddressId(getCompany(), getStreet(), getCity(), getState());
        if (addressId < 0) {
            DataAddress address = new DataAddress(getCompany(), getStreet(), getCity(), getState());
            addressId = TableAddress.getInstance().add(address);
        }
        long projectId = TableProjects.getInstance().query(getProject());
        if (addressId >= 0 && projectId >= 0) {
            long projectGroupId = TableProjectGroups.getInstance().add(new DataProjectGroup(projectId, addressId));
            setCurrentProjectGroupId(projectGroupId);
        }
    }
}

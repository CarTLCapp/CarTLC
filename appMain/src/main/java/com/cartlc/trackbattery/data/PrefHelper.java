package com.cartlc.trackbattery.data;

import android.content.Context;

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

    public String getState() {
        return getString(KEY_STATE, null);
    }

    public String getCompany() {
        return getString(KEY_COMPANY, null);
    }

    public String getCity() {
        return getString(KEY_CITY, null);
    }

    public String getProject() { return getString(KEY_PROJECT, null); }

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
}

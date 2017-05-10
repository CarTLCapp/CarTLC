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
    static public final String KEY_LOCATION = "location";
    static public final String KEY_STATE = "state";
    static public final String KEY_CITY = "city";
    static final String KEY_FIRST_NAME = "first_name";
    static final String KEY_LAST_NAME = "last_name";

    static final String KEY_LAST_TECH_ID = "last_tech_id";

    PrefHelper(Context ctx) {
        super(ctx);
        sInstance = this;
    }

    public String getLocation() {
        return getString(KEY_LOCATION, null);
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

    public String getProject() {
        return getString(KEY_PROJECT, null);
    }

    public void setTechID(String id) {
        setString(KEY_LAST_TECH_ID, id);
    }

    public String getTechID() {
        return getString(KEY_LAST_TECH_ID, null);
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
}

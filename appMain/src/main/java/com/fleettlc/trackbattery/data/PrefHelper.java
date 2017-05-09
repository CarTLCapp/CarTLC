package com.fleettlc.trackbattery.data;

import android.content.Context;

/**
 * Created by dug on 4/17/17.
 */

public class PrefHelper extends PrefHelperBase {

    static PrefHelperBase sInstance;

    public static PrefHelperBase getInstance() {
        return sInstance;
    }

    public static void Init(Context ctx) {
        new PrefHelper(ctx);
    }

    static final String KEY_COUNTRY = "country";
    static final String KEY_STATE = "state";
    static final String KEY_CITY = "city";
    static final String KEY_PROJECT = "project";

    static final String KEY_LAST_TECH_ID = "last_tech_id";

    PrefHelper(Context ctx) {
        super(ctx);
        sInstance = this;
    }

    public void setCountry(String country) {
        setString(KEY_COUNTRY, country);
    }

    public String getCountry() {
        return getString(KEY_COUNTRY, null);
    }

    public void setState(String state) {
        setString(KEY_STATE, state);
    }

    public String getState() {
        return getString(KEY_STATE, null);
    }

    public void setCity(String city) {
        setString(KEY_CITY, city);
    }

    public String getCity() {
        return getString(KEY_CITY, null);
    }

    public void setProject(String project) {
        setString(KEY_PROJECT, project);
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
}

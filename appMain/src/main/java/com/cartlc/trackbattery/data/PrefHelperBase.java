package com.cartlc.trackbattery.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by dug on 4/14/17.
 */

public class PrefHelperBase {

    protected SharedPreferences mPrefs;
    final protected Context mCtx;

    PrefHelperBase(Context ctx) {
        mCtx = ctx;
    }

    SharedPreferences getPrefs() {
        if (mPrefs == null) {
            mPrefs = mCtx.getSharedPreferences(getPrefFile(), 0);
        }
        return mPrefs;
    }

    String getPrefFile() {
        return mCtx.getPackageName() + "_preferences";
    }

    public String getString(String key, String defaultValue) {
        return getPrefs().getString(key, defaultValue);
    }

    public void setString(String key, String value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(key, value);
        editor.commit();
    }

    public long getLong(String key, long defaultValue) {
        return getPrefs().getLong(key, defaultValue);
    }

    public void setLong(String key, long value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putLong(key, value);
        editor.commit();
    }
}

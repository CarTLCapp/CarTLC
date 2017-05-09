package com.cartlc.ocrtest.data;

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

    protected String getString(String key, String defaultValue) {
        return getPrefs().getString(key, defaultValue);
    }

    protected void setString(String key, String value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(key, value);
        editor.commit();
    }
}

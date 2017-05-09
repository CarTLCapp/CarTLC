package com.fleettlc.trackbattery.data;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 4/17/17.
 */

public class TableCity extends TableString {

    static final String TABLE_NAME = "list_city";

    static TableCity sInstance;

    static void Init(SQLiteDatabase db) {
        new TableCity(db);
    }

    public static TableCity getInstance() {
        return sInstance;
    }

    HashMap<String, List<String>> mMap = new HashMap();

    TableCity(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }


    public List<String> getEntries(String state) {
        List<String> list;
        if (!mMap.containsKey(state)) {
            int position = TableState.getInstance().indexOf(state);
            if (position == -1) {
                Timber.e("Could not find " + project);
                list = new ArrayList();
            } else {
                list = query(position);
                mMap.put(project, list);
            }
        } else {
            list = mMap.get(project);
        }
        return list;
    }
}

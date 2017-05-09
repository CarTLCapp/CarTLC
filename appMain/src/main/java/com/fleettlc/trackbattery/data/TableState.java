package com.fleettlc.trackbattery.data;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 4/17/17.
 */

public class TableState extends TableString {

    static final String TABLE_NAME = "list_state";

    static TableState sInstance;

    static void Init(SQLiteDatabase db) {
        new TableState(db);
    }

    public static TableState getInstance() {
        return sInstance;
    }

    HashMap<String, List<String>> mMap = new HashMap();

    TableState(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }

    public List<String> getEntries(String project) {
        List<String> list;
        if (!mMap.containsKey(project)) {
            int position = TableProjects.getInstance().indexOf(project);
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


    public int indexOf(String state) {
        return getEntries().indexOf(project);
    }
}

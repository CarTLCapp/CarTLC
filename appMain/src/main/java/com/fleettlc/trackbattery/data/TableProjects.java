package com.fleettlc.trackbattery.data;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * Created by dug on 4/17/17.
 */

public class TableProjects extends TableString {

    static final String TABLE_NAME = "list_projects";

    static TableProjects sInstance;

    static void Init(SQLiteDatabase db) {
        new TableProjects(db);
    }

    public static TableProjects getInstance() {
        return sInstance;
    }

    List<String> mEntries;

    TableProjects(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }

    public List<String> getEntries() {
        if (mEntries == null) {
            mEntries = query();
        }
        return mEntries;
    }

    public int indexOf(String project) {
        return getEntries().indexOf(project);
    }
}

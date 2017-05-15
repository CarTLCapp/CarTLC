package com.cartlc.tracker.data;

import android.database.sqlite.SQLiteDatabase;

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

    TableProjects(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }
}

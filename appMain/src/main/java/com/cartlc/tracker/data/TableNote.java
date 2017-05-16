package com.cartlc.tracker.data;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dug on 4/17/17.
 */

public class TableNote extends TableString {
    static final String TABLE_NAME = "list_notes";

    static TableNote sInstance;

    static void Init(SQLiteDatabase db) {
        new TableNote(db);
    }

    public static TableNote getInstance() {
        return sInstance;
    }

    TableNote(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }
}

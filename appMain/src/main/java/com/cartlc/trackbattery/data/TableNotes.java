package com.cartlc.trackbattery.data;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dug on 4/17/17.
 */

public class TableNotes extends TableString {

    static final String TABLE_NAME = "list_notes";

    static TableNotes sInstance;

    static void Init(SQLiteDatabase db) {
        new TableNotes(db);
    }

    public static TableNotes getInstance() {
        return sInstance;
    }

    TableNotes(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }
}

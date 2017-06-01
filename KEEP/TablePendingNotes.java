package com.cartlc.tracker.data;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dug on 5/16/17.
 */

public class TablePendingNotes extends TableCollection {
    static final String TABLE_NAME = "pending_notes";

    static TablePendingNotes sInstance;

    static void Init(SQLiteDatabase db) {
        new TablePendingNotes(db);
    }

    public static TablePendingNotes getInstance() {
        return sInstance;
    }

    public TablePendingNotes(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }
}

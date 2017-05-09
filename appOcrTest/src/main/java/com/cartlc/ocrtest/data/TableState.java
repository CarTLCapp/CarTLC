package com.cartlc.ocrtest.data;

import android.database.sqlite.SQLiteDatabase;

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

    TableState(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }
}

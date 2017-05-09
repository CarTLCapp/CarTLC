package com.fleettlc.trackbattery.data;

import android.database.sqlite.SQLiteDatabase;

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

    TableCity(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }
}

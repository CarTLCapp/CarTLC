package com.cartlc.tracker.data;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dug on 4/17/17.
 */

public class TableTrucks extends TableString {

    static final String TABLE_NAME = "list_trucks";

    static TableTrucks sInstance;

    static void Init(SQLiteDatabase db) {
        new TableTrucks(db);
    }

    public static TableTrucks getInstance() {
        return sInstance;
    }

    TableTrucks(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }
}

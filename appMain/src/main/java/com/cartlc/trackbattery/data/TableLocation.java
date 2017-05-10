package com.cartlc.trackbattery.data;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dug on 4/17/17.
 */

public class TableLocation extends TableString {

    static final String TABLE_NAME = "list_location";

    static TableLocation sInstance;

    static void Init(SQLiteDatabase db) {
        new TableLocation(db);
    }

    public static TableLocation getInstance() {
        return sInstance;
    }

    TableLocation(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }
}

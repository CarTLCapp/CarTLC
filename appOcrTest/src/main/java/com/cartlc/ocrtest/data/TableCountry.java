package com.cartlc.ocrtest.data;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dug on 4/17/17.
 */

public class TableCountry extends TableString {

    static final String TABLE_NAME = "list_country";

    static TableCountry sInstance;

    static void Init(SQLiteDatabase db) {
        new TableCountry(db);
    }

    public static TableCountry getInstance() {
        return sInstance;
    }

    TableCountry(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }
}

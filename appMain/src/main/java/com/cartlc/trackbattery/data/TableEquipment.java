package com.cartlc.trackbattery.data;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dug on 4/17/17.
 */

public class TableEquipment extends TableString {

    static final String TABLE_NAME = "list_equipment";

    static TableEquipment sInstance;

    static void Init(SQLiteDatabase db) {
        new TableEquipment(db);
    }

    public static TableEquipment getInstance() {
        return sInstance;
    }

    TableEquipment(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }
}

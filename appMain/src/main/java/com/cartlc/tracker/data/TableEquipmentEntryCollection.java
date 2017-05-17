package com.cartlc.tracker.data;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dug on 5/16/17.
 */

public class TableEquipmentEntryCollection {
    static final String TABLE_NAME = "entry_equipment_collection";

    static TableEquipmentEntryCollection sInstance;

    static void Init(SQLiteDatabase db) {
        new TableEquipmentEntryCollection(db);
    }

    public static TableEquipmentEntryCollection getInstance() {
        return sInstance;
    }

    public TableEquipmentEntryCollection(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }
}

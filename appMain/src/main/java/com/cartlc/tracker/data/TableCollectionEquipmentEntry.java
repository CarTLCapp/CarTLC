/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.data;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dug on 5/16/17.
 */

public class TableCollectionEquipmentEntry extends TableCollection {
    static final String TABLE_NAME = "entry_equipment_collection";

    static TableCollectionEquipmentEntry sInstance;

    static void Init(SQLiteDatabase db) {
        new TableCollectionEquipmentEntry(db);
    }

    public static TableCollectionEquipmentEntry getInstance() {
        return sInstance;
    }

    public TableCollectionEquipmentEntry(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }

    public DataCollectionEquipmentEntry queryForCollectionId(long collectionId) {
        DataCollectionEquipmentEntry collection = new DataCollectionEquipmentEntry(collectionId);
        collection.equipmentListIds = query(collectionId);
        return collection;
    }

}

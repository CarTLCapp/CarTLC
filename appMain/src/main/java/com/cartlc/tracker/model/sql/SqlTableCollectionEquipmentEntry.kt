/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.sql

import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.model.data.DataCollectionEquipmentEntry
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.model.table.TableCollectionEquipmentEntry

/**
 * Created by dug on 5/16/17.
 */

class SqlTableCollectionEquipmentEntry(
        private val db: DatabaseTable,
        dbSql: SQLiteDatabase
) : SqlTableCollection(dbSql, TABLE_NAME), TableCollectionEquipmentEntry {

    companion object {
        internal val TABLE_NAME = "entry_equipment_collection"
    }

    override fun queryForCollectionId(collectionId: Long): DataCollectionEquipmentEntry {
        val collection = DataCollectionEquipmentEntry(db, collectionId)
        collection.equipmentListIds = query(collectionId).toMutableList()
        return collection
    }


}

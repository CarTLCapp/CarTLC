/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataCollectionEquipmentEntry
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableCollectionEquipmentEntry

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

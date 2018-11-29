package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataCollectionEquipmentEntry

interface TableCollectionEquipmentEntry: TableCollection {
    fun queryForCollectionId(collectionId: Long): DataCollectionEquipmentEntry
}
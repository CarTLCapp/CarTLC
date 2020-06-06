package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataCollectionEquipmentEntry

interface TableCollectionEquipmentEntry: TableCollection {
    fun queryForCollectionId(collectionId: Long): DataCollectionEquipmentEntry
}
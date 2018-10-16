package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataCollectionEquipment
import com.cartlc.tracker.model.data.DataCollectionItem

interface TableCollection {
    fun add(item: DataCollectionItem)
    fun countValues(valueId: Long): Int
    fun query(): List<DataCollectionItem>
    fun queryByServerId(server_id: Int): DataCollectionItem?
    fun remove(id: Long)
    fun save(collection: DataCollectionEquipment)
    fun update(item: DataCollectionItem)
}
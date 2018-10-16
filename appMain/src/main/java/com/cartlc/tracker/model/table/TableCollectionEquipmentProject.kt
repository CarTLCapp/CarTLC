package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataCollectionEquipmentProject
import com.cartlc.tracker.model.data.DataCollectionItem

interface TableCollectionEquipmentProject : TableCollection {
    fun addLocal(name: String, projectNameId: Long)
    fun queryForProject(projectNameId: Long): DataCollectionEquipmentProject
    fun removeIfGone(item: DataCollectionItem)
}
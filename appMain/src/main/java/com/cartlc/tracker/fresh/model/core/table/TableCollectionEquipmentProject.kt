package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataCollectionEquipmentProject
import com.cartlc.tracker.fresh.model.core.data.DataCollectionItem

interface TableCollectionEquipmentProject : TableCollection {
    fun addLocal(name: String, projectNameId: Long)
    fun hasEquipment(projectNameId: Long): Boolean
    fun queryForProject(projectNameId: Long): DataCollectionEquipmentProject
    fun removeIfGone(item: DataCollectionItem)
}
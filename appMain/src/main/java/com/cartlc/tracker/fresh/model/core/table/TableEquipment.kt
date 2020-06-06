package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataEquipment

interface TableEquipment {
    fun add(item: DataEquipment)
    fun addLocal(name: String): Long
    fun addTest(name: String): Long
    fun clearChecked()
    fun countChecked(): Int
    fun query(): List<DataEquipment>
    fun query(id: Long): DataEquipment?
    fun query(name: String): Long
    fun queryByServerId(server_id: Int): DataEquipment?
    fun queryChecked(): List<DataEquipment>
    fun queryEquipmentName(id: Long): String?
    fun queryIdsChecked(): List<Long>
    fun removeOrDisable(equip: DataEquipment)
    fun setChecked(ids: List<Long>)
    fun setChecked(item: DataEquipment, flag: Boolean)
    fun update(item: DataEquipment)
}
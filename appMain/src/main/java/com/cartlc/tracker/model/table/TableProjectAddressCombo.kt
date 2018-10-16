package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataProjectAddressCombo

interface TableProjectAddressCombo {
    fun add(projectGroup: DataProjectAddressCombo): Long
    fun count(): Int
    fun countAddress(addressId: Long): Int
    fun countProjects(projectId: Long): Int
    fun query(): List<DataProjectAddressCombo>
    fun query(id: Long): DataProjectAddressCombo?
    fun queryProjectGroupId(projectNameId: Long, addressId: Long): Long
    fun mergeIdenticals(projectGroup: DataProjectAddressCombo)
    fun remove(combo_id: Long)
    fun save(projectGroup: DataProjectAddressCombo): Boolean
    fun updateUsed(id: Long)
}
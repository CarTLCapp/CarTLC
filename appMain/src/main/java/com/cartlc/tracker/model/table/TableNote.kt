package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataNote

interface TableNote {
    fun add(item: DataNote): Long
    fun clearValues()
    fun query(id: Long): DataNote?
    fun query(name: String): Long
    fun query(selection: String? = null, selectionArgs: Array<String>? = null): List<DataNote>
    fun queryByServerId(server_id: Int): DataNote?
    fun removeIfUnused(note: DataNote)
    fun update(item: DataNote)
    fun updateValue(item: DataNote)
}
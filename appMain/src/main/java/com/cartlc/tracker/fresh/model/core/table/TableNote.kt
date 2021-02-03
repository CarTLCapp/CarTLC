package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataNote

interface TableNote {

    companion object {
        // Note: these strings must match the constants found in Note.java on the server side.
        const val NOTE_TRUCK_NUMBER_NAME = "Truck Number"
        const val NOTE_TRUCK_DAMAGE_NAME = "Truck Damage"
        const val NOTE_PARTIAL_INSTALL_REASON = "Partial Install"
    }

    val noteTruckNumber: DataNote?
    val noteTruckDamage: DataNote?
    val notePartialInstall: DataNote?

    fun add(item: DataNote): Long
    fun clearAll()
    fun clearValues()
    fun query(id: Long): DataNote?
    fun query(name: String): Long
    fun query(selection: String? = null, selectionArgs: Array<String>? = null): List<DataNote>
    fun queryByServerId(server_id: Int): DataNote?
    fun removeIfUnused(note: DataNote)
    fun update(item: DataNote)
    fun updateValue(item: DataNote)
    fun updateValues(notes: List<DataNote>)
}
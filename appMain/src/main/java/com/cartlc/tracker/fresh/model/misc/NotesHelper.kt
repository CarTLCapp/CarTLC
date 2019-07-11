/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.model.misc

import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.core.data.DataNote

class NotesHelper(
        private val repo: CarRepository
) {

    private val db = repo.db
    private val currentEditEntry
        get() = repo.prefHelper.currentEditEntry

    // TODO: Move this over to NoteHelper in DatabaseTable
    val notesFromCurrentFlowElementId: List<DataNote>
        get() = repo.currentFlowElementId?.let { elementId ->
            return repo.db.tableFlowElementNote.queryNotes(elementId)
        } ?: emptyList()

    val notesWithValuesUsingCurrentCollectionId: List<DataNote>
        get() = currentEditEntry?.let { db.tableCollectionNoteEntry.query(it.noteCollectionId) } ?: emptyList()

    fun getTruckNumberValue(entryCollectionId: Long = 0L): String? {
        val truckNote = db.tableNote.noteTruckNumber ?: return null
        var coreValue = truckNote.value
        if (entryCollectionId > 0) {
            db.tableCollectionNoteEntry.query(entryCollectionId, truckNote.id)?.let {
                coreValue = it.value
            }
        }
        return coreValue
    }

}
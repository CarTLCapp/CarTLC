/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.model.misc

import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable

class NotesHelper(
        private val repo: CarRepository
) {

    private val db = repo.db
    private val currentEditEntry
        get() = repo.prefHelper.currentEditEntry

    val notesFromCurrentFlowElementId: List<DataNote>
        get() = repo.currentFlowElementId?.let { elementId ->
            return repo.db.tableFlowElementNote.queryNotes(elementId)
        } ?: emptyList()

    val notesWithValuesUsingCurrentCollectionId: List<DataNote>
        get() = currentEditEntry?.let { db.tableCollectionNoteEntry.query(it.noteCollectionId) } ?: emptyList()


//    private fun pushToBottom(name: String) {
//        val others = ArrayList<DataNote>()
//        for (item in items) {
//            if (item.name.startsWith(name)) {
//                others.add(item)
//                break
//            }
//        }
//        for (item in others) {
//            items.remove(item)
//            items.add(item)
//        }
//    }

}
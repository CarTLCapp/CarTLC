package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataNote

interface TableCollectionNoteEntry {
    fun countNotes(noteId: Long): Int
    fun query(collectionId: Long): List<DataNote>
    fun save(collectionId: Long, notes: List<DataNote>)
}
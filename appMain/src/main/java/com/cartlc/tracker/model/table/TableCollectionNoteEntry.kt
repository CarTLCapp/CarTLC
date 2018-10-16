package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataNote

interface TableCollectionNoteEntry {
    fun countNotes(noteId: Long): Int
    fun query(collectionId: Long): List<DataNote>
    fun save(collectionId: Long, notes: List<DataNote>)
}
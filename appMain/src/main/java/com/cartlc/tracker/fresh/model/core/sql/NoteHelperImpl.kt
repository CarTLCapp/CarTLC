/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.model.core.sql

import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.NoteHelper

class NoteHelperImpl(
        private val db: DatabaseTable
) : NoteHelper {

    override fun getPendingNotes(projectNameId: Long): List<DataNote> {
        val notes = db.tableCollectionNoteProject.getNotes(projectNameId).toMutableList()
        db.tableNote.noteTruckNumber?.let {
            notes.add(it)
        }
        // If damage wasn't added the value should be null.
        db.tableNote.noteTruckDamage?.let { note ->
            note.value?.let {
                notes.add(note)
            }
        }
        db.tableFlow.queryBySubProjectId(projectNameId.toInt())?.let { flow ->
            val elements = db.tableFlowElement.queryByFlowId(flow.id)
            for (element in elements) {
                notes.addAll(db.tableFlowElementNote.queryNotes(element.id))
            }
        }
        return notes
    }
}
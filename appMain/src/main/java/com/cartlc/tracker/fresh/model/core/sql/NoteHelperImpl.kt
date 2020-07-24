/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.model.core.sql

import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.NoteHelper

class NoteHelperImpl(
        private val db: DatabaseTable
) : NoteHelper {

    override fun getNotesFromCurrentFlowElementId(elementId: Long?): List<DataNote> {
        return elementId?.let { id ->
            db.tableFlowElementNote.queryNotes(id)
        } ?: emptyList()
    }

    override fun getPendingNotes(projectNameId: Long, withPartialInstallReason: Boolean): List<DataNote> {
        val notes = mutableListOf<DataNote>()
        db.tableNote.noteTruckNumber?.let {
            notes.add(it)
        }
        // If damage wasn't added the value should be null.
        db.tableNote.noteTruckDamage?.let { note ->
            note.value?.let {
                notes.add(note)
            }
        }
        // If partial damage was added, use it
        if (withPartialInstallReason) {
            db.tableNote.notePartialInstall?.let { note ->
                note.value?.let {
                    notes.add(note)
                }
            }
        }
        db.tableFlow.queryBySubProjectId(projectNameId.toInt())?.let { flow ->
            val elements = db.tableFlowElement.queryByFlowId(flow.id)
            for (element in elements) {
                val addTo = db.tableFlowElementNote.queryNotes(element.id)
                for (note in addTo) {
                    if (!notes.contains(note)) {
                        notes.add(note)
                    }
                }
            }
        }
        return notes
    }

    override fun getNotesOverlaidFrom(elementId: Long, entry: DataEntry?): List<DataNote> {
        val notes = mutableListOf<DataNote>()
        val elements = db.tableFlowElementNote.query(elementId)
        for (element in elements) {
            db.tableNote.query(element.noteId)?.let { note ->
                entry?.let { entry ->
                    entry.overlayNoteValue(note.id)?.let {
                        notes.add(it)
                    } ?: notes.add(note)
                } ?: notes.add(note)
            }
        }
        return notes
    }

}
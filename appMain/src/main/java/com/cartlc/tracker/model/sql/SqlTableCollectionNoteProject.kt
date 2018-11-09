/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.sql

import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.model.data.DataCollectionItem
import com.cartlc.tracker.model.data.DataNote
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.model.table.TableCollectionNoteProject

import java.util.ArrayList

import timber.log.Timber

/**
 * Created by dug on 5/16/17.
 */

class SqlTableCollectionNoteProject(
        private val db: DatabaseTable,
        sqlDb: SQLiteDatabase
) : SqlTableCollection(sqlDb, TABLE_NAME), TableCollectionNoteProject {

    companion object {
        internal val TABLE_NAME = "note_project_collection"
    }

    fun addByName(projectName: String, notes: List<DataNote>) {
        var projectNameId = db.tableProjects.queryProjectName(projectName)
        if (projectNameId < 0) {
            projectNameId = db.tableProjects.addTest(projectName)
        }
        addByNameTest(projectNameId, notes)
    }

    internal fun addByNameTest(projectNameId: Long, notes: List<DataNote>) {
        val list = ArrayList<Long>()
        for (note in notes) {
            var id = db.tableNote.query(note.name)
            if (id < 0) {
                id = db.tableNote.add(note)
            }
            list.add(id)
        }
        addTest(projectNameId, list)
    }

    // Get the list of notes associated with the project.
    override fun getNotes(projectNameId: Long): List<DataNote> {
        val noteIds = query(projectNameId)
        val list = ArrayList<DataNote>()
        for (noteId in noteIds) {
            val note = db.tableNote.query(noteId)
            if (note == null) {
                Timber.e("Could not find note with ID $noteId")
            } else {
                list.add(note)
            }
        }
        return list
    }

    override fun removeIfGone(item: DataCollectionItem) {
        if (item.isBootstrap) {
            if (db.tableNote.query(item.value_id) == null) {
                Timber.i("remove(" + item.id + ", " + item.toString() + ")")
                remove(item.id)
            }
        }
    }

}

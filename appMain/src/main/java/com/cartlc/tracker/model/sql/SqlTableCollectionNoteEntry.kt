/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import com.cartlc.tracker.model.data.DataNote
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.model.table.TableCollectionNoteEntry

import com.cartlc.tracker.ui.app.TBApplication

import java.util.ArrayList

/**
 * Created by dug on 5/16/17.
 */

class SqlTableCollectionNoteEntry(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
): TableCollectionNoteEntry {

    companion object {
        internal val TABLE_NAME = "note_entry_collection"

        internal val KEY_ROWID = "_id"
        internal val KEY_COLLECTION_ID = "collection_id"
        internal val KEY_NOTE_ID = "note_id"
        internal val KEY_VALUE = "value"
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_COLLECTION_ID)
        sbuf.append(" long, ")
        sbuf.append(KEY_NOTE_ID)
        sbuf.append(" long, ")
        sbuf.append(KEY_VALUE)
        sbuf.append(" text)")

        dbSql.execSQL(sbuf.toString())
    }

    override fun countNotes(noteId: Long): Int {
        var count = 0
        try {
            val where = "$KEY_NOTE_ID=?"
            val whereArgs = arrayOf(java.lang.Long.toString(noteId))
            val cursor = dbSql.query(TABLE_NAME, null, where, whereArgs, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollectionNoteEntry::class.java, "countNotes(id)", "db")
        }

        return count
    }

    // There are TWO tableNote tables. One is TableCollectionnNoteProject which stores the
    // defined notes for each project. The enteredOther is this one which stores the values.
    //
    // The values are stored right now in the SqlTableNote table which is represented by the incoming
    // notes. We want them to also be stored now into this table.
    //
    // The reason we need two tables for the values, is that each time a new tableEntry is created
    // a new set of values for the notes are needed as well. This new set of values is stored
    // in this table. The SqlTableNote table only holds the values temporarily while they are being entered.
    override fun save(collectionId: Long, notes: List<DataNote>) {
        dbSql.beginTransaction()
        try {
            removeCollection(collectionId)
            val values = ContentValues()
            for (note in notes) {
                if (!TextUtils.isEmpty(note.value)) {
                    values.clear()
                    values.put(KEY_COLLECTION_ID, collectionId)
                    values.put(KEY_NOTE_ID, note.id)
                    values.put(KEY_VALUE, note.value)
                    dbSql.insert(TABLE_NAME, null, values)
                }
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollectionNoteEntry::class.java, "saveUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    // Get list of notes the associated collection id in this table and their values.
    override fun query(collectionId: Long): List<DataNote> {
        val list = ArrayList<DataNote>()
        try {
            val columns = arrayOf(KEY_NOTE_ID, KEY_VALUE)
            val selection = "$KEY_COLLECTION_ID =?"
            val selectionArgs = arrayOf(java.lang.Long.toString(collectionId))
            val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null)
            val idxNoteId = cursor.getColumnIndex(KEY_NOTE_ID)
            val idxValueId = cursor.getColumnIndex(KEY_VALUE)
            var note: DataNote?
            while (cursor.moveToNext()) {
                note = db.tableNote.query(cursor.getLong(idxNoteId)) // Fill out with original values.
                note!!.value = cursor.getString(idxValueId) // override
                list.add(note)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollectionNoteEntry::class.java, "query()", "db")
        }
        return list
    }

    internal fun removeCollection(collection_id: Long) {
        val where = "$KEY_COLLECTION_ID=?"
        val whereArgs = arrayOf(java.lang.Long.toString(collection_id))
        dbSql.delete(TABLE_NAME, where, whereArgs)
    }


}

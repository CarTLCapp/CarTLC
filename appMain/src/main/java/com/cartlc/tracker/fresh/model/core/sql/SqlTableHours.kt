/*
 * Copyright 2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.VisibleForTesting
import com.cartlc.tracker.fresh.model.core.data.DataHours
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableHours
import com.cartlc.tracker.fresh.ui.app.TBApplication
import timber.log.Timber

/**
 * Daily Hours Report sql table
 */
@VisibleForTesting
open class SqlTableHours(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
) : TableHours {

    companion object {

        private val TAG = SqlTableHours::class.java.simpleName

        private const val TABLE_NAME = "table_hours"

        private const val KEY_ROWID = "_id"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_DATE = "entry_date"
        private const val KEY_PROJECT_ID = "project_id"
        private const val KEY_PROJECT_DESC = "project_desc"
        private const val KEY_START_TIME = "start_time"
        private const val KEY_END_TIME = "end_time"
        private const val KEY_LUNCH_TIME = "lunch_time"
        private const val KEY_BREAK_TIME = "break_time"
        private const val KEY_DRIVE_TIME = "drive_time"
        private const val KEY_NOTES = "notes"
        private const val KEY_UPLOADED = "uploaded"
        private const val KEY_IS_READY = "is_ready"
    }

    override fun clearAll() {
        try {
            dbSql.delete(TABLE_NAME, null, null)
        } catch (ex: Exception) {
        }
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_SERVER_ID)
        sbuf.append(" int default 0, ")
        sbuf.append(KEY_DATE)
        sbuf.append(" long default 0, ")
        sbuf.append(KEY_PROJECT_ID)
        sbuf.append(" long default 0, ")
        sbuf.append(KEY_PROJECT_DESC)
        sbuf.append(" text, ")
        sbuf.append(KEY_START_TIME)
        sbuf.append(" int, ")
        sbuf.append(KEY_END_TIME)
        sbuf.append(" int, ")
        sbuf.append(KEY_LUNCH_TIME)
        sbuf.append(" int, ")
        sbuf.append(KEY_BREAK_TIME)
        sbuf.append(" int, ")
        sbuf.append(KEY_DRIVE_TIME)
        sbuf.append(" int, ")
        sbuf.append(KEY_NOTES)
        sbuf.append(" text, ")
        sbuf.append(KEY_UPLOADED)
        sbuf.append(" bit default 0, ")
        sbuf.append(KEY_IS_READY)
        sbuf.append(" bit default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    // region TableDaar

    // endregion TableDaar

    override fun save(item: DataHours): Long {
        val values = ContentValues()
        values.put(KEY_SERVER_ID, item.serverId)
        values.put(KEY_DATE, item.date)
        values.put(KEY_PROJECT_ID, item.projectNameId)
        values.put(KEY_PROJECT_DESC, item.projectDesc)
        values.put(KEY_START_TIME, item.startTime)
        values.put(KEY_END_TIME, item.endTime)
        values.put(KEY_LUNCH_TIME, item.lunchTime)
        values.put(KEY_BREAK_TIME, item.breakTime)
        values.put(KEY_DRIVE_TIME, item.driveTime)
        values.put(KEY_NOTES, item.notes)
        values.put(KEY_UPLOADED, if (item.uploaded) 1 else 0)
        values.put(KEY_IS_READY, if (item.isReady) 1 else 0)
        var saved = false
        if (item.id > 0) {
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(item.id.toString())
            saved = dbSql.update(TABLE_NAME, values, where, whereArgs) != 0
        }
        if (!saved) {
            item.id = dbSql.insert(TABLE_NAME, null, values)
        }
        return item.id
    }

    // Right now only used to update a few fields.
    // Later this will be extended to saveUploaded everything.
    override fun saveUploaded(item: DataHours) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_SERVER_ID, item.serverId)
            values.put(KEY_UPLOADED, if (item.uploaded) 1 else 0)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(item.id.toString())
            if (dbSql.update(TABLE_NAME, values, where, whereArgs) == 0) {
                Timber.e("$TAG.saveUploaded(): Unable to update $TABLE_NAME")
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableHours::class.java, "saveUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun query(id: Long): DataHours? {
        val selection = "$KEY_ROWID=?"
        val selectionArgs = arrayOf(id.toString())
        return query(selection, selectionArgs).let { result ->
            if (result.isEmpty()) null else result.first()
        }
    }

    override fun queryByServerId(id: Long): DataHours? {
        val selection = "$KEY_SERVER_ID=?"
        val selectionArgs = arrayOf(id.toString())
        return query(selection, selectionArgs).let { result ->
            if (result.isEmpty()) null else result.first()
        }
    }

    override fun queryMostRecentUploaded(): DataHours? {
        val selection = "$KEY_UPLOADED=1"
        val orderBy = "$KEY_DATE desc"
        val cursor = dbSql.query(TABLE_NAME, null, selection, null, null, null, orderBy, "1")
        return query(cursor).firstOrNull()
    }

    private fun query(selection: String, selectionArgs: Array<String>?): List<DataHours> {
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        return query(cursor)
    }

    private fun query(cursor: Cursor): List<DataHours> {
        val list = mutableListOf<DataHours>()

        val idxId = cursor.getColumnIndex(KEY_ROWID)
        val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
        val idxDate = cursor.getColumnIndex(KEY_DATE)
        val idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID)
        val idxProjectDesc = cursor.getColumnIndex(KEY_PROJECT_DESC)
        val idxStartTime = cursor.getColumnIndex(KEY_START_TIME)
        val idxEndTime = cursor.getColumnIndex(KEY_END_TIME)
        val idxLunchTime = cursor.getColumnIndex(KEY_LUNCH_TIME)
        val idxBreakTime = cursor.getColumnIndex(KEY_BREAK_TIME)
        val idxDriveTime = cursor.getColumnIndex(KEY_DRIVE_TIME)
        val idxNotes = cursor.getColumnIndex(KEY_NOTES)
        val idxUploaded = cursor.getColumnIndex(KEY_UPLOADED)
        val idxIsReady = cursor.getColumnIndex(KEY_IS_READY)

        while (cursor.moveToNext()) {
            val item = DataHours(db)
            item.id = cursor.getLong(idxId)
            item.serverId = cursor.getLong(idxServerId)
            item.date = cursor.getLong(idxDate)
            item.projectNameId = cursor.getLong(idxProjectId)
            item.projectDesc = cursor.getString(idxProjectDesc)
            item.startTime = cursor.getInt(idxStartTime)
            item.endTime = cursor.getInt(idxEndTime)
            item.lunchTime = cursor.getInt(idxLunchTime)
            item.breakTime = cursor.getInt(idxBreakTime)
            item.driveTime = cursor.getInt(idxDriveTime)
            item.notes = cursor.getString(idxNotes)
            item.uploaded = cursor.getShort(idxUploaded).toInt() != 0
            item.isReady = cursor.getShort(idxIsReady).toInt() != 0
            list.add(item)
        }
        cursor.close()
        return list
    }

    override fun queryReadyAndNotUploaded(): List<DataHours> {
        val selection = "$KEY_UPLOADED=0 AND $KEY_IS_READY=1"
        return query(selection, null)
    }

    override fun queryNotReady(): List<DataHours> {
        val selection = "$KEY_IS_READY=0"
        return query(selection, null)
    }

    fun clearUploaded() {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_UPLOADED, 0)
            values.put(KEY_SERVER_ID, 0)
            dbSql.update(TABLE_NAME, values, null, null)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            Timber.e(ex)
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun remove(item: DataHours) {
        val where = "$KEY_ROWID=?"
        val whereArgs = arrayOf(item.id.toString())
        dbSql.delete(TABLE_NAME, where, whereArgs)
    }
}

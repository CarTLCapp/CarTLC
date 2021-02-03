/*
 * Copyright 2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.VisibleForTesting
import com.cartlc.tracker.fresh.model.core.data.DataDaar
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableDaar
import com.cartlc.tracker.fresh.ui.app.TBApplication
import timber.log.Timber

/**
 * Daily After Action Report sql table
 */
@VisibleForTesting
open class SqlTableDaar(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
) : TableDaar {

    companion object {

        private val TAG = SqlTableDaar::class.java.simpleName

        private const val TABLE_NAME = "table_daar"

        private const val KEY_ROWID = "_id"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_DATE = "created"
        private const val KEY_PROJECT_ID = "project_id"
        private const val KEY_PROJECT_DESC = "project_desc";
        private const val KEY_WORK_COMPLETED = "work_completed"
        private const val KEY_MISSED_UNITS = "missed_units"
        private const val KEY_ISSUES = "issues"
        private const val KEY_INJURIES = "injuries"
        private const val KEY_START_TIME_TOMORROW = "start_time_tomorrow"
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
        sbuf.append(KEY_WORK_COMPLETED)
        sbuf.append(" text, ")
        sbuf.append(KEY_MISSED_UNITS)
        sbuf.append(" text, ")
        sbuf.append(KEY_ISSUES)
        sbuf.append(" text, ")
        sbuf.append(KEY_INJURIES)
        sbuf.append(" text, ")
        sbuf.append(KEY_START_TIME_TOMORROW)
        sbuf.append(" long default 0, ")
        sbuf.append(KEY_UPLOADED)
        sbuf.append(" bit default 0, ")
        sbuf.append(KEY_IS_READY)
        sbuf.append(" bit default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    // region TableDaar

    // endregion TableDaar

    override fun save(item: DataDaar): Long {
        val values = ContentValues()
        values.put(KEY_SERVER_ID, item.serverId)
        values.put(KEY_DATE, item.date)
        values.put(KEY_PROJECT_ID, item.projectNameId)
        values.put(KEY_PROJECT_DESC, item.projectDesc)
        values.put(KEY_WORK_COMPLETED, item.workCompleted)
        values.put(KEY_MISSED_UNITS, item.missedUnits)
        values.put(KEY_ISSUES, item.issues)
        values.put(KEY_INJURIES, item.injuries)
        values.put(KEY_START_TIME_TOMORROW, item.startTimeTomorrow)
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
    override fun saveUploaded(item: DataDaar) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_SERVER_ID, item.serverId)
            values.put(KEY_UPLOADED, if (item.uploaded) 1 else 0)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(java.lang.Long.toString(item.id))
            if (dbSql.update(TABLE_NAME, values, where, whereArgs) == 0) {
                Timber.e("$TAG.saveUploaded(): Unable to update $TABLE_NAME")
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableDaar::class.java, "saveUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun query(id: Long): DataDaar? {
        val selection = "$KEY_ROWID=?"
        val selectionArgs = arrayOf(id.toString())
        return query(selection, selectionArgs).let { result ->
            if (result.isEmpty()) null else result.first()
        }
    }

    override fun queryByServerId(id: Long): DataDaar? {
        val selection = "$KEY_SERVER_ID=?"
        val selectionArgs = arrayOf(id.toString())
        return query(selection, selectionArgs).let { result ->
            if (result.isEmpty()) null else result.first()
        }
    }

    override fun queryMostRecentUploaded(): DataDaar? {
        val selection = "$KEY_UPLOADED=1"
        val orderBy = "$KEY_DATE desc"
        val cursor = dbSql.query(TABLE_NAME, null, selection, null, null, null, orderBy, "1")
        return query(cursor).firstOrNull()
    }

    private fun query(selection: String, selectionArgs: Array<String>?): List<DataDaar> {
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        return query(cursor)
    }

    private fun query(cursor: Cursor): List<DataDaar> {
        val list = mutableListOf<DataDaar>()

        val idxId = cursor.getColumnIndex(KEY_ROWID)
        val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
        val idxDate = cursor.getColumnIndex(KEY_DATE)
        val idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID)
        val idxProjectDesc = cursor.getColumnIndex(KEY_PROJECT_DESC)
        val idxWorkCompleted = cursor.getColumnIndex(KEY_WORK_COMPLETED)
        val idxMissedUnits = cursor.getColumnIndex(KEY_MISSED_UNITS)
        val idxIssues = cursor.getColumnIndex(KEY_ISSUES)
        val idxInjuries = cursor.getColumnIndex(KEY_INJURIES)
        val idxStartTimeTomorrow = cursor.getColumnIndex(KEY_START_TIME_TOMORROW)
        val idxUploaded = cursor.getColumnIndex(KEY_UPLOADED)
        val idxIsReady = cursor.getColumnIndex(KEY_IS_READY)

        while (cursor.moveToNext()) {
            val item = DataDaar(db)
            item.id = cursor.getLong(idxId)
            item.serverId = cursor.getLong(idxServerId)
            item.date = cursor.getLong(idxDate)
            item.projectNameId = cursor.getLong(idxProjectId)
            item.projectDesc = cursor.getString(idxProjectDesc)
            item.workCompleted = cursor.getString(idxWorkCompleted)
            item.missedUnits = cursor.getString(idxMissedUnits)
            item.issues = cursor.getString(idxIssues)
            item.injuries = cursor.getString(idxInjuries)
            item.startTimeTomorrow = cursor.getLong(idxStartTimeTomorrow)
            item.uploaded = cursor.getShort(idxUploaded).toInt() != 0
            item.isReady = cursor.getShort(idxIsReady).toInt() != 0
            list.add(item)
        }
        cursor.close()
        return list
    }

    override fun queryReadyAndNotUploaded(): List<DataDaar> {
        val selection = "$KEY_UPLOADED=0 AND $KEY_IS_READY=1"
        return query(selection, null)
    }

    override fun queryNotReady(): List<DataDaar> {
        val selection = "$KEY_IS_READY=0"
        return query(selection, null);
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

    override fun remove(item: DataDaar) {
        val where = "$KEY_ROWID=?"
        val whereArgs = arrayOf(item.id.toString())
        dbSql.delete(TABLE_NAME, where, whereArgs)
    }
}

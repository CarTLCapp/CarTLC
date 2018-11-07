/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

import com.cartlc.tracker.ui.app.TBApplication

import java.util.ArrayList

/**
 * Created by dug on 4/14/17.
 */

abstract class SqlTableString protected constructor(protected val mDb: SQLiteDatabase, protected val mTableName: String) {

    fun drop() {
        mDb.execSQL("DROP TABLE IF EXISTS $mTableName")
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(mTableName)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_VALUE)
        sbuf.append(" text not null)")
        mDb.execSQL(sbuf.toString())
    }

    fun clear() {
        try {
            mDb.delete(mTableName, null, null)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableString::class.java, "clear()", "db")
        }
    }

    fun remove(value: String) {
        try {
            val where = "$KEY_VALUE=?"
            val whereArgs = arrayOf(value)
            mDb.delete(mTableName, where, whereArgs)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableString::class.java, "remove()", value)
        }
    }

    fun remove(id: Long) {
        try {
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(java.lang.Long.toString(id))
            mDb.delete(mTableName, where, whereArgs)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableString::class.java, "remove(id)", "db")
        }
    }

    fun add(list: List<String>) {
        mDb.beginTransaction()
        try {
            val values = ContentValues()
            for (value in list) {
                values.clear()
                values.put(KEY_VALUE, value)
                mDb.insert(mTableName, null, values)
            }
            mDb.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableString::class.java, "add(list)", "db")
        } finally {
            mDb.endTransaction()
        }
    }

    fun add(item: String): Long {
        var id = -1L
        mDb.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_VALUE, item)
            id = mDb.insert(mTableName, null, values)
            mDb.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableString::class.java, "add(item)", "db")
        } finally {
            mDb.endTransaction()
        }
        return id
    }

    fun count(): Int {
        var count = 0
        try {
            val cursor = mDb.query(mTableName, null, null, null, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableString::class.java, "count()", "db")
        }
        return count
    }

    fun query(): List<String> {
        val list = ArrayList<String>()
        try {
            val columns = arrayOf(KEY_VALUE)
            val orderBy = "$KEY_VALUE ASC"

            val cursor = mDb.query(mTableName, columns, null, null, null, null, orderBy)
            val idxValue = cursor.getColumnIndex(KEY_VALUE)
            while (cursor.moveToNext()) {
                list.add(cursor.getString(idxValue))
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableString::class.java, "query()", "db")
        }

        // Move enteredOther to bottom of the list.
        if (list.contains(TBApplication.OTHER)) {
            list.remove(TBApplication.OTHER)
            list.add(TBApplication.OTHER)
        }
        return list
    }

    fun query(id: Long): String? {
        var projectName: String? = null
        try {
            val columns = arrayOf(KEY_VALUE)
            val selection = "$KEY_ROWID=?"
            val selectionArgs = arrayOf(java.lang.Long.toString(id))
            val cursor = mDb.query(mTableName, columns, selection, selectionArgs, null, null, null)
            val idxValue = cursor.getColumnIndex(KEY_VALUE)
            if (cursor.moveToFirst()) {
                projectName = cursor.getString(idxValue)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableString::class.java, "query(id)", "db")
        }
        return projectName
    }

    fun query(name: String): Long {
        var rowId = -1L
        try {
            val columns = arrayOf(KEY_ROWID)
            val selection = "$KEY_VALUE=?"
            val selectionArgs = arrayOf(name)
            val cursor = mDb.query(mTableName, columns, selection, selectionArgs, null, null, null)
            val idxValue = cursor.getColumnIndex(KEY_ROWID)
            if (cursor.moveToFirst()) {
                rowId = cursor.getLong(idxValue)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableString::class.java, "query(name)", name)
        }
        return rowId
    }

    companion object {

        internal val KEY_ROWID = "_id"
        internal val KEY_VALUE = "value"
    }
}

/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.model.data.DataString
import com.cartlc.tracker.model.table.TableString

import com.cartlc.tracker.ui.app.TBApplication

import java.util.ArrayList

/**
 * Created by dug on 4/14/17.
 */

class SqlTableString(
        private val db: SQLiteDatabase
) : TableString {

    companion object {
        private const val TABLE_NAME = "table_strings"
        private const val KEY_ROWID = "_id"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_VALUE = "string_value"
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_SERVER_ID)
        sbuf.append(" integer, ")
        sbuf.append(KEY_VALUE)
        sbuf.append(" text not null)")
        db.execSQL(sbuf.toString())
    }

    fun clear() {
        try {
            db.delete(TABLE_NAME, null, null)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableString::class.java, "clear()", "db")
        }
    }

    override fun add(text: String): Long {
        val columns = arrayOf(KEY_ROWID, KEY_VALUE)
        val selection = "$KEY_VALUE=?"
        val selectionArgs = arrayOf(text)
        val cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
        val id: Long
        if (cursor.moveToFirst() && cursor.count > 0) {
            val idxId = cursor.getColumnIndex(KEY_ROWID)
            id = cursor.getLong(idxId)
        } else {
            val values = ContentValues()
            values.put(KEY_VALUE, text)
            id = db.insert(TABLE_NAME, null, values)
        }
        cursor.close()
        return id
    }

    override fun save(data: DataString): Long {
        var id = -1L
        val values = ContentValues()
        values.put(KEY_VALUE, data.value)
        values.put(KEY_SERVER_ID, data.serverId)
        var saved = false
        if (data.id > 0) {
            val where = "${KEY_ROWID}=?"
            val whereArgs = arrayOf(data.id.toString())
            saved = db.update(TABLE_NAME, values, where, whereArgs) != 0
        }
        if (!saved) {
            data.id = db.insert(TABLE_NAME, null, values)
        }
        return id
    }

    override fun queryNotUploaded(): List<DataString> {
        val list = ArrayList<DataString>()
        val columns = arrayOf(KEY_ROWID, KEY_VALUE)
        val selection = "$KEY_SERVER_ID=0"
        val cursor = db.query(TABLE_NAME, columns, selection, null, null, null, null)
        val idxValue = cursor.getColumnIndex(KEY_VALUE)
        val idxId = cursor.getColumnIndex(KEY_ROWID)
        while (cursor.moveToNext()) {
            var data = DataString()
            data.value = cursor.getString(idxValue)
            data.id = cursor.getLong(idxId)
            list.add(data)
        }
        cursor.close()
        return list
    }

    override fun query(id: Long): DataString? {
        var data: DataString? = null
        val columns = arrayOf(KEY_SERVER_ID, KEY_VALUE)
        val selection = "$KEY_ROWID=?"
        val selectionArgs = arrayOf(java.lang.Long.toString(id))
        val cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
        val idxValue = cursor.getColumnIndex(KEY_VALUE)
        val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
        if (cursor.moveToFirst()) {
            data = DataString()
            data.value = cursor.getString(idxValue)
            data.id = id
            data.serverId = cursor.getLong(idxServerId)
        }
        cursor.close()
        return data
    }

    override fun queryByServerId(id: Long): DataString? {
        var data: DataString? = null
        val columns = arrayOf(KEY_ROWID, KEY_VALUE)
        val selection = "$KEY_SERVER_ID=?"
        val selectionArgs = arrayOf(java.lang.Long.toString(id))
        val cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
        val idxValue = cursor.getColumnIndex(KEY_VALUE)
        val idxId = cursor.getColumnIndex(KEY_ROWID)
        if (cursor.moveToFirst()) {
            data = DataString()
            data.value = cursor.getString(idxValue)
            data.serverId = id
            data.id = cursor.getLong(idxId)
        }
        cursor.close()
        return data
    }
}

/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataFlowElement
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableFlowElement
import com.cartlc.tracker.fresh.ui.app.TBApplication

/**
 * Created by dug on 8/31/17.
 */

class SqlTableFlowElement(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
) : TableFlowElement {

    companion object {

        private const val TABLE_NAME = "table_flow_element"

        private const val KEY_ROWID = "_id"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_FLOW_ID = "sub_flow_id"
        private const val KEY_PROMPT = "prompt"
        private const val KEY_TYPE = "type"
        private const val KEY_REQUEST_IMAGE = "request_image"
        private const val KEY_GENERIC_NOTE = "generic_note"

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
        sbuf.append(KEY_FLOW_ID)
        sbuf.append(" int default 0, ")
        sbuf.append(KEY_PROMPT)
        sbuf.append(" text default null, ")
        sbuf.append(KEY_TYPE)
        sbuf.append(" char(2) default null, ")
        sbuf.append(KEY_REQUEST_IMAGE)
        sbuf.append(" bit default 0, ")
        sbuf.append(KEY_GENERIC_NOTE)
        sbuf.append(" bit default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    override fun add(item: DataFlowElement): Long {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_SERVER_ID, item.serverId)
            values.put(KEY_FLOW_ID, item.flowId)
            values.put(KEY_PROMPT, item.prompt)
            values.put(KEY_TYPE, item.type.code.toString())
            values.put(KEY_REQUEST_IMAGE, if (item.requestImage) 1 else 0)
            values.put(KEY_GENERIC_NOTE, if (item.genericNote) 1 else 0)
            item.id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableFlowElement::class.java, "add(item)", "db")
        } finally {
            dbSql.endTransaction()
        }
        return item.id
    }

    override fun query(): List<DataFlowElement> {
        return query(null, null)
    }

    private fun query(selection: String?, selectionArgs: Array<String>?): List<DataFlowElement> {
        val list = ArrayList<DataFlowElement>()
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
        val idxFlowId = cursor.getColumnIndex(KEY_FLOW_ID)
        val idxPrompt = cursor.getColumnIndex(KEY_PROMPT)
        val idxType = cursor.getColumnIndex(KEY_TYPE)
        val idxRequestImage = cursor.getColumnIndex(KEY_REQUEST_IMAGE)
        val idxGenericNote = cursor.getColumnIndex(KEY_GENERIC_NOTE)

        var item: DataFlowElement
        while (cursor.moveToNext()) {
            item = DataFlowElement(
                    cursor.getLong(idxRowId),
                    cursor.getInt(idxServerId),
                    cursor.getLong(idxFlowId),
                    cursor.getString(idxPrompt),
                    DataFlowElement.Type.from(cursor.getString(idxType)),
                    cursor.getShort(idxRequestImage).toInt() != 0,
                    cursor.getShort(idxGenericNote).toInt() != 0
            )
            list.add(item)
        }
        cursor.close()
        return list
    }

    override fun queryByServerId(server_id: Int): DataFlowElement? {
        val selection = "$KEY_SERVER_ID=?"
        val selectionArgs = arrayOf(server_id.toString())
        val list = query(selection, selectionArgs)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    override fun queryByFlowId(flow_id: Long): List<DataFlowElement> {
        val selection = "$KEY_FLOW_ID=?"
        val selectionArgs = arrayOf(flow_id.toString())
        return query(selection, selectionArgs)
    }

    override fun update(item: DataFlowElement) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_SERVER_ID, item.serverId)
            values.put(KEY_FLOW_ID, item.flowId)
            values.put(KEY_PROMPT, item.prompt)
            values.put(KEY_TYPE, item.type.code.toString())
            values.put(KEY_REQUEST_IMAGE, if (item.requestImage) 1 else 0)
            values.put(KEY_GENERIC_NOTE, if (item.genericNote) 1 else 0)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(item.id.toString())
            dbSql.update(TABLE_NAME, values, where, whereArgs)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableFlowElement::class.java, "update()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun remove(item: DataFlowElement) {
        val where = "$KEY_ROWID=?"
        val whereArgs = arrayOf(item.id.toString())
        dbSql.delete(TABLE_NAME, where, whereArgs)
    }

    override fun toString(flow_id: Long): String {
        val sbuf = StringBuilder()
        val elements = queryByFlowId(flow_id)
        for (element in elements) {
            if (sbuf.isNotEmpty()) {
                sbuf.append(" ")
            }
            sbuf.append("[")
            sbuf.append(element.type.code.toString())
            if (!element.prompt.isNullOrEmpty()) {
                sbuf.append(" \"")
                sbuf.append(element.prompt)
                sbuf.append("\"")
            }
            if (element.genericNote) {
                sbuf.append(" GENERIC")
            }
            if (element.requestImage) {
                sbuf.append(" IMAGE")
            }
            if (db.tableFlowElementNote.hasNotes(element.id)) {
                sbuf.append(" NOTES")
                sbuf.append(db.tableFlowElementNote.toString(element.id))
            }
            sbuf.append("]")
        }
        return sbuf.toString()
    }

}

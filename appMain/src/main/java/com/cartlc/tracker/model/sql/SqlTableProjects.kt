/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.model.data.DataProject
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.model.table.TableProjects

import com.cartlc.tracker.ui.app.TBApplication

import timber.log.Timber

/**
 * Created by dug on 4/17/17.
 */

class SqlTableProjects(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
): TableProjects {

    companion object {

        private val TABLE_NAME = "list_projects"

        private val KEY_ROWID = "_id"
        private val KEY_NAME = "name"
        private val KEY_SERVER_ID = "server_id"
        private val KEY_DISABLED = "disabled"
        private val KEY_IS_BOOT = "is_boot_strap"
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_NAME)
        sbuf.append(" text not null, ")
        sbuf.append(KEY_SERVER_ID)
        sbuf.append(" integer, ")
        sbuf.append(KEY_DISABLED)
        sbuf.append(" bit default 0, ")
        sbuf.append(KEY_IS_BOOT)
        sbuf.append(" bit default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    fun clear() {
        try {
            dbSql.delete(TABLE_NAME, null, null)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "clear()", "db")
        }
    }

    fun remove(value: String) {
        try {
            val where = "$KEY_NAME=?"
            val whereArgs = arrayOf(value)
            dbSql.delete(TABLE_NAME, where, whereArgs)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "remove(value)", "db")
        }
    }

    fun remove(id: Long) {
        try {
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(java.lang.Long.toString(id))
            dbSql.delete(TABLE_NAME, where, whereArgs)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "remove(id)", "db")
        }
    }

    fun add(list: List<String>) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            for (value in list) {
                values.clear()
                values.put(KEY_NAME, value)
                values.put(KEY_DISABLED, 0)
                dbSql.insert(TABLE_NAME, null, values)
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "add()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun addTest(item: String): Long {
        var id = -1L
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_NAME, item)
            values.put(KEY_IS_BOOT, 1)
            values.put(KEY_DISABLED, 0)
            id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "addTest()", "db")
        } finally {
            dbSql.endTransaction()
        }
        return id
    }

    override fun add(item: String, server_id: Int, disabled: Boolean): Long {
        var id = -1L
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_NAME, item)
            values.put(KEY_SERVER_ID, server_id)
            values.put(KEY_DISABLED, if (disabled) 1 else 0)
            id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "add(item)", "db")
        } finally {
            dbSql.endTransaction()
        }
        return id
    }

    override fun update(project: DataProject): Long {
        val ret: Long = -1
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_NAME, project.name)
            values.put(KEY_SERVER_ID, project.serverId)
            values.put(KEY_DISABLED, if (project.disabled) 1 else 0)
            values.put(KEY_IS_BOOT, if (project.isBootStrap) 1 else 0)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(java.lang.Long.toString(project.id))
            if (dbSql.update(TABLE_NAME, values, where, whereArgs) == 0) {
                project.id = dbSql.insert(TABLE_NAME, null, values)
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "update()", "db")
        } finally {
            dbSql.endTransaction()
        }
        return ret
    }

    fun count(): Int {
        var count = 0
        try {
            val cursor = dbSql.query(TABLE_NAME, null, null, null, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "count()", "db")
        }
        return count
    }


    override fun query(activeOnly: Boolean): List<String> {
        val list = mutableListOf<String>()
        try {
            val columns = arrayOf(KEY_NAME, KEY_DISABLED)
            val orderBy = "$KEY_NAME ASC"
            // Warning: do not use KEY_DISABLED=0 in selection because I failed to include "default 0"
            // for the column definition above on earlier versions. This means the value is actually NULL.
            val cursor = dbSql.query(TABLE_NAME, columns, null, null, null, null, orderBy)
            val idxValue = cursor.getColumnIndex(KEY_NAME)
            val idxDisabled = cursor.getColumnIndex(KEY_DISABLED)
            while (cursor.moveToNext()) {
                val name = cursor.getString(idxValue)
                val disabled = cursor.getShort(idxDisabled).toInt() == 1
                if (!activeOnly || !disabled) {
                    list.add(name)
                }
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "query()", "db")
        }

        // Move other to bottom of the list.
        if (list.contains(TBApplication.OTHER)) {
            list.remove(TBApplication.OTHER)
            list.add(TBApplication.OTHER)
        }
        return list
    }

    override fun queryProjectName(id: Long): String? {
        var projectName: String? = null
        try {
            val columns = arrayOf(KEY_NAME)
            val selection = "$KEY_ROWID=?"
            val selectionArgs = arrayOf(java.lang.Long.toString(id))
            val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
            val idxValue = cursor.getColumnIndex(KEY_NAME)
            if (cursor.moveToFirst()) {
                projectName = cursor.getString(idxValue)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "queryProjectName()", "db")
        }
        return projectName
    }

    override fun queryByServerId(server_id: Int): DataProject? {
        val selection = "$KEY_SERVER_ID=?"
        val selectionArgs = arrayOf(Integer.toString(server_id))
        val list = query(selection, selectionArgs)
        return if (list.size > 0) {
            list[0]
        } else null
    }

    override fun queryById(id: Long): DataProject? {
        val selection = "$KEY_ROWID=?"
        val selectionArgs = arrayOf(java.lang.Long.toString(id))
        val list = query(selection, selectionArgs)
        return if (list.size > 0) {
            list[0]
        } else null
    }

    override fun isDisabled(id: Long): Boolean {
        val project = queryById(id) ?: return true
        return project.disabled
    }

    override fun queryByName(name: String): DataProject? {
        val selection = "$KEY_NAME=?"
        val selectionArgs = arrayOf(name)
        val list = query(selection, selectionArgs)
        return if (list.size > 0) {
            list[0]
        } else null
    }

    internal fun query(selection: String, selectionArgs: Array<String>): List<DataProject> {
        val list = mutableListOf<DataProject>()
        try {
            val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)
            val idxValue = cursor.getColumnIndex(KEY_NAME)
            val idxRowId = cursor.getColumnIndex(KEY_ROWID)
            val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
            val idxDisabled = cursor.getColumnIndex(KEY_DISABLED)
            val idxTest = cursor.getColumnIndex(KEY_IS_BOOT)
            while (cursor.moveToNext()) {
                val project = DataProject()
                project.name = cursor.getString(idxValue)
                project.disabled = cursor.getShort(idxDisabled).toInt() != 0
                project.isBootStrap = cursor.getShort(idxTest).toInt() != 0
                project.serverId = cursor.getInt(idxServerId)
                project.id = cursor.getLong(idxRowId)
                list.add(project)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "query()", "db")
        }
        return list
    }

    override fun queryProjectName(name: String): Long {
        var rowId = -1L
        try {
            val columns = arrayOf(KEY_ROWID)
            val selection = "$KEY_NAME=?"
            val selectionArgs = arrayOf(name)
            val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
            val idxValue = cursor.getColumnIndex(KEY_ROWID)
            if (cursor.moveToFirst()) {
                rowId = cursor.getLong(idxValue)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "queryProjectName()", name)
        }
        return rowId
    }

    override fun removeOrDisable(project: DataProject) {
        if (db.entry.countProjects(project.id) == 0 && db.projectAddressCombo.countProjects(project.id) == 0) {
            // No entries for this, so just remove.
            Timber.i("remove(" + project.id + ", " + project.name + ")")
            remove(project.id)
        } else {
            Timber.i("disable(" + project.id + ", " + project.name + ")")
            project.disabled = true
            update(project)
        }
    }

    fun clearUploaded() {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_SERVER_ID, 0)
            if (dbSql.update(TABLE_NAME, values, null, null) == 0) {
                Timber.e("SqlTableProjects.clearUploaded(): Unable to update entries")
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "clearUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

}

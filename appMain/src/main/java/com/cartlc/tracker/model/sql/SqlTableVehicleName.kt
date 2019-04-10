/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.model.data.DataVehicleName
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.model.table.TableVehicleName

/**
 * Created by dug on 8/31/17.
 */

class SqlTableVehicleName(
        private val dm: DatabaseTable,
        private val dbSql: SQLiteDatabase
) : TableVehicleName {

    companion object {

        private const val TABLE_NAME = "table_vehicle_names"

        private const val KEY_ROWID = "_id"
        private const val KEY_NAME = "name"
        private const val KEY_NUMBER = "number"
    }

    override val vehicleNames: List<String>
        get() = names

    private var names = listOf<String>()
        get() {
            if (field.isEmpty()) {
                field = queryNames()
            }
            return field
        }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_NAME)
        sbuf.append(" varchar(256), ")
        sbuf.append(KEY_NUMBER)
        sbuf.append(" smallint default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    override fun save(name: DataVehicleName): Long {
        val values = ContentValues()
        values.put(KEY_NAME, name.name)
        values.put(KEY_NUMBER, name.number)
        var saved = false
        if (name.id > 0) {
            val where = "${KEY_ROWID}=?"
            val whereArgs = arrayOf(name.id.toString())
            saved = dbSql.update(TABLE_NAME, values, where, whereArgs) != 0
        }
        if (!saved) {
            name.id = dbSql.insert(TABLE_NAME, null, values)
        }
        names = emptyList()
        return name.id
    }

    override fun queryByNumber(number: Int): DataVehicleName? {
        val selection = "${KEY_NUMBER}=?"
        val selectionArgs = arrayOf(number.toString())
        val list = query(selection, selectionArgs)
        if (list.isEmpty()) {
            return null
        }
        return list[0]
    }

    override fun query(): List<DataVehicleName> {
        return query(null, null)
    }

    private fun query(selection: String?, selectionArgs: Array<String>?): List<DataVehicleName> {
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        val idxId = cursor.getColumnIndex(KEY_ROWID)
        val idxName = cursor.getColumnIndex(KEY_NAME)
        val idxNumber = cursor.getColumnIndex(KEY_NUMBER)
        val list = mutableListOf<DataVehicleName>()
        while (cursor.moveToNext()) {
            val name = DataVehicleName()
            name.id = cursor.getLong(idxId)
            name.name = cursor.getString(idxName)
            name.number = cursor.getInt(idxNumber)
            list.add(name)
        }
        cursor.close()
        list.sort()
        return list
    }

    private fun queryNames(): List<String> {
        val list = query()
        val names = mutableListOf<String>()
        for (name in list) {
            names.add(name.vehicleName)
        }
        return names
    }

    override fun remove(name: DataVehicleName) {
        val where = "$KEY_ROWID=?"
        val whereArgs = arrayOf(name.id.toString())
        dbSql.delete(TABLE_NAME, where, whereArgs)
    }
}

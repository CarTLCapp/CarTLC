/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.model.core.data.DataTruck
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableTruck
import com.cartlc.tracker.ui.app.TBApplication

import java.util.ArrayList

import timber.log.Timber

/**
 * Created by dug on 8/31/17.
 */

class SqlTableTruck(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
): TableTruck {

    companion object {

        private const val TABLE_NAME = "table_trucks_v14"

        private const val KEY_ROWID = "_id"
        private const val KEY_TRUCK_NUMBER = "truck_number"
        private const val KEY_LICENSE_PLATE = "license_plate"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_PROJECT_ID = "project_id"
        private const val KEY_COMPANY_NAME = "company_name"
        private const val KEY_HAS_ENTRY = "has_entry"
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_TRUCK_NUMBER)
        sbuf.append(" varchar(128), ")
        sbuf.append(KEY_LICENSE_PLATE)
        sbuf.append(" varchar(128), ")
        sbuf.append(KEY_SERVER_ID)
        sbuf.append(" integer, ")
        sbuf.append(KEY_PROJECT_ID)
        sbuf.append(" int default 0, ")
        sbuf.append(KEY_COMPANY_NAME)
        sbuf.append(" varchar(256), ")
        sbuf.append(KEY_HAS_ENTRY)
        sbuf.append(" bit default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    // Will find exact match of the tableTruck given the parameters. Otherwise will create
    // a new tableTruck with these values.
    // @Returns id of newly saved tableTruck.
    override fun save(truckNumber: String, licensePlate: String, projectId: Long, companyName: String): Long {
        val values = ContentValues()
        val truck: DataTruck
        val trucks = query(truckNumber, licensePlate, projectId, companyName)
        if (trucks.size > 0) {
            truck = trucks[0]
            if (truck.projectNameId == 0L) {
                truck.projectNameId = projectId
                values.put(KEY_PROJECT_ID, projectId)
            }
            if (truck.companyName == null) {
                truck.companyName = companyName
                values.put(KEY_COMPANY_NAME, companyName)
            }
            truck.hasEntry = true
            values.put(KEY_HAS_ENTRY, 1)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(java.lang.Long.toString(truck.id))
            dbSql.update(TABLE_NAME, values, where, whereArgs)
        } else {
            truck = DataTruck()
            truck.truckNumber = truckNumber
            truck.licensePlateNumber = licensePlate
            truck.companyName = companyName
            truck.projectNameId = projectId
            truck.hasEntry = true
            values.put(KEY_TRUCK_NUMBER, truckNumber)
            values.put(KEY_COMPANY_NAME, companyName)
            values.put(KEY_PROJECT_ID, projectId)
            values.put(KEY_LICENSE_PLATE, licensePlate)
            values.put(KEY_HAS_ENTRY, 1)
            truck.id = dbSql.insert(TABLE_NAME, null, values)
        }
        return truck.id
    }

    override fun save(truck: DataTruck): Long {
        try {
            val values = ContentValues()
            values.put(KEY_TRUCK_NUMBER, truck.truckNumber)
            values.put(KEY_LICENSE_PLATE, truck.licensePlateNumber)
            values.put(KEY_PROJECT_ID, truck.projectNameId)
            values.put(KEY_COMPANY_NAME, truck.companyName)
            values.put(KEY_SERVER_ID, truck.serverId)
            values.put(KEY_HAS_ENTRY, if (truck.hasEntry) 1 else 0)
            if (truck.id > 0) {
                val where = "$KEY_ROWID=?"
                val whereArgs = arrayOf(java.lang.Long.toString(truck.id))
                if (dbSql.update(TABLE_NAME, values, where, whereArgs) == 0) {
                    values.put(KEY_ROWID, truck.id)
                    val confirm_id = dbSql.insert(TABLE_NAME, null, values)
                    if (confirm_id != truck.id) {
                        Timber.e("Did not transfer truck properly for ID " + truck.id + "...got back " + confirm_id)
                    }
                }
            } else {
                truck.id = dbSql.insert(TABLE_NAME, null, values)
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableTruck::class.java, "save(truck)", "db")
        }
        return truck.id
    }

    override fun query(id: Long): DataTruck? {
        val selection = "$KEY_ROWID=?"
        val selectionArgs = arrayOf(java.lang.Long.toString(id))
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        var truck: DataTruck? = DataTruck()
        if (cursor.moveToNext()) {
            val idxId = cursor.getColumnIndex(KEY_ROWID)
            val idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER)
            val idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE)
            val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
            val idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID)
            val idxCompanyName = cursor.getColumnIndex(KEY_COMPANY_NAME)
            val idxHasEntry = cursor.getColumnIndex(KEY_HAS_ENTRY)
            truck!!.id = cursor.getLong(idxId)
            truck.truckNumber = cursor.getString(idxTruckNumber)
            truck.licensePlateNumber = cursor.getString(idxLicensePlate)
            truck.serverId = cursor.getLong(idxServerId)
            truck.projectNameId = cursor.getLong(idxProjectId)
            truck.companyName = cursor.getString(idxCompanyName)
            truck.hasEntry = cursor.getShort(idxHasEntry).toInt() != 0
        } else {
            truck = null
        }
        cursor.close()
        return truck
    }

    override fun queryByTruckNumber(truck_number: Int): List<DataTruck> {
        val selection = "$KEY_TRUCK_NUMBER=?"
        val selectionArgs = arrayOf(Integer.toString(truck_number))
        return query(selection, selectionArgs)
    }

    override fun queryByLicensePlate(license_plate: String): List<DataTruck> {
        val selection = "$KEY_TRUCK_NUMBER=?"
        val selectionArgs = arrayOf(license_plate)
        return query(selection, selectionArgs)
    }

    fun query(truck_number: String?, license_plate: String?, projectId: Long, companyName: String?): List<DataTruck> {
        val selection = StringBuffer()
        val selectionArgs = ArrayList<String>()

        if (truck_number != null && truck_number.trim { it <= ' ' }.length > 0) {
            selection.append(KEY_TRUCK_NUMBER)
            selection.append("=?")
            selectionArgs.add(truck_number)
        }
        if (license_plate != null && license_plate.trim { it <= ' ' }.length > 0) {
            if (selection.length > 0) {
                selection.append(" AND ")
            }
            selection.append(KEY_LICENSE_PLATE)
            selection.append("=?")
            selectionArgs.add(license_plate)
        }
        if (projectId > 0) {
            if (selection.length > 0) {
                selection.append(" AND ")
            }
            selection.append("(")
            selection.append(KEY_PROJECT_ID)
            selection.append("=? OR ")
            selection.append(KEY_PROJECT_ID)
            selection.append("=0)")
            selectionArgs.add(java.lang.Long.toString(projectId))
        }
        if (companyName != null && companyName.trim { it <= ' ' }.length > 0) {
            if (selection.length > 0) {
                selection.append(" AND ")
            }
            selection.append("(")
            selection.append(KEY_COMPANY_NAME)
            selection.append("=? OR ")
            selection.append(KEY_COMPANY_NAME)
            selection.append(" IS NULL)")
            selectionArgs.add(companyName)
        }
        return query(selection.toString(), selectionArgs.toTypedArray())
    }

    override fun query(selection: String?, selectionArgs: Array<String>?): List<DataTruck> {
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        val idxId = cursor.getColumnIndex(KEY_ROWID)
        val idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER)
        val idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE)
        val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
        val idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID)
        val idxCompanyName = cursor.getColumnIndex(KEY_COMPANY_NAME)
        val idxHasEntry = cursor.getColumnIndex(KEY_HAS_ENTRY)
        val list = ArrayList<DataTruck>()
        while (cursor.moveToNext()) {
            val truck = DataTruck()
            truck.id = cursor.getLong(idxId)
            truck.truckNumber = cursor.getString(idxTruckNumber)
            truck.licensePlateNumber = cursor.getString(idxLicensePlate)
            truck.serverId = cursor.getLong(idxServerId)
            truck.projectNameId = cursor.getLong(idxProjectId)
            truck.companyName = cursor.getString(idxCompanyName)
            truck.hasEntry = cursor.getShort(idxHasEntry).toInt() != 0
            list.add(truck)
        }
        cursor.close()
        return list
    }

    override fun queryStrings(curGroup: DataProjectAddressCombo?): List<String> {
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        if (curGroup != null) {
            val sbuf = StringBuffer()
            sbuf.append("(")
            sbuf.append(KEY_PROJECT_ID)
            sbuf.append("=? OR ")
            sbuf.append(KEY_PROJECT_ID)
            sbuf.append("=0)")
            curGroup.companyName?.let { companyName ->
                sbuf.append(" AND ")
                sbuf.append("(")
                sbuf.append(KEY_COMPANY_NAME)
                sbuf.append("=? OR ")
                sbuf.append(KEY_COMPANY_NAME)
                sbuf.append(" IS NULL)")
                sbuf.append(" AND ")
                sbuf.append("(")
                sbuf.append(KEY_HAS_ENTRY)
                sbuf.append("=0)")
                selection = sbuf.toString()
                selectionArgs = arrayOf<String>(java.lang.Long.toString(curGroup.projectNameId), companyName)
            }
        }
        val trucks = query(selection, selectionArgs).toMutableList()
        trucks.sort()
        val list = ArrayList<String>()
        for (truck in trucks) {
            list.add(truck.toString())
        }
        return list
    }

    override fun queryByServerId(id: Long): DataTruck? {
        val selection = "$KEY_SERVER_ID=?"
        val selectionArgs = arrayOf(java.lang.Long.toString(id))
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        var truck: DataTruck? = DataTruck()
        if (cursor.moveToNext()) {
            val idxId = cursor.getColumnIndex(KEY_ROWID)
            val idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER)
            val idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE)
            val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
            val idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID)
            val idxCompanyName = cursor.getColumnIndex(KEY_COMPANY_NAME)
            val idxHasEntry = cursor.getColumnIndex(KEY_HAS_ENTRY)
            truck!!.id = cursor.getLong(idxId)
            truck.truckNumber = cursor.getString(idxTruckNumber)
            truck.licensePlateNumber = cursor.getString(idxLicensePlate)
            truck.serverId = cursor.getLong(idxServerId)
            truck.projectNameId = cursor.getLong(idxProjectId)
            truck.companyName = cursor.getString(idxCompanyName)
            truck.hasEntry = cursor.getShort(idxHasEntry).toInt() != 0
        } else {
            truck = null
        }
        cursor.close()
        return truck
    }

    internal fun remove(id: Long) {
        val where = "$KEY_ROWID=?"
        val whereArgs = arrayOf(java.lang.Long.toString(id))
        dbSql.delete(TABLE_NAME, where, whereArgs)
    }

    override fun removeIfUnused(truck: DataTruck) {
        if (db.tableEntry.countTrucks(truck.id) == 0) {
            Timber.i("remove(" + truck.toString() + ")")
            remove(truck.id)
        }
    }


}

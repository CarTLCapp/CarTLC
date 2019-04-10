/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.sql

import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.model.data.DataTruck
import com.cartlc.tracker.model.table.DatabaseTable

import com.cartlc.tracker.ui.app.TBApplication

/**
 * Created by dug on 8/31/17.
 */

class SqlTableTruckV13(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
) {

    companion object {

        private const val TABLE_NAME = "table_trucks"

        private const val KEY_ROWID = "_id"
        private const val KEY_TRUCK_NUMBER = "truck_number"
        private const val KEY_LICENSE_PLATE = "license_plate"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_PROJECT_ID = "project_id"
        private const val KEY_COMPANY_NAME = "company_name"

        lateinit var instance: SqlTableTruckV13
            internal set

        fun Init(db: DatabaseTable, dbSql: SQLiteDatabase) {
            SqlTableTruckV13(db, dbSql)
        }
    }

    init {
        instance = this
    }

    fun upgrade11() {
        try {
            dbSql.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $KEY_PROJECT_ID int default 0")
            dbSql.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $KEY_COMPANY_NAME varchar(256)")
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableTruck::class.java, "upgrade11()", "db")
        }
    }

    fun transfer() {
        val cursor = dbSql.query(TABLE_NAME, null, null, null, null, null, null, null)
        var truck: DataTruck
        while (cursor.moveToNext()) {
            val idxId = cursor.getColumnIndex(KEY_ROWID)
            val idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER)
            val idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE)
            val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
            val idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID)
            val idxCompanyName = cursor.getColumnIndex(KEY_COMPANY_NAME)
            truck = DataTruck()
            truck.id = cursor.getLong(idxId)
            truck.truckNumber = Integer.toString(cursor.getInt(idxTruckNumber))
            truck.licensePlateNumber = cursor.getString(idxLicensePlate)
            truck.serverId = cursor.getLong(idxServerId)
            truck.projectNameId = cursor.getLong(idxProjectId)
            truck.companyName = cursor.getString(idxCompanyName)
            truck.hasEntry = db.tableEntry.countTrucks(truck.id) > 0
            db.tableTruck.save(truck)
        }
        cursor.close()
        dbSql.delete(TABLE_NAME, null, null)
    }


}

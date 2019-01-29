/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.sql

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.VisibleForTesting

import com.cartlc.tracker.model.table.*
import com.cartlc.tracker.ui.app.TBApplication

import timber.log.Timber

/**
 * Created by dug on 4/17/17.
 */
@VisibleForTesting
open class DatabaseManager(private val ctx: Context) : DatabaseTable {

    companion object {
        internal val DATABASE_NAME = "cartcl.db"
        internal val DATABASE_VERSION = 17
    }

    private var dbHelper: DatabaseHelper
    private var dbSql: SQLiteDatabase

    init {
        dbHelper = DatabaseHelper(ctx, this)
        dbSql = dbHelper.writableDatabase
    }

    internal class DatabaseHelper(
            ctx: Context,
            private val dm: DatabaseManager
    ) : SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {

        lateinit var tableAddress: SqlTableAddress
        lateinit var tableCollectionNoteEntry: SqlTableCollectionNoteEntry
        lateinit var tableCollectionNoteProject: SqlTableCollectionNoteProject
        lateinit var tableCollectionEquipmentEntry: SqlTableCollectionEquipmentEntry
        lateinit var tableCollectionEquipmentProject: SqlTableCollectionEquipmentProject
        lateinit var tableEntry: SqlTableEntry
        lateinit var tableEquipment: SqlTableEquipment
        lateinit var tableNote: SqlTableNote
        lateinit var tablePictureCollection: SqlTablePictureCollection
        lateinit var tableProjects: SqlTableProjects
        lateinit var tableProjectAddressCombo: SqlTableProjectAddressCombo
        lateinit var tableTruck: SqlTableTruck
        lateinit var tableCrash: SqlTableCrash
        lateinit var tableZipCode: SqlTableZipCode
        lateinit var tableString: SqlTableString
        lateinit var tableVehicle: SqlTableVehicle
        lateinit var tableVehicleName: SqlTableVehicleName

        override fun onCreate(db: SQLiteDatabase) {
            init(db)
            try {
                tableAddress.create()
                tableEntry.create()
                tableEquipment.create()
                tableCollectionEquipmentEntry.create()
                tableCollectionEquipmentProject.create()
                tableNote.create()
                tableCollectionNoteEntry.create()
                tableCollectionNoteProject.create()
                tablePictureCollection.create()
                tableProjectAddressCombo.create()
                tableProjects.create()
                tableCrash.create()
                tableZipCode.create()
                tableTruck.create()
                tableString.create()
                tableVehicle.create()
                tableVehicleName.create()
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }

        fun init(db: SQLiteDatabase) {
            tableAddress = SqlTableAddress(dm, db)
            tableEntry = SqlTableEntry(dm, db)
            tableEquipment = SqlTableEquipment(dm, db)
            tableCollectionEquipmentEntry = SqlTableCollectionEquipmentEntry(dm, db)
            tableCollectionEquipmentProject = SqlTableCollectionEquipmentProject(dm, db)
            tableNote = SqlTableNote(dm, db)
            tableCollectionNoteEntry = SqlTableCollectionNoteEntry(dm, db)
            tableCollectionNoteProject = SqlTableCollectionNoteProject(dm, db)
            tablePictureCollection = SqlTablePictureCollection(db)
            tableProjectAddressCombo = SqlTableProjectAddressCombo(dm, db)
            tableProjects = SqlTableProjects(dm, db)
            tableCrash = SqlTableCrash(dm, db)
            tableZipCode = SqlTableZipCode(db)
            tableTruck = SqlTableTruck(dm, db)
            tableString = SqlTableString(db)
            tableVehicle = SqlTableVehicle(dm, db)
            tableVehicleName = SqlTableVehicleName(dm, db)
            SqlTableTruckV13.Init(dm, db)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            init(db)
            if (oldVersion == 1 || oldVersion == 2) {
                tableCrash.create()
                SqlTablePictureCollection.upgrade3(dm, db)
                tableZipCode.create()
                SqlTableNote.upgrade3(db)
                tableTruck.create()
                tableEntry.upgrade3()
            } else if (oldVersion <= 9) {
                SqlTableCrash.upgrade10(dm, db)
                SqlTableTruckV13.instance.upgrade11()
                tableTruck.create()
                SqlTableTruckV13.instance.transfer()
            } else if (oldVersion <= 12) {
                SqlTableTruckV13.instance.upgrade11()
                tableTruck.create()
                SqlTableTruckV13.instance.transfer()
                tableEntry.upgrade11()
            } else if (oldVersion <= 15) {
                tableTruck.create()
                SqlTableTruckV13.instance.transfer()
            } else if (oldVersion <= 16) {
                tableString.create()
                tableVehicle.create()
                tableVehicleName.create()
            }
        }

        override fun onOpen(db: SQLiteDatabase) {
            init(db)
        }

        fun clearUploaded() {
            tableEntry.clearUploaded()
            tableEquipment.clearUploaded()
            tableNote.clearUploaded()
            tablePictureCollection.clearUploaded()
            tableProjects.clearUploaded()
            tableAddress.clearUploaded()
            tableCollectionEquipmentEntry.clearUploaded()
            tableCollectionEquipmentProject.clearUploaded()
            tableCollectionNoteProject.clearUploaded()
            tableCrash.clearUploaded()
            tableVehicle.clearUploaded()
        }
    }

    override fun clearUploaded() {
        dbHelper.clearUploaded()
    }

    override val tableAddress: TableAddress
        get() = dbHelper.tableAddress

    override val tableProjects: TableProjects
        get() = dbHelper.tableProjects

    override val tableEquipment: TableEquipment
        get() = dbHelper.tableEquipment

    override val tableNote: TableNote
        get() = dbHelper.tableNote

    override val tableTruck: TableTruck
        get() = dbHelper.tableTruck

    override val tableCollectionNoteEntry: TableCollectionNoteEntry
        get() = dbHelper.tableCollectionNoteEntry

    override val tableCollectionNoteProject: TableCollectionNoteProject
        get() = dbHelper.tableCollectionNoteProject

    override val tableEntry: TableEntry
        get() = dbHelper.tableEntry

    override val tableCollectionEquipmentEntry: TableCollectionEquipmentEntry
        get() = dbHelper.tableCollectionEquipmentEntry

    override val tableCollectionEquipmentProject: TableCollectionEquipmentProject
        get() = dbHelper.tableCollectionEquipmentProject

    override val tableCrash: TableCrash
        get() = dbHelper.tableCrash

    override val tablePictureCollection: TablePictureCollection
        get() = dbHelper.tablePictureCollection

    override val tableProjectAddressCombo: TableProjectAddressCombo
        get() = dbHelper.tableProjectAddressCombo

    override val tableZipCode: TableZipCode
        get() = dbHelper.tableZipCode

    override val appVersion: String
        get() = (ctx.applicationContext as TBApplication).version

    override val tableVehicle: TableVehicle
        get() = dbHelper.tableVehicle

    override val tableVehicleName: TableVehicleName
        get() = dbHelper.tableVehicleName

    override val tableString: TableString
        get() = dbHelper.tableString

    override fun reportError(ex: Exception, claz: Class<*>, function: String, type: String): String =
            TBApplication.ReportError(ex, claz, function, type)

    override fun reportDebugMessage(msg: String) =
            tableCrash.info(msg)
}

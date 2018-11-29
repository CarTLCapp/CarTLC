/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.amock

import com.cartlc.tracker.model.data.DataAddress
import com.cartlc.tracker.model.table.DatabaseTable

import javax.inject.Inject

/**
 * Created by dug on 5/9/17.
 */

class BootstrapData {

    internal val ADDRESSES = arrayOf(
            DataAddress("Alamo Concrete"),
            DataAddress("CW Roberts"),
            DataAddress("Central Concrete"),
            DataAddress("IMI"),
            DataAddress("Ozinga"),
            DataAddress("Wingra Ready Mix"),
            DataAddress("Point Ready Mix"))

    @Inject
    lateinit var db: DatabaseTable

//    fun init() {
//        DaggerDatabaseTableComponent.builder().build().inject(this)
//        if (db.tableAddress.count() == 0) {
//            db.tableAddress.add(Arrays.asList(*ADDRESSES))
//        }
//        if (SqlTableProjects.instance.count() == 0) {
//            AddCollections()
//            AddNotes()
//        }
//    }
//
//    internal fun AddCollections() {
//        SqlTableCollectionEquipmentProject.instance.addByName("Five Cubits", Arrays.asList(*arrayOf("OBC", "OBC Bracket", "RDT", "VMX", "Tablet", "Charging Converter", "6 pin Canbus Cable", "9 pin Canbus Cable", "Green Canbus Cable", "ODB II Canbus Cable", "GDS Cup", "External Antenna", "Internal Antenna", "External Speaker", "Microphone", "Tablet", "Repair Work", "Speaker Box", "Other")))
//        SqlTableCollectionEquipmentProject.instance.addByName("Digital Fleet", Arrays.asList(*arrayOf("Antenna", "Tablet", "Modem", "JBox", "Canbus", "Ram Mount/Cradle", "Charging Converter", "Repair Work", "Uninstall", "Other")))
//        SqlTableCollectionEquipmentProject.instance.addByName("Smart Witness", Arrays.asList(*arrayOf("KP1S", "CPI", "SVC 1080", "Modem", "Driver Facing Camera", "Back Up Camera", "Side Camera 1", "Side Camera 2", "Other")))
//        SqlTableCollectionEquipmentProject.instance.addByName("Fed Ex", Arrays.asList(*arrayOf("KP1S", "SVA30", "Modem", "Mobileye", "Backup Sensors", "Other")))
//        SqlTableCollectionEquipmentProject.instance.addByName("Verifi", Arrays.asList(*arrayOf("Other")))
//        SqlTableProjects.instance.addTest(TBApplication.OTHER)
//    }
//
//    internal fun AddNotes() {
//        SqlTableCollectionNoteProject.instance.addByName("Five Cubits", Arrays.asList(*arrayOf(DataNote("Serial #", DataNote.Type.ALPHANUMERIC), DataNote("IMEI #", DataNote.Type.NUMERIC), DataNote("Other", DataNote.Type.MULTILINE))))
//        SqlTableCollectionNoteProject.instance.addByName("Digital Fleet", Arrays.asList(*arrayOf(DataNote("Serial #"), DataNote("IMEI #"), DataNote("Other"))))
//        SqlTableCollectionNoteProject.instance.addByName("Smart Witness", Arrays.asList(*arrayOf(DataNote("Serial #"), DataNote("IMEI #"), DataNote("Sim #", DataNote.Type.NUMERIC_WITH_SPACES), DataNote("DRID #", DataNote.Type.TEXT), DataNote("Other"))))
//        SqlTableCollectionNoteProject.instance.addByName("Fed Ex", Arrays.asList(*arrayOf(DataNote("Serial #"), DataNote("IMEI #"), DataNote("Sim #"), DataNote("DRID #"), DataNote("Mobileye", DataNote.Type.TEXT), DataNote("Other"))))
//        SqlTableCollectionNoteProject.instance.addByName("Verifi", Arrays.asList(*arrayOf(DataNote("Serial #"), DataNote("IMEI #"), DataNote("Other"))))
//        SqlTableCollectionNoteProject.instance.addByName("Other", Arrays.asList(*arrayOf(DataNote("Serial #"), DataNote("IMEI #"), DataNote("Other"))))
//    }

}

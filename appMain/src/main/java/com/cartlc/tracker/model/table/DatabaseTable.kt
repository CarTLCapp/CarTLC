package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.pref.PrefHelper

interface DatabaseTable {
    val tableAddress: TableAddress
    val tableCollectionNoteEntry: TableCollectionNoteEntry
    val tableCollectionNoteProject: TableCollectionNoteProject
    val tableCollectionEquipmentEntry: TableCollectionEquipmentEntry
    val tableCollectionEquipmentProject: TableCollectionEquipmentProject
    val tableCrash: TableCrash
    val tableEntry: TableEntry
    val tableEquipment: TableEquipment
    val tableNote: TableNote
    val tablePictureCollection: TablePictureCollection
    val tableProjects: TableProjects
    val tableProjectAddressCombo: TableProjectAddressCombo
    val tableTruck: TableTruck
    val tableZipCode: TableZipCode
    val tableString: TableString
    val tableVehicle: TableVehicle
    val tableVehicleName: TableVehicleName
    val appVersion: String
    fun reportError(ex: Exception, claz: Class<*>, function: String, type: String): String
    fun clearUploaded()
}
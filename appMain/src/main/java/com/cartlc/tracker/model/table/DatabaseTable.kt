package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.pref.PrefHelper

interface DatabaseTable {
    val address: TableAddress
    val collectionNoteEntry: TableCollectionNoteEntry
    val collectionNoteProject: TableCollectionNoteProject
    val collectionEquipmentEntry: TableCollectionEquipmentEntry
    val collectionEquipmentProject: TableCollectionEquipmentProject
    val crash: TableCrash
    val entry: TableEntry
    val equipment: TableEquipment
    val note: TableNote
    val pictureCollection: TablePictureCollection
    val projects: TableProjects
    val projectAddressCombo: TableProjectAddressCombo
    val truck: TableTruck
    val zipCode: TableZipCode
    val appVersion: String
    fun reportError(ex: Exception, claz: Class<*>, function: String, type: String): String
}
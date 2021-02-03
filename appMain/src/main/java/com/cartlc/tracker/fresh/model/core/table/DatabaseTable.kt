package com.cartlc.tracker.fresh.model.core.table

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
    val tablePicture: TablePicture
    val tableProjects: TableProjects
    val tableProjectAddressCombo: TableProjectAddressCombo
    val tableTruck: TableTruck
    val tableZipCode: TableZipCode
    val tableString: TableString
    val tableVehicle: TableVehicle
    val tableVehicleName: TableVehicleName
    val tableFlow: TableFlow
    val tableFlowElement: TableFlowElement
    val tableFlowElementNote: TableFlowElementNote
    val tableDaar: TableDaar
    val appVersion: String
    val noteHelper: NoteHelper
    fun reportError(ex: Exception, claz: Class<*>, function: String, type: String): String
    fun reportDebugMessage(msg: String)
    fun clearUploaded()
}
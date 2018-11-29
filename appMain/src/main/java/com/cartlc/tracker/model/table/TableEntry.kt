package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataEntry
import com.cartlc.tracker.model.data.DataProjectAddressCombo
import com.cartlc.tracker.model.sql.SqlTableEntry

interface TableEntry {
    fun add(entry: DataEntry): Boolean
    fun countAddresses(addressId: Long): Int
    fun countTrucks(truckId: Long): Int
    fun countProjects(projectId: Long): Int
    fun countProjectAddressCombo(comboId: Long): SqlTableEntry.Count
    fun query(): List<DataEntry>
    fun query(id: Long): DataEntry?
    fun queryForProjectAddressCombo(id: Long): List<DataEntry>
    fun queryPendingDataToUploadToMaster(): List<DataEntry>
    fun queryPendingPicturesToUpload(): List<DataEntry>
    fun queryServerIds(): List<DataEntry>
    fun remove(entry: DataEntry)
    fun reUploadEntries(combo: DataProjectAddressCombo): Int
    fun saveProjectAddressCombo(entry: DataEntry)
    fun saveUploaded(entry: DataEntry)
}
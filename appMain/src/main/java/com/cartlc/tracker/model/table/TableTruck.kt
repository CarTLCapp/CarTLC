package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataProjectAddressCombo
import com.cartlc.tracker.model.data.DataTruck

interface TableTruck {
    fun query(id: Long): DataTruck?
    fun query(selection: String? = null, selectionArgs: Array<String>? = null): List<DataTruck>
    fun queryByLicensePlate(license_plate: String): List<DataTruck>
    fun queryByTruckNumber(truck_number: Int): List<DataTruck>
    fun queryByServerId(id: Long): DataTruck?
    fun queryStrings(curGroup: DataProjectAddressCombo?): List<String>
    fun removeIfUnused(truck: DataTruck)
    fun save(truck: DataTruck): Long
    fun save(truckNumber: String, licensePlate: String, projectId: Long, companyName: String): Long
}
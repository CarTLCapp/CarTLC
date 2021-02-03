package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.model.core.data.DataTruck

interface TableTruck {
    fun clearAll()
    fun query(id: Long): DataTruck?
    fun query(selection: String? = null, selectionArgs: Array<String>? = null): List<DataTruck>
    fun queryByLicensePlate(license_plate: String): List<DataTruck>
    fun queryByTruckNumber(truck_number: Int): List<DataTruck>
    fun queryByServerId(id: Long): DataTruck?
    fun queryStrings(curGroup: DataProjectAddressCombo?): List<String>
    fun removeIfUnused(truck: DataTruck)
    fun save(truck: DataTruck): Long
    fun save(truckNumberValue: String,
             truckNumberPictureId: Int,
             truckDamageExists: Boolean,
             truckDamagePictureId: Int,
             truckDamageValue: String,
             projectId: Long,
             companyName: String): Long
}
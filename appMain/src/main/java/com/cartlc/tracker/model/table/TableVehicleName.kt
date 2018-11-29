package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataVehicleName

interface TableVehicleName {
    val vehicleNames: List<String>
    fun query(): List<DataVehicleName>
    fun queryByNumber(number: Int): DataVehicleName?
    fun remove(name: DataVehicleName)
    fun save(name: DataVehicleName): Long
}
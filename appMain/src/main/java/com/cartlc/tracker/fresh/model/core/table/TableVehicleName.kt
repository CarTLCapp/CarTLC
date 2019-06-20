package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataVehicleName

interface TableVehicleName {
    val vehicleNames: List<String>
    fun query(): List<DataVehicleName>
    fun queryByNumber(number: Int): DataVehicleName?
    fun remove(name: DataVehicleName)
    fun save(name: DataVehicleName): Long
}
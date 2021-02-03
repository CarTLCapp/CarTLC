package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataVehicle

interface TableVehicle {
    fun clearAll()
    fun query(id: Long): DataVehicle?
    fun queryByServerId(id: Long): DataVehicle?
    fun queryNotUploaded(): List<DataVehicle>
    fun save(vehicle: DataVehicle): Long
    fun saveUploaded(vehicle: DataVehicle)
}
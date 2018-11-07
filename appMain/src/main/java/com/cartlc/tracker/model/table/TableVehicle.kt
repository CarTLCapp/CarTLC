package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataVehicle

interface TableVehicle {
    fun query(id: Long): DataVehicle?
    fun queryByServerId(id: Long): DataVehicle?
    fun queryNotUploaded(): List<DataVehicle>
    fun save(vehicle: DataVehicle): Long
}
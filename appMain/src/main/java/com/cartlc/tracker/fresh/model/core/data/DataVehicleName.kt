/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.data

/**
 * Created by dug on 8/31/17.
 */
class DataVehicleName : Comparable<DataVehicleName> {

    var id: Long = 0
    var name: String = ""
    var number: Int = 0

    val vehicleName: String
        get() = name + " #" + Integer.toString(number)

    override fun compareTo(other: DataVehicleName): Int {
        return number - other.number
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other is DataVehicleName) {
            return other.name.equals(name) && other.number == number
        }
        return false
    }

    override fun toString(): String {
        return "DataVehicleName(id=$id, name='$name', number=$number)"
    }

}

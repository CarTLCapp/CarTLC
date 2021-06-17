/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.data

/**
 * Created by dug on 5/12/17.
 */

class DataEquipment : Comparable<DataEquipment> {
    var id: Long = 0
    val name: String
    var serverId: Long = 0
    var isChecked: Boolean = false
    var isLocal: Boolean = false
    var isBootStrap: Boolean = false
    var disabled: Boolean = false

    constructor(id: Long, name: String, isChecked: Boolean, isLocal: Boolean) {
        this.id = id
        this.name = name
        this.isChecked = isChecked
        this.isLocal = isLocal
    }

    constructor(name: String, server_id: Int, isDisabled: Boolean) {
        this.name = name
        this.serverId = server_id.toLong()
        this.disabled = isDisabled
    }

    // region Object

    override fun toString(): String {
        val sbuf = StringBuilder()
        sbuf.append("DataEquipment(")
        sbuf.append(name)
        sbuf.append(", serverId=")
        sbuf.append(serverId)
        sbuf.append(", checked=")
        sbuf.append(isChecked)
        sbuf.append(", local=")
        sbuf.append(isLocal)
        sbuf.append(", test=")
        sbuf.append(isBootStrap)
        sbuf.append(", disabled=")
        sbuf.append(disabled)
        sbuf.append(")")
        return sbuf.toString()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is DataEquipment) {
            return name == other.name && disabled == other.disabled && serverId == other.serverId
        } else super.equals(other)
    }

    override fun compareTo(other: DataEquipment): Int {
        return name.compareTo(other.name)
    }

    // endregion Object
}

/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.data

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

    constructor(name: String, server_id: Int) {
        this.name = name
        this.serverId = server_id.toLong()
    }

    override fun toString(): String {
        val sbuf = StringBuilder()
        sbuf.append("NAME=")
        sbuf.append(name)
        sbuf.append(", checked=")
        sbuf.append(isChecked)
        sbuf.append(", local=")
        sbuf.append(isLocal)
        sbuf.append(", test=")
        sbuf.append(isBootStrap)
        return sbuf.toString()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is DataEquipment) {
            equals(other as DataEquipment?)
        } else super.equals(other)
    }

    override fun compareTo(other: DataEquipment): Int {
        return name.compareTo(other.name)
    }

    fun equals(item: DataEquipment): Boolean {
        return name == item.name
    }
}

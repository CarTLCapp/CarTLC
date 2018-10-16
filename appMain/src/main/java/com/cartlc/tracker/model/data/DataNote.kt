/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.data

import java.util.Locale

/**
 * Created by dug on 5/16/17.
 */

class DataNote {

    var id: Long = 0
    var name: String = ""
    var value: String? = null
    var type: Type? = null
    var num_digits: Short = 0
    var serverId: Int = 0
    var isBootStrap: Boolean = false

    enum class Type {
        TEXT,
        NUMERIC,
        ALPHANUMERIC,
        NUMERIC_WITH_SPACES,
        MULTILINE;


        companion object {

            fun from(ord: Int): Type {
                for (value in values()) {
                    if (value.ordinal == ord) {
                        return value
                    }
                }
                return Type.TEXT
            }

            fun from(item: String): Type {
                val match = item.toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                for (value in values()) {
                    if (value.toString().toLowerCase(Locale.getDefault()) == match) {
                        return value
                    }
                }
                return Type.TEXT
            }
        }
    }

    constructor() {}

    constructor(name: String) {
        this.name = name
    }

    constructor(name: String, type: Type) {
        this.name = name
        this.type = type
        this.isBootStrap = true
    }

    constructor(name: String, type: Type, num_digits: Short, server_id: Int) {
        this.name = name
        this.type = type
        this.num_digits = num_digits
        this.serverId = server_id
    }

    override fun toString(): String {
        val sbuf = StringBuilder()
        sbuf.append("ID=")
        sbuf.append(id)
        sbuf.append(", NAME=")
        sbuf.append(name)
        sbuf.append(", VALUE=")
        sbuf.append(value)
        if (type != null) {
            sbuf.append(", TYPE=")
            sbuf.append(type!!.toString())
        }
        sbuf.append(", #DIGITS=")
        sbuf.append(num_digits.toInt())
        sbuf.append(" SID=")
        sbuf.append(serverId)
        return sbuf.toString()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is DataNote) {
            return (name == other.name
                    && type?.ordinal == other.type?.ordinal
                    && num_digits == other.num_digits)
        } else super.equals(other)
    }

    fun valueLength(): Int {
        return if (value == null) {
            0
        } else value!!.length
    }

}

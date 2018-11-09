/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.data

/**
 * Created by dug on 8/31/17.
 */
class DataString {
    var id: Long = 0
    var serverId: Long = 0
    var value: String = ""

    override fun toString(): String {
        return "ID=${id}, SERVER_ID=${serverId}, VALUE=${value}"
    }
}

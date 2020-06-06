/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.data

/**
 * Created by dug on 8/31/17.
 */
class DataString {
    var id: Long = 0
    var serverId: Long = 0
    var value: String = ""

    constructor()

    constructor(id: Long, text: String) {
        this.id = id
        this.value = text
    }

    constructor(id: Long, text: String, serverId: Long) {
        this.id = id
        this.value = text
        this.serverId = serverId
    }

    override fun toString(): String {
        return "ID=${id}, SERVER_ID=${serverId}, VALUE=${value}"
    }
}

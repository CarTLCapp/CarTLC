/*
 * *
 *   * Copyright 2019, FleetTLC. All rights reserved
 *
 */

package com.cartlc.tracker.fresh.model.core.data

class DataFlow(
        var id: Long = 0,
        var serverId: Int = 0,
        var subProjectId: Long = 0
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataFlow

        if (subProjectId != other.subProjectId) return false

        return true
    }

    override fun toString(): String {
        return "DataFlow(id=$id, serverId=$serverId, subProjectId=$subProjectId)"
    }

    override fun hashCode(): Int {
        return subProjectId.hashCode()
    }

}
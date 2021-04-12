/*
 * *
 *   * Copyright 2019, FleetTLC. All rights reserved
 *
 */

package com.cartlc.tracker.fresh.model.core.data

class DataFlow(
        var id: Long = 0,
        var serverId: Int = 0,
        var subProjectId: Long = 0,
        var hasFlagTruckNumber: Boolean = false,
        var hasFlagTruckDamage: Boolean = false
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataFlow

        if (subProjectId != other.subProjectId) return false
        if (hasFlagTruckNumber != other.hasFlagTruckNumber) return false
        if (hasFlagTruckDamage != other.hasFlagTruckDamage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = subProjectId.hashCode()
        result = 31 * result + hasFlagTruckNumber.hashCode()
        result = 31 * result + hasFlagTruckDamage.hashCode()
        return result
    }

    override fun toString(): String {
        return "DataFlow(id=$id, serverId=$serverId, subProjectId=$subProjectId, hasFlagTruckNumber=$hasFlagTruckNumber, hasFlagTruckDamage=$hasFlagTruckDamage)"
    }

}
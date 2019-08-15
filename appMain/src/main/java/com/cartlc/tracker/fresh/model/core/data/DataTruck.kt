/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.data

import com.cartlc.tracker.fresh.model.core.table.DatabaseTable

/**
 * Created by dug on 8/31/17.
 */

class DataTruck : Comparable<DataTruck> {

    var id: Long = 0
    var serverId: Long = 0
    var truckNumberValue: String? = null
    var truckNumberPictureId: Int = 0
    var truckHasDamage: Boolean = false
    var truckDamagePictureId: Int = 0
    var projectNameId: Long = 0
    var companyName: String? = null
    var hasEntry: Boolean = false

    constructor()

    constructor(id: Long, truckNumber: String, projectNameId: Long, companyName: String) {
        this.id = id
        this.truckNumberValue = truckNumber
        this.projectNameId = projectNameId
        this.companyName = companyName
    }

    override fun toString(): String {
        return truckNumberValue ?: ""
    }

    fun toLongString(db: DatabaseTable): String {
        val sbuf = StringBuilder()
        sbuf.append(id)
        if (serverId != 0L) {
            sbuf.append(" [")
            sbuf.append(serverId)
            sbuf.append("]")
        }
        if (truckNumberValue != null) {
            sbuf.append(", ")
            sbuf.append(truckNumberValue)
        }
        if (projectNameId > 0) {
            if (sbuf.isNotEmpty()) {
                sbuf.append(", ")
            }
            sbuf.append(db.tableProjects.queryProjectName(projectNameId))
            sbuf.append("(")
            sbuf.append(projectNameId)
            sbuf.append(")")
        }
        if (companyName != null) {
            if (sbuf.isNotEmpty()) {
                sbuf.append(", ")
            }
            sbuf.append(companyName)
        }
        if (hasEntry) {
            sbuf.append(", HASENTRY")
        }
        return sbuf.toString()
    }

    override fun compareTo(other: DataTruck): Int {
        if (truckNumberValue == null) {
            return if (other.truckNumberValue == null) {
                0
            } else -1
        } else if (other.truckNumberValue == null) {
            return 1
        }
        return truckNumberValue!!.compareTo(other.truckNumberValue!!)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataTruck

        if (truckNumberValue != other.truckNumberValue) return false
        if (projectNameId != other.projectNameId) return false
        if (companyName != other.companyName) return false
        if (hasEntry != other.hasEntry) return false

        return true
    }

    override fun hashCode(): Int {
        var result = truckNumberValue?.hashCode() ?: 0
        result = 31 * result + projectNameId.hashCode()
        result = 31 * result + (companyName?.hashCode() ?: 0)
        result = 31 * result + hasEntry.hashCode()
        return result
    }

}

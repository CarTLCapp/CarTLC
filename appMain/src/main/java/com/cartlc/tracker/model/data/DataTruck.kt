/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.data

import com.cartlc.tracker.model.table.DatabaseTable

/**
 * Created by dug on 8/31/17.
 */

class DataTruck : Comparable<DataTruck> {

    var id: Long = 0
    var serverId: Long = 0
    var truckNumber: String? = null
    var licensePlateNumber: String? = null
    var projectNameId: Long = 0
    var companyName: String? = null
    var hasEntry: Boolean = false

    constructor()

    constructor(truckNumber: String, licensePlateNumber: String, projectNameId: Long, companyName: String) {
        this.truckNumber = truckNumber
        this.licensePlateNumber = licensePlateNumber
        this.projectNameId = projectNameId
        this.companyName = companyName
    }

    constructor(id: Long, truckNumber: String, licensePlateNumber: String, projectNameId: Long, companyName: String) {
        this.id = id
        this.truckNumber = truckNumber
        this.licensePlateNumber = licensePlateNumber
        this.projectNameId = projectNameId
        this.companyName = companyName
    }

    override fun equals(other: Any?): Boolean {
        if (other is DataTruck) {
            return equals(other)
        }
        if (other is Long) {
            return id == other
        }
        return false
    }

    fun equals(other: DataTruck): Boolean {
        if (truckNumber == null) {
            if (other.truckNumber != null) {
                return false
            }
        } else if (truckNumber != other.truckNumber) {
            return false
        }
        if (projectNameId != other.projectNameId) {
            return false
        }
        if (companyName == null) {
            if (other.companyName != null) {
                return false
            }
        } else if (companyName != other.companyName) {
            return false
        }
        if (licensePlateNumber == null) {
            if (other.licensePlateNumber != null) {
                return false
            }
        } else if (licensePlateNumber != other.licensePlateNumber) {
            return false
        }
        return hasEntry == other.hasEntry
    }

    override fun toString(): String {
        return toString(truckNumber, licensePlateNumber)
    }

    fun toLongString(db: DatabaseTable): String {
        val sbuf = StringBuilder()
        sbuf.append(id)
        if (serverId != 0L) {
            sbuf.append(" [")
            sbuf.append(serverId)
            sbuf.append("]")
        }
        if (truckNumber != null) {
            sbuf.append(", ")
            sbuf.append(truckNumber)
        }
        if (!licensePlateNumber.isNullOrEmpty()) {
            if (sbuf.length > 0) {
                sbuf.append(" : ")
            }
            sbuf.append(licensePlateNumber)
        }
        if (projectNameId > 0) {
            if (sbuf.length > 0) {
                sbuf.append(", ")
            }
            sbuf.append(db.tableProjects.queryProjectName(projectNameId))
            sbuf.append("(")
            sbuf.append(projectNameId)
            sbuf.append(")")
        }
        if (companyName != null) {
            if (sbuf.length > 0) {
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
        if (truckNumber == null) {
            return if (other.truckNumber == null) {
                0
            } else -1
        } else if (other.truckNumber == null) {
            return 1
        }
        return truckNumber!!.compareTo(other.truckNumber!!)
    }

    companion object {

        fun toString(truckNumber: String?, licensePlateNumber: String?): String {
            val sbuf = StringBuilder()
            if (truckNumber != null) {
                sbuf.append(truckNumber)
            }
            if (!licensePlateNumber.isNullOrEmpty()) {
                if (sbuf.length > 0) {
                    sbuf.append(" : ")
                }
                sbuf.append(licensePlateNumber)
            }
            return sbuf.toString()
        }
    }
}

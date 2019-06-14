/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.data

import com.cartlc.tracker.model.table.DatabaseTable
import timber.log.Timber

/**
 * Created by dug on 5/10/17.
 */
class DataProjectAddressCombo : Comparable<DataProjectAddressCombo> {

    constructor(db: DatabaseTable, projectNameId: Long, addressId: Long) {
        this.db = db
        this.projectNameId = projectNameId
        this.addressId = addressId
    }

    constructor(db: DatabaseTable, rowId: Long, projectNameId: Long, addressId: Long) {
        this.db = db
        this.id = rowId
        this.projectNameId = projectNameId
        this.addressId = addressId
    }

    var id: Long = 0
    var projectNameId: Long
    var addressId: Long

    private var mAddress: DataAddress? = null
    private var db: DatabaseTable

    val projectDashName: String
        get() {
            val name = db.tableProjects.queryProjectName(projectNameId) ?: return "-"
            val rootProject = name.first
            val subProject = name.second
            if (subProject.isEmpty()) {
                return rootProject
            }
            return "$rootProject - $subProject"
        }

    val isRootProject: Boolean
        get() {
            val name = db.tableProjects.queryProjectName(projectNameId) ?: return false
            return name.second.isEmpty()
        }

    val rootName: String
        get() {
            val name = db.tableProjects.queryProjectName(projectNameId) ?: return ""
            return name.first
        }

    val projectName: Pair<String, String>?
        get() = db.tableProjects.queryProjectName(projectNameId)

    val project: DataProject?
        get() = db.tableProjects.queryById(projectNameId)

    val address: DataAddress?
        get() {
            if (mAddress == null) {
                mAddress = db.tableAddress.query(addressId)
                if (mAddress == null) {
                    Timber.e("Could not find address ID=$addressId")
                }
            }
            return mAddress
        }

    val addressLine: String
        get() {
            val address = address
            return address?.line ?: "Invalid"
        }

    val companyName: String?
        get() {
            val address = address
            return address?.company
        }

    val hintLine: String
        get() {
            val sbuf = StringBuffer()
            sbuf.append(projectName)
            sbuf.append("\n")
            sbuf.append(companyName)
            return sbuf.toString()
        }

    val entries: List<DataEntry>
        get() = db.tableEntry.queryForProjectAddressCombo(id)

    fun reset(projectNameId: Long, addressId: Long) {
        this.projectNameId = projectNameId
        this.addressId = addressId
//        mProjectName = null
        mAddress = null
    }

    override fun compareTo(other: DataProjectAddressCombo): Int {
        val name = projectDashName
        val otherName = other.projectDashName
        return name.compareTo(otherName)
    }

    override fun toString(): String {
        val sbuf = StringBuilder()
        sbuf.append("ID=")
        sbuf.append(id)
        sbuf.append(", PROJID=")
        sbuf.append(projectNameId)
        sbuf.append(" [")
        sbuf.append(projectName)
        sbuf.append("] ADDRESS=")
        sbuf.append(addressId)
        if (address != null) {
            sbuf.append(" [")
            sbuf.append(address!!.block)
            sbuf.append("]")
        }
        return sbuf.toString()
    }


//    val hasValidState: Boolean
//        get() = address?.hasValidState() ?: false
//
//    fun fix(): DataAddress? {
//        address?.let {
//            if (it.fix()) {
//                return it
//            }
//        }
//        return null
//    }
}

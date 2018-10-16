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

    private var mProjectName: String? = null
    private var mAddress: DataAddress? = null
    private var db: DatabaseTable

    val projectName: String?
        get() {
            if (mProjectName == null) {
                mProjectName = db.projects.queryProjectName(projectNameId)
                if (mProjectName == null) {
                    Timber.e("Could not find project ID=$projectNameId")
                }
            }
            return mProjectName
        }

    val project: DataProject?
        get() = db.projects.queryById(projectNameId)

    val address: DataAddress?
        get() {
            if (mAddress == null) {
                mAddress = db.address.query(addressId)
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
        get() = db.entry.queryForProjectAddressCombo(id)

    fun reset(projectNameId: Long, addressId: Long) {
        this.projectNameId = projectNameId
        this.addressId = addressId
        mProjectName = null
        mAddress = null
    }

    override fun compareTo(other: DataProjectAddressCombo): Int {
        val name = projectName
        val otherName = other.projectName
        if (name != null && otherName != null) {
            return name.compareTo(otherName)
        }
        return if (name == null && otherName == null) {
            0
        } else 1
    }

    val hasValidState: Boolean
        get() = address?.hasValidState() ?: false

    fun fix(): DataAddress? {
        address?.let {
            if (it.fix()) {
                return it
            }
        }
        return null
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
}

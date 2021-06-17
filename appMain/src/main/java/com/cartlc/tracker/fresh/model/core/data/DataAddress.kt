/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.data

/**
 * Created by dug on 5/10/17.
 */
class DataAddress {

    companion object {

        internal fun equals(field1: String?, field2: String?): Boolean {
            if (field1 == null && field2 == null) {
                return true
            }
            if (field1 != null && field2 == null) {
                return false
            }
            return if (field1 == null && field2 != null) {
                false
            } else {
                field1 == field2
            }
        }
    }

    constructor(company: String) {
        this.company = company
        this.isBootStrap = true
    }

    constructor(company: String, street: String, city: String?, state: String?, zipcode: String?) {
        this.company = company
        this.street = street
        this.city = city
        this.state = state
        this.zipcode = zipcode
    }

    constructor(server_id: Int, company: String, street: String, city: String?, state: String?, zipcode: String?, disabled: Boolean) {
        this.serverId = server_id
        this.company = company
        this.street = street
        this.city = city
        this.state = state
        this.zipcode = zipcode
        this.disabled = disabled
    }

    constructor(id: Long, server_id: Int, company: String, street: String?, city: String?, state: String?, zipcode: String?) {
        this.id = id
        this.serverId = server_id
        this.company = company
        this.street = street
        this.city = city
        this.state = state
        this.zipcode = zipcode
    }

    var id: Long = 0
    var serverId: Int = 0
    var company: String
    var street: String? = null
    var city: String? = null
    var state: String? = null
    var zipcode: String? = null
    var disabled: Boolean = false
    var isLocal: Boolean = false
    var isBootStrap: Boolean = false

    val block: String
        get() {
            val sbuf = StringBuilder()
            sbuf.append(company)
            if (hasValidAddress()) {
                sbuf.append("\n")
                sbuf.append(street)
                sbuf.append(",\n")
                sbuf.append(city)
                sbuf.append(", ")
                sbuf.append(state)
            }
            if (!zipcode.isNullOrEmpty()) {
                sbuf.append(" ")
                sbuf.append(zipcode)
            }
            return sbuf.toString()
        }

    // Used to send address to the server.
    val line: String
        get() {
            val sbuf = StringBuilder()
            sbuf.append(company)
            if (hasValidAddress()) {
                sbuf.append(", ")
                sbuf.append(street)
                sbuf.append(", ")
                sbuf.append(city)
                sbuf.append(", ")
                sbuf.append(state)
            }
            if (hasZipCode()) {
                sbuf.append(", ")
                sbuf.append(zipcode)
            }
            return sbuf.toString()
        }

    private fun hasValidAddress(): Boolean {
        return !street.isNullOrBlank() && !city.isNullOrBlank() && !state.isNullOrBlank()
    }

    fun hasValidState(): Boolean {
        return !state.isNullOrEmpty() && DataStates.isValid(state)
    }

    private fun hasZipCode(): Boolean {
        return !zipcode.isNullOrBlank()
    }

    fun fix(): Boolean {
        if (!hasValidState()) {
            if (DataStates.isValid(city)) {
                val saved = city
                city = state
                state = saved
                return true
            }
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (other is DataAddress) {
            return equals(other)
        }
        if (other is Long) {
            return id == other
        }
        return false
    }

    fun equals(item: DataAddress): Boolean {
        return equals(company, item.company) &&
                equals(street, item.street) &&
                equals(city, item.city) &&
                equals(state, item.state) &&
                equals(zipcode, item.zipcode) &&
                disabled == item.disabled
    }

    override fun toString(): String {
        val sbuf = StringBuilder()
        sbuf.append(company)
        sbuf.append(", ")
        sbuf.append(street)
        sbuf.append(", ")
        sbuf.append(city)
        sbuf.append(", ")
        sbuf.append(state)
        sbuf.append(", ")
        sbuf.append(zipcode)
        sbuf.append(", ")
        sbuf.append(", L(")
        sbuf.append(isLocal)
        sbuf.append("), D(")
        sbuf.append(disabled)
        sbuf.append(")")
        sbuf.append(", SID[")
        sbuf.append(serverId)
        sbuf.append("], LID[")
        sbuf.append(id)
        sbuf.append("]")
        return sbuf.toString()
    }

}

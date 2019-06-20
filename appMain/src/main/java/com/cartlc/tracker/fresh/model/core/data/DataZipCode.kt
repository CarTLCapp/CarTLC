/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.data

/**
 * Created by dug on 8/24/17.
 */

class DataZipCode {

    var zipCode: String? = null
    var stateLongName: String? = null
    var stateShortName: String? = null
    var city: String? = null

    private val hasZipCode: Boolean
        get() = zipCode?.isNotBlank() ?: false
    private val hasStateLongName: Boolean
        get() = stateLongName?.isNotBlank() ?: false
    private val hasCity: Boolean
        get() = city?.isNotBlank() ?: false

    val isValid: Boolean
        get() = hasZipCode && hasStateLongName && hasCity

    val hint: String
        get() {
            val sbuf = StringBuilder()
            sbuf.append(city)
            sbuf.append(", ")
            if (stateShortName == null) {
                sbuf.append(stateLongName)
            } else {
                sbuf.append(stateShortName)
            }
            return sbuf.toString()
        }

    override fun toString(): String {
        val sbuf = StringBuilder()
        sbuf.append("ZIP=")
        sbuf.append(zipCode)
        sbuf.append(",ST=")
        sbuf.append(stateShortName)
        sbuf.append(",STATE=")
        sbuf.append(stateLongName)
        sbuf.append(",CITY=")
        sbuf.append(city)
        return sbuf.toString()
    }

    fun check() {
        if (DataStates.isValid(city)) {
            if (stateShortName == stateLongName) {
                stateLongName = city
                city = stateShortName
                stateShortName = null
            }
        }
    }

}

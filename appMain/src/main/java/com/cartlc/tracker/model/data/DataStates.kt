/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.data

/**
 * Created by dug on 5/11/17.
 */

object DataStates {

    internal val STATES = arrayOf(
            State("Alabama", "AL"),
            State("Alaska", "AK"),
            State("Arizona", "AZ"),
            State("Arkansas", "AR"),
            State("California", "CA"),
            State("Colorado", "CO"),
            State("Connecticut", "CT"),
            State("Delaware", "DE"),
            State("Florida", "FL"),
            State("Georgia", "GA"),
            State("Hawaii", "HI"),
            State("Idaho", "ID"),
            State("Illinois", "IL"),
            State("Indiana", "IN"),
            State("Iowa", "IA"),
            State("Kansas", "KS"),
            State("Kentucky", "KY"),
            State("Louisiana", "LA"),
            State("Maine", "ME"),
            State("Maryland", "MD"),
            State("Massachusetts", "MA"),
            State("Michigan", "MI"),
            State("Minnesota", "MN"),
            State("Mississippi", "MS"),
            State("Missouri", "MO"),
            State("Montana", "MT"),
            State("Nebraska", "NE"),
            State("Nevada", "NV"),
            State("New Hampshire", "NH"),
            State("New Jersey", "NJ"),
            State("New Mexico", "NM"),
            State("New York", "NY"),
            State("North Carolina", "NC"),
            State("North Dakota", "ND"),
            State("Ohio", "OH"),
            State("Oklahoma", "OK"),
            State("Oregon", "OR"),
            State("Pennsylvania", "PA"),
            State("Rhode Island", "RI"),
            State("South Carolina", "SC"),
            State("South Dakota", "SD"),
            State("Tennessee", "TN"),
            State("Texas", "TX"),
            State("Utah", "UT"),
            State("Vermont", "VT"),
            State("Virginia", "VA"),
            State("Washington", "WA"),
            State("West Virginia", "WV"),
            State("Wisconsin", "WI"),
            State("Wyoming", "WY"))

    class State internal constructor(var full: String, var abbr: String)

    fun getUnusedStates(usedStates: List<String>): List<String> {
        val unused = mutableListOf<String>()
        for (state in STATES) {
            if (!usedStates.contains(state.full) && !usedStates.contains(state.abbr)) {
                unused.add(state.full)
            }
        }
        unused.sort()
        return unused
    }

    fun isValid(scan: String?): Boolean {
        return scan?.let { get(it) != null } ?: false
    }

    operator fun get(scan: String): State? {
        for (state in STATES) {
            if (state.full.equals(scan, ignoreCase = true) || state.abbr.equals(scan, ignoreCase = true)) {
                return state
            }
        }
        return null
    }

    fun getAbbr(scan: String): String? {
        val state = get(scan)
        return state?.abbr
    }

    fun getFull(scan: String): String? {
        val state = get(scan)
        return state?.full
    }
}

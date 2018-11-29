/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.data

import com.cartlc.tracker.model.misc.HashLongList
import com.cartlc.tracker.model.misc.HashStringList
import com.cartlc.tracker.model.misc.StringBuilderWithComma
import com.cartlc.tracker.model.table.TableString

/**
 * Created by dug on 8/31/17.
 */

class DataVehicle(private val db: TableString) {

    var id: Long = 0
    var serverId: Long = 0
    var inspecting: Long = 0
    var typeOfInspection: Long = 0
    var mileage: Int = 0
    var headLights = HashLongList(db)
    var tailLights = HashLongList(db)
    var exteriorLightIssues: String = ""
    var fluidChecks = HashLongList(db)
    var fluidProblemsDetected: String = ""
    var tireInspection = HashLongList(db)
    var exteriorDamage: String = ""
    var other: String = ""
    var uploaded: Boolean = false

    var inspectingValue: String
        get() = db.query(inspecting)?.value ?: ""
        set(value) { inspecting = db.add(value) }
    var typeOfInspectionValue: String
        get() = db.query(typeOfInspection)?.value ?: ""
        set(value) { typeOfInspection = db.add(value) }
    val headLightsValue: HashStringList
        get() = headLights.expand()
    val tailLightsValue: HashStringList
        get() = tailLights.expand()
    val fluidChecksValue: HashStringList
        get() = fluidChecks.expand()
    val tireInspectionValue: HashStringList
        get() = tireInspection.expand()

    override fun toString(): String {
        return StringBuilderWithComma()
                .append(id)
                .append(serverId)
                .append(inspectingValue)
                .append(typeOfInspectionValue)
                .append(mileage)
                .append(headLights.expand())
                .append(tailLights.expand())
                .append(exteriorLightIssues)
                .append(fluidChecks.expand())
                .append(fluidProblemsDetected)
                .append(tireInspection.expand())
                .append(exteriorDamage)
                .append(uploaded)
                .toString()
    }
}

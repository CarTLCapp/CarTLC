package com.cartlc.tracker.fresh.model.core.data

import com.cartlc.tracker.fresh.model.core.table.DatabaseTable

class DataHours(private val db: DatabaseTable) {

    var id: Long = 0
    var serverId: Long = 0
    var date: Long = 0 // Day
    var projectNameId: Long = 0
    var projectDesc: String? = null
    var startTime: Int = 0 // Minute into day
    var endTime: Int = 0 // Minute into day
    var lunchTime: Int = 0 // Minutes of day
    var breakTime: Int = 0 // Minutes of day
    var driveTime: Int = 0 // Minutes of day
    var notes: String? = null
    var uploaded: Boolean = false
    var isReady: Boolean = false

    val project: DataProject?
        get() = db.tableProjects.queryById(projectNameId)

    override fun toString(): String {
        return "DataHours(id=$id, serverId=$serverId, date=$date, projectNameId=$projectNameId, projectDesc=$projectDesc, startTime=$startTime, endTime=$endTime, lunchTime=$lunchTime, breakTime=$breakTime, driveTime=$driveTime, notes=$notes, uploaded=$uploaded, isReady=$isReady)"
    }

}

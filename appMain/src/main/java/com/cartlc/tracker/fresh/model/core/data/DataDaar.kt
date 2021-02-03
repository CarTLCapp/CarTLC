package com.cartlc.tracker.fresh.model.core.data

import com.cartlc.tracker.fresh.model.core.table.DatabaseTable

class DataDaar(private val db: DatabaseTable) {

    var id: Long = 0
    var serverId: Long = 0
    var date: Long = 0
    var projectNameId: Long = 0
    var projectDesc: String? = null
    var workCompleted: String? = null
    var missedUnits: String? = null
    var issues: String? = null
    var injuries: String? = null
    var startTimeTomorrow: Long = 0 // full date
    var uploaded: Boolean = false
    var isReady: Boolean = false

    val project: DataProject?
        get() = db.tableProjects.queryById(projectNameId)

    override fun toString(): String {
        return "DataDaar(id=$id, serverId=$serverId, date=$date, projectNameId=$projectNameId, projectDesc=$projectDesc, workCompleted=$workCompleted, missedUnits=$missedUnits, issues=$issues, injuries=$injuries, startTimeTomorrow=$startTimeTomorrow, uploaded=$uploaded, isReady=$isReady)"
    }

}

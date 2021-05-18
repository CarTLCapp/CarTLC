package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataHours

interface TableHours {
    fun clearAll()
    fun query(id: Long): DataHours?
    fun queryByServerId(id: Long): DataHours?
    fun queryReadyAndNotUploaded(): List<DataHours>
    fun queryNotReady(): List<DataHours>
    fun queryMostRecentUploaded(): DataHours?
    fun remove(item: DataHours)
    fun save(item: DataHours): Long
    fun saveUploaded(item: DataHours)
}

package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataDaar

interface TableDaar {

    fun query(id: Long): DataDaar?
    fun queryByServerId(id: Long): DataDaar?
    fun queryReadyAndNotUploaded(): List<DataDaar>
    fun queryNotReady(): List<DataDaar>
    fun queryMostRecentUploaded(): DataDaar?
    fun remove(item: DataDaar)
    fun save(item: DataDaar): Long
    fun saveUploaded(item: DataDaar)

}
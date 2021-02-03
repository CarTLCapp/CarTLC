package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataString

interface TableString {
    fun add(text: String): Long
    fun clearAll()
    fun query(id: Long): DataString?
    fun queryByServerId(id: Long): DataString?
    fun queryNotUploaded(): List<DataString>
    fun save(data: DataString): Long
}
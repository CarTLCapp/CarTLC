package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataString

interface TableString {
    fun add(text: String): Long
    fun query(id: Long): DataString?
    fun queryByServerId(id: Long): DataString?
    fun queryNotUploaded(): List<DataString>
    fun save(data: DataString): Long
}
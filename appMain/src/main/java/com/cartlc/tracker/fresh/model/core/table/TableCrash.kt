package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.sql.SqlTableCrash

interface TableCrash {
    fun clearAll()
    fun queryNeedsUploading(): List<SqlTableCrash.CrashLine>
    fun info(message: String)
    fun message(code: Int, message: String, trace: String?)
    fun setUploaded(line: SqlTableCrash.CrashLine)
    fun delete(line: SqlTableCrash.CrashLine)
}
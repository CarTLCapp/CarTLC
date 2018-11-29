package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.sql.SqlTableCrash

interface TableCrash {
    fun queryNeedsUploading(): List<SqlTableCrash.CrashLine>
    fun info(message: String)
    fun message(code: Int, message: String, trace: String?)
    fun setUploaded(line: SqlTableCrash.CrashLine)
}
package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataZipCode

interface TableZipCode {
    fun add(data: DataZipCode)
    fun query(zipCode: String?): DataZipCode?
    fun queryCity(zipCode: String): String?
    fun queryState(zipCode: String): String?
}
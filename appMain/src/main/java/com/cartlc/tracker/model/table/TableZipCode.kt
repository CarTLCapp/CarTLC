package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataZipCode

interface TableZipCode {
    fun add(data: DataZipCode)
    fun query(zipCode: String?): DataZipCode?
    fun queryCity(zipCode: String): String?
    fun queryState(zipCode: String): String?
}
package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataAddress

interface TableAddress {
    val count: Int

    fun add(list: List<DataAddress>)
    fun add(address: DataAddress): Long
    fun isLocalCompanyOnly(company: String?): Boolean
    fun query(): List<DataAddress>
    fun query(id: Long): DataAddress?
    fun queryByServerId(serverId: Int): DataAddress?
    fun queryAddressId(company: String, street: String, city: String, state: String, zipcode: String?): Long
    fun queryByCompanyName(name: String): List<DataAddress>
    fun queryZipCodes(company: String): List<String>
    fun queryStates(company: String, zipcode: String?): List<String>
    fun queryCities(company: String, zipcode: String?, state: String): List<String>
    fun queryStreets(company: String, city: String, state: String, zipcode: String?): List<String>
    fun queryCompanies(): List<String>
    fun removeOrDisable(item: DataAddress)
    fun update(address: DataAddress)
}
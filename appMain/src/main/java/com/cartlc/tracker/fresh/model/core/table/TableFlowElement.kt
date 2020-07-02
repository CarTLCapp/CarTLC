/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataFlowElement

interface TableFlowElement {

    companion object {

        fun convertToStrings(items: List<DataFlowElement>): List<String> {
            val list = mutableListOf<String>()
            for (item in items) {
                item.prompt?.let {
                    if (it.isNotBlank()) {
                        list.add(it)
                    }
                }
            }
            return list
        }

    }

    fun add(item: DataFlowElement): Long
    fun query(): List<DataFlowElement>
    fun query(flow_element_id: Long): DataFlowElement?
    fun first(flow_id: Long): Long?
    fun last(flow_id: Long): Long?
    fun next(flow_element_id: Long): Long?
    fun prev(flow_element_id: Long): Long?
    fun progress(flow_element_id: Long): Pair<Int,Int>?
    fun flowSize(flow_id: Long): Int
    fun isConfirmTop(flow_id: Long, flow_element_id: Long): Boolean
    fun queryByServerId(server_id: Int): DataFlowElement?
    fun queryByFlowId(flow_id: Long): List<DataFlowElement>
    fun queryConfirmBatch(flow_id: Long, flow_element_id: Long): List<DataFlowElement>
    fun remove(item: DataFlowElement)
    fun update(item: DataFlowElement)
    fun toString(flow_id: Long): String
}
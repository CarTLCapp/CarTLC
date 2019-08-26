/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataFlowElement

interface TableFlowElement {

    fun add(item: DataFlowElement): Long
    fun query(): List<DataFlowElement>
    fun queryByServerId(server_id: Int): DataFlowElement?
    fun queryByFlowId(flow_id: Long): List<DataFlowElement>
    fun remove(item: DataFlowElement)
    fun update(item: DataFlowElement)
    fun toString(flow_id: Long): String

}
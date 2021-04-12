/*
 * Copyright 2019-2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataFlow
import com.cartlc.tracker.fresh.model.core.data.DataProject

interface TableFlow {

    fun add(item: DataFlow): Long
    fun clearAll()
    fun query(): List<DataFlow>
    fun queryById(flow_id: Long): DataFlow?
    fun queryBySubProjectId(project_id: Int): DataFlow?
    fun queryByServerId(server_id: Int): DataFlow?
    fun filterHasFlow(incoming: List<DataProject>): List<DataProject>
    fun update(item: DataFlow)
    fun remove(item: DataFlow)
    override fun toString(): String

}
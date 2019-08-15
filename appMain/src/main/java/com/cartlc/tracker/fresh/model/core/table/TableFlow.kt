/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataFlow

interface TableFlow {

    fun add(item: DataFlow): Long
    fun query(): List<DataFlow>
    fun queryBySubProjectId(project_id: Int): DataFlow?
    fun queryByServerId(server_id: Int): DataFlow?
    fun update(item: DataFlow)
    fun remove(item: DataFlow)
    override fun toString(): String

}
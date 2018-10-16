package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataProject

interface TableProjects {
    fun add(item: String, server_id: Int, disabled: Boolean): Long
    fun addTest(item: String): Long
    fun isDisabled(id: Long): Boolean
    fun query(activeOnly: Boolean = false): List<String>
    fun queryById(id: Long): DataProject?
    fun queryByName(name: String): DataProject?
    fun queryByServerId(server_id: Int): DataProject?
    fun queryProjectName(id: Long): String?
    fun queryProjectName(name: String): Long
    fun removeOrDisable(project: DataProject)
    fun update(project: DataProject): Long
}
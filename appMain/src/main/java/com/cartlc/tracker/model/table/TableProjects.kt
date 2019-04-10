package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataProject

interface TableProjects {
    fun add(rootProject: String, subProject: String, server_id: Int, disabled: Boolean): Long
    fun addTest(item: String): Long
    fun isDisabled(id: Long): Boolean
    fun query(activeOnly: Boolean = false): List<DataProject>
    fun queryRootProjectNames(): List<String>
    fun querySubProjectNames(rootName: String): List<String>
    fun queryById(id: Long): DataProject?
    fun queryByName(rootName: String, subProject: String): DataProject?
    fun queryByServerId(server_id: Int): DataProject?
    fun queryProjectName(id: Long): Pair<String, String>?
    fun queryProjectId(rootName: String, subProject: String): Long
    fun removeOrDisable(project: DataProject)
    fun update(project: DataProject): Long
}
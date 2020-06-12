package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataProject

interface TableProjects {
    fun add(rootProject: String, subProject: String, serverId: Int, disabled: Boolean): Long
    fun add(rootProject: String): Long
    fun addTest(item: String): Long
    fun hasServerId(rootName: String, subProject: String): Boolean
    fun isDisabled(id: Long): Boolean
    fun query(activeOnly: Boolean = false): List<DataProject>
    fun queryRootProjectNames(): List<String>
    fun querySubProjects(rootName: String): List<DataProject>
    fun queryById(id: Long): DataProject?
    fun queryByName(rootName: String, subProject: String): DataProject?
    fun queryByName(rootName: String): DataProject?
    fun queryByServerId(server_id: Int): DataProject?
    fun queryProjectName(id: Long): Pair<String, String>?
    fun queryProjectId(rootName: String, subProject: String): Long
    fun queryRootProjectId(rootName: String): Long
    fun removeOrDisable(project: DataProject)
    fun update(project: DataProject): Long
}
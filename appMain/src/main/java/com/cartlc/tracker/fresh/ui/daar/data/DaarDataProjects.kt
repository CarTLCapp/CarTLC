package com.cartlc.tracker.fresh.ui.daar.data

import com.cartlc.tracker.fresh.ui.daar.DaarViewMvc

interface DaarDataProjects {

    val isReady: Boolean
    var selectedProjectTab: DaarViewMvc.ProjectSelect
    var selectingRootName: Boolean
    var selectedRootProjectName: String?
    var selectedSubProjectName: String?
    val selectedProjectId: Long?
    val selectedRecentProjectName: String?
    var selectedRecentPosition: Int?

    val rootProjectNames: List<String>
    fun subProjectsOf(rootName: String): List<String>
    val mostRecentProjects: List<String>
    val mostRecentDate: Long

}

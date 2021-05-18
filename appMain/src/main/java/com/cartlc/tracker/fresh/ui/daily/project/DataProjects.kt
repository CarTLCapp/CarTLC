package com.cartlc.tracker.fresh.ui.daily.project

interface DataProjects {

    val isReady: Boolean
    var selectedProjectTab: ProjectSelect
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

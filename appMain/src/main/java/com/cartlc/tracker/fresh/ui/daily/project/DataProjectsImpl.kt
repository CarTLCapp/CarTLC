package com.cartlc.tracker.fresh.ui.daily.project

import com.cartlc.tracker.fresh.model.core.table.DatabaseTable

abstract class DataProjectsImpl(
        protected val db: DatabaseTable
) : DataProjects {

    open val mostRecentUploadedDate: Long = 0

    private var mostRecentProjectsNameList: List<String>? = null
    private var mostRecentProjectsIdList: List<Long>? = null

    // region DataDataProjects

    override val isReady: Boolean
        get() {
            return selectedProjectId != null
        }

    override var selectedProjectTab: ProjectSelect = ProjectSelect.PROJECT_RECENT
    override var selectingRootName: Boolean = true
    override var selectedRootProjectName: String? = null
    override var selectedSubProjectName: String? = null
    override var selectedRecentPosition: Int? = null

    override val selectedProjectId: Long?
        get() {
            return if (selectedProjectTab == ProjectSelect.PROJECT_RECENT) {
                selectedRecentPosition?.let { position -> mostRecentProjectsIdList?.get(position) }
            } else {
                selectedRootProjectName?.let { rootName ->
                    selectedSubProjectName?.let { subName ->
                        db.tableProjects.queryByName(rootName, subName)?.id
                    }
                }
            }
        }

    override val selectedRecentProjectName: String?
        get() = if (selectedProjectTab == ProjectSelect.PROJECT_RECENT) {
            selectedRecentPosition?.let { position -> mostRecentProjectsNameList?.get(position) }
        } else null

    override val mostRecentProjects: List<String>
        get() {
            return mostRecentProjectsNameList ?: run {
                val mostRecentNameList = mutableListOf<String>()
                val mostRecentIdList = mutableListOf<Long>()
                db.tableEntry.querySince(mostRecentUploadedDate).forEach { entry ->
                    entry.projectAddressCombo?.let { combo ->
                        if (!mostRecentNameList.contains(combo.projectDashName)) {
                            mostRecentNameList.add(combo.projectDashName)
                            mostRecentIdList.add(combo.projectNameId)
                        }
                    }
                    if (entry.date > mostRecentDate) {
                        mostRecentDate = entry.date
                    }
                }
                mostRecentProjectsIdList = mostRecentIdList
                mostRecentProjectsNameList = mostRecentNameList
                mostRecentNameList
            }
        }

    override var mostRecentDate: Long = 0

    override val rootProjectNames: List<String>
        get() = db.tableProjects.queryRootProjectNames()

    override fun subProjectsOf(rootName: String): List<String> {
        val names = mutableListOf<String>()
        db.tableProjects.querySubProjects(rootName).forEach { project ->
            project.subProject?.let { names.add(it) }
        }
        return names
    }

    // endregion DataDataProjects

}

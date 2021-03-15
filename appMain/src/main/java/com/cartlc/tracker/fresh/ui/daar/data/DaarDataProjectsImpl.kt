package com.cartlc.tracker.fresh.ui.daar.data

import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.ui.daar.DaarViewMvc.ProjectSelect
import java.util.concurrent.TimeUnit

class DaarDataProjectsImpl(
        private val db: DatabaseTable
) : DaarDataProjects {

    companion object {
        private const val WINDOW_SINCE_DAYS = 2L
        private const val FALLBACK_SINCE_DAYS = 3L
    }

    private val mostRecentUploadedDaarDate: Long
        get() {
            return db.tableDaar.queryMostRecentUploaded()?.let { data ->
                data.date - TimeUnit.DAYS.toMillis(WINDOW_SINCE_DAYS)
            } ?: run {
                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(FALLBACK_SINCE_DAYS)
            }
        }

    private var mostRecentProjectsNameList: List<String>? = null
    private var mostRecentProjectsIdList: List<Long>? = null

    // region DaarDataProjects

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
                db.tableEntry.querySince(mostRecentUploadedDaarDate).forEach { entry ->
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
        private set

    override val rootProjectNames: List<String>
        get() =  db.tableProjects.queryRootProjectNames()

    override fun subProjectsOf(rootName: String): List<String> {
        val names = mutableListOf<String>()
        db.tableProjects.querySubProjects(rootName).forEach { project ->
            project.subProject?.let { names.add(it) }
        }
        return names
    }

    // endregion DaarDataProjects


}
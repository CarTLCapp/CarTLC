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

    private var mostRecentProjectsList: List<String>? = null

    // region DaarDataProjects


    override val isReady: Boolean
        get() {
            return !selectedRootProjectName.isNullOrEmpty() && !selectedSubProjectName.isNullOrEmpty()
        }

    override var selectedProjectTab: ProjectSelect = ProjectSelect.PROJECT_RECENT
    override var selectingRootName: Boolean = true
    override var selectedRootProjectName: String? = null
    override var selectedSubProjectName: String? = null

    override val selectedProjectId: Long?
        get() {
            return selectedRootProjectName?.let { rootName ->
                selectedSubProjectName?.let { subName ->
                   db.tableProjects.queryByName(rootName, subName)?.id
                }
            }
        }

    override val mostRecentProjects: List<String>
        get() {
            return mostRecentProjectsList ?: run {
                val mostRecentList = mutableListOf<String>()
                db.tableEntry.querySince(mostRecentUploadedDaarDate).forEach { entry ->
                    entry.projectAddressCombo?.let { combo ->
                        mostRecentList.add(combo.projectDashName)
                    }
                    if (entry.date > mostRecentDate) {
                        mostRecentDate = entry.date
                    }
                }
                mostRecentProjectsList = mostRecentList
                mostRecentList
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

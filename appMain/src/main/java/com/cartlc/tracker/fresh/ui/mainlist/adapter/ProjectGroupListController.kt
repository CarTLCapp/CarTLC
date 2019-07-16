/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter

import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.model.core.sql.SqlTableEntry
import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.ProjectGroupItemViewMvc
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.msg.MessageHandler
import com.cartlc.tracker.model.msg.StringMessage

class ProjectGroupListController(
        private val repo: CarRepository,
        private val messageHandler: MessageHandler,
        private val listener: Listener
) : ProjectGroupListAdapter.Listener, ProjectGroupItemViewMvc.Listener, ProjectGroupListUseCase {

    interface Listener {
        fun onProjectGroupSelected(projectGroup: DataProjectAddressCombo)
        fun onProjectRefreshNeeded()
    }

    private val prefHandler = repo.prefHelper
    private var allProjectGroups: List<DataProjectAddressCombo> = emptyList()
    private val filteredProjectGroups = mutableListOf<DataProjectAddressCombo>()
    private val currentProjectGroupId: Long
        get() = prefHandler.currentProjectGroupId

    override val itemCount: Int
        get() = filteredProjectGroups.size

    private val projectGroups: List<DataProjectAddressCombo>
        get() = repo.db.tableProjectAddressCombo.query()

    override fun onBindViewHolder(viewMvc: ProjectGroupItemViewMvc, position: Int) {
        val projectGroup = filteredProjectGroups[position]
        viewMvc.projectName = projectGroup.projectDashName
        val count = countEntries(projectGroup)
        if (count.totalEntries > 0) {
            val sbuf = StringBuilder()
            sbuf.append(messageHandler.getString(StringMessage.title_entries_))
            sbuf.append(" ")
            sbuf.append(count.totalEntries.toString())
            sbuf.append("   ")
            if (count.uploadedAll()) {
                sbuf.append(messageHandler.getString(StringMessage.title_uploaded_done))
            } else {
                sbuf.append(" ")
                sbuf.append(count.totalUploadedMaster.toString())
                if (count.totalUploadedMaster != count.totalUploadedAws) {
                    sbuf.append("/")
                    sbuf.append(count.totalUploadedAws.toString())
                }
            }
            viewMvc.projectNotes = sbuf.toString()
        } else {
            viewMvc.projectNotes = null
        }
        val address = projectGroup.address
        if (address == null) {
            viewMvc.projectAddress = ""
        } else {
            viewMvc.projectAddress = address.block
        }
        viewMvc.highlight = (projectGroup.id == currentProjectGroupId)
        viewMvc.bind(projectGroup, this)
    }

    // region ProjectGroupItemViewMvc.Listener

    override fun onProjectGroupSelected(projectGroup: DataProjectAddressCombo) {
        prefHandler.currentProjectGroup = projectGroup
        listener.onProjectGroupSelected(projectGroup)
        listener.onProjectRefreshNeeded()
    }

    // endregion ProjectGroupItemViewMvc.Listener

    private fun countEntries(projectGroup: DataProjectAddressCombo): SqlTableEntry.Count {
        val count = SqlTableEntry.Count()
        val rootName = projectGroup.rootName
        for (item in allProjectGroups) {
            if (!item.isRootProject && item.rootName == rootName && item.addressId == projectGroup.addressId) {
                val subCount = countProjectAddressCombo(item)
                count.totalUploadedAws += subCount.totalUploadedAws
                count.totalUploadedMaster += subCount.totalUploadedMaster
                count.totalEntries += subCount.totalEntries
            }
        }
        return count
    }

    private fun countProjectAddressCombo(projectGroup: DataProjectAddressCombo): SqlTableEntry.Count =
            repo.db.tableEntry.countProjectAddressCombo(projectGroup.id)

    // region ProjectGroupListUseCase

    override fun onProjectDataChanged() {
        allProjectGroups = projectGroups
        filterGroups()
        listener.onProjectRefreshNeeded()
    }

    // endregion ProjectGroupListUseCase

    private fun filterGroups() {
        filteredProjectGroups.clear()
        for (projectGroup in allProjectGroups) {
            if (projectGroup.isRootProject) {
                filteredProjectGroups.add(projectGroup)
            }
        }
    }

}
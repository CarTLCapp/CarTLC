/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.model.core.sql.SqlTableEntry
import com.cartlc.tracker.viewmodel.frag.MainListViewModel

import kotlinx.android.synthetic.main.entry_item_project.view.*
import timber.log.Timber

/**
 * Created by dug on 5/10/17.
 */

class ProjectGroupListAdapter(
        private val mContext: Context,
        private val vm: MainListViewModel
) : RecyclerView.Adapter<ProjectGroupListAdapter.CustomViewHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private var allProjectGroups: List<DataProjectAddressCombo> = emptyList()
    private val filteredProjectGroups = mutableListOf<DataProjectAddressCombo>()
    private var curProjectGroupId: Long? = null

    inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = layoutInflater.inflate(R.layout.entry_item_project, null)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val projectGroup = filteredProjectGroups[position]
        holder.view.project_name.text = projectGroup.projectDashName
        val count = countEntries(projectGroup)
        if (count.totalEntries > 0) {
            val sbuf = StringBuilder()
            sbuf.append(mContext.getString(R.string.title_entries_))
            sbuf.append(" ")
            sbuf.append(Integer.toString(count.totalEntries))
            sbuf.append("   ")

            if (count.uploadedAll()) {
                sbuf.append(mContext.getString(R.string.title_uploaded_done))
            } else {
                sbuf.append(" ")
                sbuf.append(Integer.toString(count.totalUploadedMaster))
                if (count.totalUploadedMaster != count.totalUploadedAws) {
                    sbuf.append("/")
                    sbuf.append(Integer.toString(count.totalUploadedAws))
                }
            }
            holder.view.project_notes.text = sbuf.toString()
            holder.view.project_notes.visibility = View.VISIBLE
        } else {
            holder.view.project_notes.text = ""
            holder.view.project_notes.visibility = View.GONE
        }
        val address = projectGroup.address
        if (address == null) {
            holder.view.project_address.text = null
        } else {
            holder.view.project_address.text = address.block
        }
        if (projectGroup.id == curProjectGroupId) {
            holder.itemView.setBackgroundResource(R.color.project_highlight)
        } else {
            holder.itemView.setBackgroundResource(R.color.project_normal)
        }
        holder.itemView.setOnClickListener { setSelected(projectGroup) }
    }

    private fun setSelected(group: DataProjectAddressCombo) {
        curProjectGroupId = group.id
        vm.currentProjectGroup = group
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return filteredProjectGroups.size
    }

    fun onDataChanged() {
        allProjectGroups = vm.projectGroups
        curProjectGroupId = vm.currentProjectGroupId
        filterGroups()
        notifyDataSetChanged()
    }

    private fun filterGroups() {
        filteredProjectGroups.clear()
        for (projectGroup in allProjectGroups) {
            if (projectGroup.isRootProject) {
                filteredProjectGroups.add(projectGroup)
            }
        }
    }

    private fun countEntries(projectGroup: DataProjectAddressCombo): SqlTableEntry.Count {
        val count = SqlTableEntry.Count()
        val rootName = projectGroup.rootName
        for (item in allProjectGroups) {
            if (!item.isRootProject && item.rootName == rootName && item.addressId == projectGroup.addressId) {
                val subCount = vm.countProjectAddressCombo(item)
                count.totalUploadedAws += subCount.totalUploadedAws
                count.totalUploadedMaster += subCount.totalUploadedMaster
                count.totalEntries += subCount.totalEntries
            }
        }
        return count
    }
}
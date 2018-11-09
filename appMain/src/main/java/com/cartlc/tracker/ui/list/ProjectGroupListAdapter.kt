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
import com.cartlc.tracker.model.data.DataProjectAddressCombo
import com.cartlc.tracker.viewmodel.MainListViewModel

import kotlinx.android.synthetic.main.entry_item_project.view.*

/**
 * Created by dug on 5/10/17.
 */

class ProjectGroupListAdapter(
        private val mContext: Context,
        private val vm: MainListViewModel
) : RecyclerView.Adapter<ProjectGroupListAdapter.CustomViewHolder>() {

    private val mLayoutInflater: LayoutInflater
    private var mProjectGroups: List<DataProjectAddressCombo> = emptyList()
    private var mCurProjectGroupId: Long? = null

    inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    init {
        mLayoutInflater = LayoutInflater.from(mContext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = mLayoutInflater.inflate(R.layout.entry_item_project, null)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val projectGroup = mProjectGroups[position]
        holder.view.project_name.text = projectGroup.projectName
        val count = vm.tmpDb.tableEntry.countProjectAddressCombo(projectGroup.id)
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
        if (projectGroup.id == mCurProjectGroupId) {
            holder.itemView.setBackgroundResource(R.color.project_highlight)
        } else {
            holder.itemView.setBackgroundResource(R.color.project_normal)
        }
        holder.itemView.setOnClickListener { setSelected(projectGroup) }
    }

    private fun setSelected(group: DataProjectAddressCombo) {
        mCurProjectGroupId = group.id
        vm.tmpPrefHelper.currentProjectGroup = group
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mProjectGroups.size
    }

    fun onDataChanged() {
        mProjectGroups = vm.tmpDb.tableProjectAddressCombo.query()
        mCurProjectGroupId = vm.tmpPrefHelper.currentProjectGroupId
        notifyDataSetChanged()
    }

}
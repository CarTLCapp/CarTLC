/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.cartlc.tracker.R
import com.cartlc.tracker.model.data.DataEquipment
import com.cartlc.tracker.model.data.DataProjectAddressCombo
import com.cartlc.tracker.viewmodel.MainListViewModel

import kotlinx.android.synthetic.main.entry_item_equipment.view.*

/**
 * Created by dug on 5/12/17.
 */

class EquipmentSelectListAdapter(
        private val vm: MainListViewModel
): RecyclerView.Adapter<EquipmentSelectListAdapter.CustomViewHolder>() {

    private var mItems = mutableListOf<DataEquipment>()

    inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: DataEquipment) {
            with(view) {
                check_button!!.text = item.name
                check_button!!.setOnCheckedChangeListener(null)
                check_button!!.isChecked = item.isChecked
                check_button!!.setOnCheckedChangeListener { _, isChecked ->
                    item.isChecked = isChecked
                    vm.tmpDb.tableEquipment.setChecked(item, isChecked)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.entry_item_equipment, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(mItems[position])
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun onDataChanged(currentProjectGroup: DataProjectAddressCombo?) {
        if (currentProjectGroup != null) {
            val collection = vm.tmpDb.tableCollectionEquipmentProject.queryForProject(currentProjectGroup.projectNameId)
            mItems = collection.equipment.toMutableList()
            mItems.sort()
            notifyDataSetChanged()
        }
    }

    fun hasChecked(currentProjectGroup: DataProjectAddressCombo?): Boolean {
        if (currentProjectGroup != null) {
            val collection = vm.tmpDb.tableCollectionEquipmentProject.queryForProject(currentProjectGroup.projectNameId)
            for (item in collection.equipment) {
                if (item.isChecked) {
                    return true
                }
            }
        }
        return false
    }
}

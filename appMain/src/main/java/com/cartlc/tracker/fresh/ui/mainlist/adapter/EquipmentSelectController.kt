/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter

import com.cartlc.tracker.fresh.model.core.data.DataCollectionEquipmentProject
import com.cartlc.tracker.fresh.model.core.data.DataEquipment
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.EquipmentSelectItemViewMvc
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.pref.PrefHelper

class EquipmentSelectController(
        private val repo: CarRepository,
        private val listener: Listener
) : EquipmentSelectUseCase,
        EquipmentSelectListAdapter.Listener,
        EquipmentSelectItemViewMvc.Listener
{

    interface Listener {
        fun onEquipmentDataChanged(items: List<DataEquipment>)
    }

    private val prefHelper: PrefHelper
        get() = repo.prefHelper

    private val currentProjectGroup: DataProjectAddressCombo?
        get() = prefHelper.currentProjectGroup

    private var items = mutableListOf<DataEquipment>()

    private fun queryForProject(currentProjectGroup: DataProjectAddressCombo): DataCollectionEquipmentProject =
            repo.db.tableCollectionEquipmentProject.queryForProject(currentProjectGroup.projectNameId)

    // region EquipmentSelectListAdapter.Listener

    override fun onBindViewHolder(viewMvc: EquipmentSelectItemViewMvc, position: Int) {
        val item = items[position]
        viewMvc.text = item.name
        viewMvc.isChecked = item.isChecked
        viewMvc.bind(item, this)
    }

    // endregion EquipmentSelectListAdapter.Listener

    // region EquipmentSelectItemViewMvc.Listener

    override fun onCheckedChanged(item: DataEquipment, isChecked: Boolean) {
        item.isChecked = isChecked
        onItemChecked(item, isChecked)
    }

    private fun onItemChecked(item: DataEquipment, isChecked: Boolean) {
        repo.db.tableEquipment.setChecked(item, isChecked)
    }

    // endregion EquipmentSelectItemViewMvc.Listener

    override fun onEquipmentDataChanged() {
        currentProjectGroup?.let {
            val collection = queryForProject(it)
            items = collection.equipment.toMutableList()
            items.sort()
            listener.onEquipmentDataChanged(items)
        }
    }
}
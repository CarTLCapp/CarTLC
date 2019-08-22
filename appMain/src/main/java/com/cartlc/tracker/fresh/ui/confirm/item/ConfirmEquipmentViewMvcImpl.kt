/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.confirm.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl
import com.cartlc.tracker.fresh.ui.confirm.data.ConfirmDataEquipment
import com.cartlc.tracker.fresh.ui.mainlist.adapter.SimpleListAdapter

class ConfirmEquipmentViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?,
        factoryViewMvc: FactoryViewMvc
) : ViewMvcImpl(), ConfirmEquipmentViewMvc {

    override val rootView: View = inflater.inflate(R.layout.confirm_equipment, container, false)

    private val ctx = rootView.context
    private val equipmentGrid = findViewById<RecyclerView>(R.id.equipment_grid)
    private val equipmentListAdapter: SimpleListAdapter = SimpleListAdapter(factoryViewMvc, R.layout.confirm_equipment_item, null)

    init {
        equipmentGrid.adapter = equipmentListAdapter
        equipmentGrid.layoutManager = GridLayoutManager(ctx, 2)
    }

    // region ConfirmEquipmentViewMvc

    override var data: ConfirmDataEquipment
        get() = ConfirmDataEquipment(equipmentNames)
        set(value) {
            equipmentNames = value.equipmentNames
        }

    private var equipmentNames: List<String>
        get() = TODO("not implemented")
        set(value) {
            equipmentListAdapter.items = value
        }

    // endregion ConfirmEquipmentViewMvc
}
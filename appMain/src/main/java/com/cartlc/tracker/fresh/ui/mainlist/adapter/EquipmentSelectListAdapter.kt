/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.cartlc.tracker.fresh.model.core.data.DataEquipment
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.EquipmentSelectItemViewMvc

class EquipmentSelectListAdapter(
        private val viewMvcFactory: FactoryViewMvc,
        private val listener: Listener
) : RecyclerView.Adapter<EquipmentSelectListAdapter.MyViewHolder>() {

    interface Listener {
        fun onBindViewHolder(viewMvc: EquipmentSelectItemViewMvc, position: Int)
    }

    var items: List<DataEquipment> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class MyViewHolder(val viewMvc: EquipmentSelectItemViewMvc) : RecyclerView.ViewHolder(viewMvc.rootView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(viewMvcFactory.allocEquipmentSelectItemViewMvc(parent))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        listener.onBindViewHolder(holder.viewMvc, position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

}

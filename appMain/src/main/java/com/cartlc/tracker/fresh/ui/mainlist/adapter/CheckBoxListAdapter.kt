/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.CheckBoxItemViewMvc
import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.RadioListItemViewMvc

class CheckBoxListAdapter(
        private val viewMvcFactory: FactoryViewMvc,
        private val listener: Listener
) : RecyclerView.Adapter<CheckBoxListAdapter.MyViewHolder>() {

    interface Listener {
        val itemCount: Int
        fun onBindViewHolder(viewMvc: CheckBoxItemViewMvc, position: Int)
    }

    class MyViewHolder(val viewMvc: CheckBoxItemViewMvc) : RecyclerView.ViewHolder(viewMvc.rootView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(viewMvcFactory.allocCheckBoxItemViewMvc(parent))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        listener.onBindViewHolder(holder.viewMvc, position)
    }

    override fun getItemCount(): Int {
        return listener.itemCount
    }
}

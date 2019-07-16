/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.cartlc.tracker.fresh.ui.app.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.SimpleItemViewMvc

class SimpleListAdapter(
        private val viewMvcFactory: FactoryViewMvc,
        private val entryItemLayoutId: Int,
        private val listener: Listener?
) : RecyclerView.Adapter<SimpleListAdapter.MyViewHolder>(), SimpleItemViewMvc.Listener {

    interface Listener {
        fun onSimpleItemClicked(position: Int, text: String)
    }

    class MyViewHolder(val viewMvc: SimpleItemViewMvc) : RecyclerView.ViewHolder(viewMvc.rootView)

    var items: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var selectedPos: Int = -1
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(viewMvcFactory.allocSimpleListItemViewMvc(parent, entryItemLayoutId))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.viewMvc.bind(position, items[position], this)
        listener?.let { holder.viewMvc.selected = position == selectedPos }
    }

    override fun onClicked(position: Int, value: String) {
        listener?.onSimpleItemClicked(position, value)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setSelected(value: String): Int {
        selectedPos = items.indexOf(value)
        return selectedPos
    }

    fun setNoneSelected() {
        selectedPos = -1
    }

}

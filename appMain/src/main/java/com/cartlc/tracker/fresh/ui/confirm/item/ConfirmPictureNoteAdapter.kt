/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.confirm.item

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.confirm.data.NoteLabelValue

class ConfirmPictureNoteAdapter(
        private val viewMvcFactory: FactoryViewMvc
) : RecyclerView.Adapter<ConfirmPictureNoteAdapter.MyViewHolder>() {

    var items = emptyList<NoteLabelValue>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class MyViewHolder(val viewMvc: ConfirmPictureNoteItemViewMvc) : RecyclerView.ViewHolder(viewMvc.rootView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(viewMvcFactory.allocConfirmPictureNoteItemViewMvc(parent))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.viewMvc.value = items[position]
    }

    override fun getItemCount(): Int {
        return items.size
    }

}

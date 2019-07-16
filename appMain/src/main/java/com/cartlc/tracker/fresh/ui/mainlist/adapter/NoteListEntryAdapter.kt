/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.ui.app.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.NoteListEntryItemViewMvc

/**
 * Created by dug on 5/12/17.
 */

class NoteListEntryAdapter(
        private val viewMvcFactory: FactoryViewMvc,
        private val listener: Listener
) : RecyclerView.Adapter<NoteListEntryAdapter.MyViewHolder>() {

    interface Listener {
        fun onBindViewHolder(viewMvc: NoteListEntryItemViewMvc, position: Int)
    }

    var items: List<DataNote> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class MyViewHolder(val viewMvc: NoteListEntryItemViewMvc) : RecyclerView.ViewHolder(viewMvc.rootView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(viewMvcFactory.allocNoteListEntryItemViewMvc(parent))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        listener.onBindViewHolder(holder.viewMvc, position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

}

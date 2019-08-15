/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc

class PictureNoteAdapter(
        private val viewMvcFactory: FactoryViewMvc,
        private val listener: Listener
) : RecyclerView.Adapter<PictureNoteAdapter.MyViewHolder>() {

    interface Listener {
        val isThumbnail: Boolean
        val noteCount: Int
        fun onBindViewHolder(itemViewMvc: ViewMvc, position: Int)
    }

    class MyViewHolder(val viewMvc: ViewMvc) : RecyclerView.ViewHolder(viewMvc.rootView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return if (listener.isThumbnail)
            MyViewHolder(viewMvcFactory.allocPictureNoteThumbnailItemViewMvc(parent))
        else
            MyViewHolder(viewMvcFactory.allocPictureNoteItemViewMvc(parent))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        listener.onBindViewHolder(holder.viewMvc, position)
    }

    override fun getItemCount(): Int {
        return listener.noteCount
    }

}

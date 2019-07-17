/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.callassistant.util.viewmvc.ViewMvc
import com.cartlc.tracker.fresh.ui.app.FactoryViewMvc

class PictureListAdapter(
        private val viewMvcFactory: FactoryViewMvc,
        private val listener: Listener
) : RecyclerView.Adapter<PictureListAdapter.MyViewHolder>() {

    interface Listener {
        val isThumbnail: Boolean
        val itemCount: Int
        fun onBindViewHolder(itemViewMvc: ViewMvc, position: Int)
    }

    class MyViewHolder(val viewMvc: ViewMvc) : RecyclerView.ViewHolder(viewMvc.rootView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return if (listener.isThumbnail)
            MyViewHolder(viewMvcFactory.allocPictureListThumbnailItemViewMvc(parent))
        else
            MyViewHolder(viewMvcFactory.allocPictureListItemViewMvc(parent))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        listener.onBindViewHolder(holder.viewMvc, position)
    }

    override fun getItemCount(): Int {
        return listener.itemCount
    }

}

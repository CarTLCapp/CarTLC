/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.confirm.item

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import java.io.File

class ConfirmPictureAdapter(
        private val viewMvcFactory: FactoryViewMvc
) : RecyclerView.Adapter<ConfirmPictureAdapter.MyViewHolder>() {

    var items = emptyList<File>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class MyViewHolder(val viewMvc: ConfirmPictureItemViewMvc) : RecyclerView.ViewHolder(viewMvc.rootView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(viewMvcFactory.allocConfirmPictureItemViewMvc(parent))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.viewMvc.bindPicture(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

}

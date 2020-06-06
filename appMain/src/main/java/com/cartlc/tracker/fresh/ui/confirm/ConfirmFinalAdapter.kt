/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.confirm

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.confirm.data.ConfirmDataType
import com.cartlc.tracker.fresh.ui.confirm.item.ConfirmBasicsViewMvc
import com.cartlc.tracker.fresh.ui.confirm.item.ConfirmEquipmentViewMvc
import com.cartlc.tracker.fresh.ui.confirm.item.ConfirmNotesViewMvc
import com.cartlc.tracker.fresh.ui.confirm.item.ConfirmPictureViewMvc

class ConfirmFinalAdapter(
        private val viewMvcFactory: FactoryViewMvc
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class BasicsViewHolder(val viewMvc: ConfirmBasicsViewMvc) : RecyclerView.ViewHolder(viewMvc.rootView)
    class EquipmentViewHolder(val viewMvc: ConfirmEquipmentViewMvc) : RecyclerView.ViewHolder(viewMvc.rootView)
    class NotesViewHolder(val viewMvc: ConfirmNotesViewMvc) : RecyclerView.ViewHolder(viewMvc.rootView)
    class PictureViewHolder(val viewMvc: ConfirmPictureViewMvc) : RecyclerView.ViewHolder(viewMvc.rootView)
    class UnknownViewHolder(val viewMvc: ConfirmNotesViewMvc) : RecyclerView.ViewHolder(viewMvc.rootView)

    var items = emptyList<ConfirmDataType>()

    override fun getItemViewType(position: Int): Int {
        return items[position].ord
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ConfirmDataType.ORD_BASICS -> BasicsViewHolder(viewMvcFactory.allocConfirmBasicsViewMvc(parent))
            ConfirmDataType.ORD_EQUIPMENT -> EquipmentViewHolder(viewMvcFactory.allocConfirmEquipmentViewMvc(parent))
            ConfirmDataType.ORD_NOTES -> NotesViewHolder(viewMvcFactory.allocConfirmNotesViewMvc(parent))
            ConfirmDataType.ORD_PICTURES -> PictureViewHolder(viewMvcFactory.allocConfirmPictureViewMvc(parent))
            else -> UnknownViewHolder(viewMvcFactory.allocConfirmNotesViewMvc(parent))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ConfirmDataType.BASICS -> (holder as BasicsViewHolder).viewMvc.data = item.data
            is ConfirmDataType.EQUIPMENT -> (holder as EquipmentViewHolder).viewMvc.data = item.data
            is ConfirmDataType.NOTES -> (holder as NotesViewHolder).viewMvc.data = item.data
            is ConfirmDataType.PICTURES -> (holder as PictureViewHolder).viewMvc.data = item.data
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}

/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.ui.listentries

import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvc
import com.cartlc.tracker.fresh.ui.listentries.item.ListEntriesItemViewMvc

interface ListEntriesViewMvc : ObservableViewMvc<ListEntriesViewMvc.Listener> {

    interface Listener {
        val itemCount: Int
        fun onEditAddress()
        fun onDelete()
        fun onBindViewHolder(itemViewMvc: ListEntriesItemViewMvc, position: Int)
    }

    fun notifyDataSetChanged()
    var projectName: String?
    var projectAddress: String?
    var deleteVisible: Boolean

}
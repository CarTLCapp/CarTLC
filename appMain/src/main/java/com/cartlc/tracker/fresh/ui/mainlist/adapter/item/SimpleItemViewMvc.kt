package com.cartlc.tracker.fresh.ui.mainlist.adapter.item

import com.callassistant.util.viewmvc.ViewMvc

interface SimpleItemViewMvc : ViewMvc {

    interface Listener {
        fun onClicked(position: Int, value: String)
    }

    var selected: Boolean

    fun bind(position: Int, value: String, listener: Listener)

}
/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter.item

import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc

interface SimpleItemViewMvc : ViewMvc {

    interface Listener {
        fun onClicked(position: Int, value: String)
    }

    var selected: Boolean

    fun bind(position: Int, value: String, listener: Listener)

}
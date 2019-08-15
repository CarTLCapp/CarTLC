/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter.item

import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc

interface CheckBoxItemViewMvc: ViewMvc {

    interface Listener {
        fun onCheckedChanged(position: Int, item: String, isChecked: Boolean)
    }

    var text: String?
    var isChecked: Boolean

    fun bind(position: Int, item: String, listener: Listener)

}
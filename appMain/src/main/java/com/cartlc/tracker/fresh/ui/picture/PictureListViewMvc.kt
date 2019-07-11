/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc

interface PictureListViewMvc : ViewMvc {

    interface Listener {
        val pictureCount: Int
        fun onBindViewHolder(itemViewMvc: ViewMvc, position: Int)
        val noteCount: Int
    }

    var listener: Listener?

    fun onPictureRefreshNeeded()

}
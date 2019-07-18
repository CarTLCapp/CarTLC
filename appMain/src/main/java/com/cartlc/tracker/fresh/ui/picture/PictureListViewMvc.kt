/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc

interface PictureListViewMvc : ViewMvc {

    interface Listener : PictureListAdapter.Listener

    var listener: Listener?

    fun onPictureRefreshNeeded()

}
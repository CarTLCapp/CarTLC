/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import com.callassistant.util.viewmvc.ViewMvc

interface PictureListViewMvc : ViewMvc {

    interface Listener : PictureListAdapter.Listener

    var listener: Listener?

    fun onPictureRefreshNeeded()

}
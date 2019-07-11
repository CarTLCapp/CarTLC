/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture.item

import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc
import java.io.File

interface PictureListItemViewMvc: ViewMvc {

    interface Listener {
        fun onRemoveClicked()
        fun onCwClicked()
        fun onCcwClicked()
    }

    var loading: String?
    fun bindPicture(pictureFile: File?)
    fun bindListener(listener: Listener)

}
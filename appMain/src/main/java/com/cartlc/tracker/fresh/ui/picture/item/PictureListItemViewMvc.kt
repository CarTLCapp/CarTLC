/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture.item

import android.widget.ImageView
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc
import java.io.File

interface PictureListItemViewMvc: ViewMvc {

    interface Listener {
        fun onRemoveClicked()
        fun onCwClicked()
        fun onCcwClicked()
        fun onImageLoaded(imageHeight: Int)
    }

    var btnRemoveVisible: Boolean
    var btnCcwVisible: Boolean
    var btnCwVisible: Boolean
    var loading: String?
    fun bindPicture(pictureFile: File?)
    fun bindListener(listener: Listener)

}
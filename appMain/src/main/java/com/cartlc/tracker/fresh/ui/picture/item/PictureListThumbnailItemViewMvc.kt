/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture.item

import com.callassistant.util.viewmvc.ViewMvc
import java.io.File

interface PictureListThumbnailItemViewMvc: ViewMvc {

    var loading: String?
    var note: String?

    fun bindPicture(pictureFile: File?)

}
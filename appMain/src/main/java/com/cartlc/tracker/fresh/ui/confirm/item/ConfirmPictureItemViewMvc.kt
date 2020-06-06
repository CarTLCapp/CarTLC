/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.confirm.item

import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc
import java.io.File

interface ConfirmPictureItemViewMvc: ViewMvc {

    fun bindPicture(pictureFile: File?)

}
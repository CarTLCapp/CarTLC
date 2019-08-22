/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.confirm.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl
import com.cartlc.tracker.ui.util.helper.BitmapHelper
import java.io.File

class ConfirmPictureItemViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ViewMvcImpl(), ConfirmPictureItemViewMvc {

    override val rootView: View = inflater.inflate(R.layout.confirm_picture_item, container, false)

    private val pictureView = findViewById<ImageView>(R.id.picture)
    private val loadedHeight = context.resources.getDimension(R.dimen.image_thumbnail_max_height).toInt()

    // region PictureListThumbnailItemViewMvc

    override fun bindPicture(pictureFile: File?) {
        if (pictureFile == null || !pictureFile.exists()) {
            pictureView.setImageResource(android.R.color.transparent)
        } else {
            BitmapHelper.loadBitmap(pictureFile.absolutePath, loadedHeight, pictureView, true)
        }
    }

    // endregion PictureListThumbnailItemViewMvc
}
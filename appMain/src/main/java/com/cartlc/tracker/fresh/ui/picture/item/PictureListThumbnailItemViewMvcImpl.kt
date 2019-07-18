/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl
import com.cartlc.tracker.ui.util.helper.BitmapHelper
import java.io.File

class PictureListThumbnailItemViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ViewMvcImpl(), PictureListThumbnailItemViewMvc {

    override val rootView = inflater.inflate(R.layout.entry_item_picture_thumbnail, container, false)

    private val pictureView = findViewById<ImageView>(R.id.picture)
    private val loadingView = findViewById<TextView>(R.id.loading)
    private val noteView = findViewById<TextView>(R.id.note)
    private val maxHeight = context.resources.getDimension(R.dimen.image_thumbnail_max_height).toInt()

    override var loading: String?
        get() = loadingView.text.toString()
        set(value) {
            loadingView.text = value
            loadingView.visibility = if (value == null) View.GONE else View.VISIBLE
        }

    override var note: String?
        get() = noteView.text.toString()
        set(value) {
            noteView.text = value
            noteView.visibility = if (value == null || value.isEmpty()) View.INVISIBLE else View.VISIBLE
        }

    override fun bindPicture(pictureFile: File?) {
        if (pictureFile == null || !pictureFile.exists()) {
            pictureView.setImageResource(android.R.color.transparent)
        } else {
            BitmapHelper.loadBitmap(pictureFile.absolutePath, maxHeight, pictureView)
        }
    }

}
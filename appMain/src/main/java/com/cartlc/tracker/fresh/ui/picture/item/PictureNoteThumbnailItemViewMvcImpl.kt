/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl

class PictureNoteThumbnailItemViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ViewMvcImpl(), PictureNoteThumbnailItemViewMvc {

    override val rootView: View = inflater.inflate(R.layout.picture_list_item_thumbnail_note, container, false)

    private val noteLabelView = findViewById<TextView>(R.id.label)
    private val noteValueView = findViewById<TextView>(R.id.value)

    override var noteLabel: String?
        get() = noteLabelView.text.toString()
        set(value) {
            noteLabelView.text = value
            noteLabelView.visibility = if (value == null || value.isEmpty()) View.INVISIBLE else View.VISIBLE
        }

    override var noteValue: String?
        get() = noteValueView.text.toString()
        set(value) {
            noteValueView.text = value
            noteValueView.visibility = if (value == null || value.isEmpty()) View.INVISIBLE else View.VISIBLE
        }

}
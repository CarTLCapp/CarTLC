/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.confirm.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl
import com.cartlc.tracker.fresh.ui.confirm.data.NoteLabelValue

class ConfirmPictureNoteItemViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ViewMvcImpl(), ConfirmPictureNoteItemViewMvc {

    override val rootView: View = inflater.inflate(R.layout.confirm_picture_note_item, container, false)

    private val noteLabelView = findViewById<TextView>(R.id.label)
    private val noteValueView = findViewById<TextView>(R.id.value)

    override var value: NoteLabelValue
        get() {
            return NoteLabelValue(
                    noteLabelView.text.toString(),
                    noteValueView.text.toString()
            )
        }
        set(value) {
            noteLabelView.text = value.label
            noteLabelView.visibility = if (value.label.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
            noteValueView.text = value.value
            noteValueView.visibility = if (value.value.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
        }

}
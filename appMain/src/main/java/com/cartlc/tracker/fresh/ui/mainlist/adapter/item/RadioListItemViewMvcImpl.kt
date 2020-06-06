/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter.item

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl

class RadioListItemViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ViewMvcImpl(), RadioListItemViewMvc {

    override val rootView = inflater.inflate(R.layout.mainlist_item_radio, container, false)

    private val itemView = findViewById<RadioButton>(R.id.item)

    override var text: String?
        get() = itemView.text.toString()
        set(value) { itemView.text = value }

    override var isChecked: Boolean
        get() = itemView.isChecked
        set(value) {
            itemView.setOnCheckedChangeListener(null)
            itemView.isChecked = value
        }

    override fun bind(position: Int, item: String, listener: RadioListItemViewMvc.Listener) {
        itemView.setOnCheckedChangeListener { _, isChecked ->
            listener.onCheckedChanged(position, item, isChecked)
        }
    }
}
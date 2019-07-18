package com.cartlc.tracker.fresh.ui.mainlist.adapter.item

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl

class SimpleItemViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?,
        @LayoutRes entryItemLayoutId: Int
) : ViewMvcImpl(), SimpleItemViewMvc {

    override val rootView = inflater.inflate(entryItemLayoutId, container, false)

    private val itemView = findViewById<TextView>(R.id.item)

    override var selected: Boolean
        get() = TODO("not implemented")
        set(value) {
            if (value) {
                itemView.setBackgroundResource(R.color.list_item_selected)
            } else {
                itemView.setBackgroundResource(android.R.color.transparent)
            }
        }

    override fun bind(position: Int, value: String, listener: SimpleItemViewMvc.Listener) {
        itemView.text = value
        itemView.setOnClickListener {
            listener.onClicked(position, value)
        }
    }
}
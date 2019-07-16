package com.cartlc.tracker.fresh.ui.mainlist.adapter.item

import com.callassistant.util.viewmvc.ViewMvc

interface NoteListEntryItemViewMvc: ViewMvc {

    interface Listener {

        fun afterTextChanged(text: String)
        fun onEntryFocused(hasFocus: Boolean)

    }

    var label: String?
    var entryText: String?
    var isSelected: Boolean
    var inputType: Int
    var maxLines: Int
    var numLines: Int

    fun bind(listener: Listener)

}
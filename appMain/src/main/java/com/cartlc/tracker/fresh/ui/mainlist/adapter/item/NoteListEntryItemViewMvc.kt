/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter.item

import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc

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
/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture.item

import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc

interface PictureNoteItemViewMvc : ViewMvc {

    interface Listener {
        fun getHint(note: DataNote): String?
        fun onNoteValueChanged(note: DataNote)
        fun onNoteFocused(note: DataNote, hasFocus: Boolean)
    }

    fun clear()
    fun bind(note: DataNote, listener: Listener)

}
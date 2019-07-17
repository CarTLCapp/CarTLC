/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture.item

interface PictureListItemViewMvc: PictureListThumbnailItemViewMvc {

    interface Listener {
        fun onRemoveClicked()
        fun onCwClicked()
        fun onCcwClicked()
        fun onNoteDialogClicked()
    }

    fun bind(listener: Listener)

}
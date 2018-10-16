/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.list

import android.content.Context
import com.cartlc.tracker.R

/**
 * Created by dug on 5/10/17.
 */

class PictureThumbnailListAdapter(context: Context) : PictureListAdapter(context, {}) {

    override val itemLayout: Int
        get() = R.layout.entry_item_picture_thumbnail

    override val maxHeightResource: Int
        get() = R.dimen.image_thumbnail_max_height

}
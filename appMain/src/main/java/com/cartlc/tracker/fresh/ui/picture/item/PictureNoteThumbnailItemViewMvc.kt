/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture.item

import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc

interface PictureNoteThumbnailItemViewMvc: ViewMvc {
    var noteLabel: String?
    var noteValue: String?
}
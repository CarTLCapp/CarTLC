/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.confirm.item

import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc
import com.cartlc.tracker.fresh.ui.confirm.data.NoteLabelValue

interface ConfirmPictureNoteItemViewMvc : ViewMvc {

    var value: NoteLabelValue
}
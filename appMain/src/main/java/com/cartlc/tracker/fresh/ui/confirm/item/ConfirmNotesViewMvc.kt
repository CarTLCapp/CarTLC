/*
 * Copyright 2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.confirm.item

import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc
import com.cartlc.tracker.fresh.ui.confirm.data.ConfirmDataNotes

interface ConfirmNotesViewMvc: ViewMvc {

    var data: ConfirmDataNotes

}
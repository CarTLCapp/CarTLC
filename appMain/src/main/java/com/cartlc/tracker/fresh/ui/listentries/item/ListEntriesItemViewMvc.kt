/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.ui.listentries.item

import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc

interface ListEntriesItemViewMvc: ViewMvc {

    fun bindOnEditListener(listener: () -> Unit)

    var truckValue: String?
    var status: String?
    var notesLine: String?
    var equipmentLine: String?

}
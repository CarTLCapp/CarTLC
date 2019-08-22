package com.cartlc.tracker.fresh.ui.confirm

import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc
import com.cartlc.tracker.fresh.ui.confirm.data.ConfirmDataType

interface ConfirmFinalViewMvc : ViewMvc {

    var items: List<ConfirmDataType>

}
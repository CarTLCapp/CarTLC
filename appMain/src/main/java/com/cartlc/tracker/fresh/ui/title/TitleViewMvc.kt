package com.cartlc.tracker.fresh.ui.title

import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc

interface TitleViewMvc : ViewMvc {

    var mainTitleText: String?
    var mainTitleVisible: Boolean
    var subTitleText: String?
    var subTitleVisible: Boolean
    var separatorVisible: Boolean

}
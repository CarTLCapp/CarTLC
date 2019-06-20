package com.cartlc.tracker.fresh.ui.title

import com.callassistant.util.viewmvc.ObservableViewMvc

interface TitleViewMvc : ObservableViewMvc<TitleViewMvc.Listener> {

    interface Listener {
    }

    var mainTitleText: String?
    var mainTitleVisible: Boolean
    var subTitleText: String?
    var subTitleVisible: Boolean
    var separatorVisible: Boolean

}
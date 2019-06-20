package com.cartlc.tracker.fresh.ui.buttons

import android.view.View
import com.callassistant.util.viewmvc.ObservableViewMvc

interface ButtonsViewMvc : ObservableViewMvc<ButtonsViewMvc.Listener> {

    interface Listener {
        fun onBtnPrevClicked(view: View)
        fun onBtnNextClicked(view: View)
        fun onBtnCenterClicked(view: View)
        fun onBtnChangeClicked(view: View)
    }

    var showing: Boolean
    var btnPrevVisible: Boolean
    var btnNextVisible: Boolean
    var btnCenterVisible: Boolean
    var btnChangeVisible: Boolean
    var btnPrevText: String?
    var btnNextText: String?
    var btnCenterText: String?

}
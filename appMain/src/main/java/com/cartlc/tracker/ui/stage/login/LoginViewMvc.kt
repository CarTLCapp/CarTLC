package com.cartlc.tracker.ui.stage.login

import com.callassistant.util.viewmvc.ObservableViewMvc

interface LoginViewMvc : ObservableViewMvc<LoginViewMvc.Listener> {

    interface Listener {

        fun onFirstTechCodeChanged(value: String)
        fun onSecondaryTechCodeChanged(value: String)
        fun onSecondaryCheckBoxChanged(value: Boolean)

    }

    var firstTechCode: String
    var firstTechName: String
    var secondaryTechCode: String
    var secondaryTechName: String
    var secondaryTechCodeEnabled: Boolean
    var secondaryCheckBoxChecked: Boolean

}
package com.cartlc.tracker.ui.stage.login

import com.callassistant.util.viewmvc.ObservableViewMvc

interface LoginViewMvc : ObservableViewMvc<LoginViewMvc.Listener> {

    interface Listener {

        fun onFirstNameChanged(value: String)
        fun onLastNameChanged(value: String)
        fun onSecondaryFirstNameChanged(value: String)
        fun onSecondaryLastNameChanged(value: String)
        fun onSecondaryCheckBoxChanged(value: Boolean)

    }

    var firstName: String
    var lastName: String
    var secondaryFirstName: String
    var secondaryLastName: String
    var secondaryFirstNameEnabled: Boolean
    var secondaryLastNameEnabled: Boolean

}
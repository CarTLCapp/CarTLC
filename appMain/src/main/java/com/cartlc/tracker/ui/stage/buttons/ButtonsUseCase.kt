package com.cartlc.tracker.ui.stage.buttons

import com.callassistant.util.observable.BaseObservable
import com.cartlc.tracker.model.event.Button

interface ButtonsUseCase : BaseObservable<ButtonsUseCase.Listener> {

    interface Listener {

        fun onButtonEvent(action: Button)

    }

}
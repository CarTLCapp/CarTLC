package com.cartlc.tracker.ui.stage.buttons

import com.callassistant.util.observable.BaseObservable
import com.cartlc.tracker.model.event.Button

interface ButtonsUseCase : BaseObservable<ButtonsUseCase.Listener> {

    interface Listener {
        fun onButtonConfirm(action: Button): Boolean
        fun onButtonEvent(action: Button)
    }

    var wasNext: Boolean
    var wasSkip: Boolean

    var nextVisible: Boolean
    var prevVisible: Boolean
    var centerVisible: Boolean

    var nextText: String?
    var prevText: String?
    var centerText: String?

    fun skip()
    fun dispatch(button: Button)
    fun checkCenterButtonIsEdit()
}
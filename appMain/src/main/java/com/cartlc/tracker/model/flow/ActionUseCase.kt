package com.cartlc.tracker.model.flow

import com.callassistant.util.observable.BaseObservable
import com.cartlc.tracker.model.event.Action

interface ActionUseCase : BaseObservable<ActionUseCase.Listener> {

    interface Listener {

        fun onActionChanged(action: Action)
    }

    fun dispatchActionEvent(action: Action)

}
package com.cartlc.tracker.model.flow

import com.cartlc.tracker.fresh.ui.common.observable.BaseObservableImpl
import com.cartlc.tracker.model.event.Action

class ActionUseCaseImpl : BaseObservableImpl<ActionUseCase.Listener>(), ActionUseCase {

    override fun dispatchActionEvent(action: Action) {
        for (listener in listeners) {
            listener.onActionChanged(action)
        }
    }

}
package com.cartlc.tracker.model.flow

import com.cartlc.tracker.fresh.ui.common.observable.BaseObservable

interface FlowUseCase : BaseObservable<FlowUseCase.Listener> {

    interface Listener {
        fun onStageChangedAboutTo(flow: Flow)
        fun onStageChanged(flow: Flow)
    }

    var previousFlowValue: Flow?
    var curFlow: Flow

    val wasFromNotify: Boolean

    fun notifyListeners()

}
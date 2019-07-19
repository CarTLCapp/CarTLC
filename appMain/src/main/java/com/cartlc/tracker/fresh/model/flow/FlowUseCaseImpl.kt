package com.cartlc.tracker.fresh.model.flow

import com.cartlc.tracker.fresh.ui.common.observable.BaseObservableImpl

class FlowUseCaseImpl : BaseObservableImpl<FlowUseCase.Listener>(), FlowUseCase {

    override var previousFlowValue: Flow? = null
    override var wasFromNotify: Boolean = false

    override var curFlow: Flow = LoginFlow()
        set(value) {
            wasFromNotify = false
            for (listener in listeners) {
                listener.onStageChangedAboutTo(value)
            }
            previousFlowValue = field
            field = value
            for (listener in listeners) {
                listener.onStageChanged(value)
            }
        }

    override fun notifyListeners() {
        wasFromNotify = true
        for (listener in listeners) {
            listener.onStageChangedAboutTo(curFlow)
        }
        for (listener in listeners) {
            listener.onStageChanged(curFlow)
        }
    }

}
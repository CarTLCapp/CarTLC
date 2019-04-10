package com.cartlc.tracker.ui.stage.buttons

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.msg.StringMessage
import com.cartlc.tracker.ui.app.dependencyinjection.BoundFrag

class MainButtonsController(
        boundFrag: BoundFrag,
        viewMvc: ButtonsViewMvc
) : ButtonsController(boundFrag, viewMvc), MainButtonsUseCase {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    override fun onCreate() {
        super.onCreate()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStageChanged(flow: Flow) {
        super.onStageChanged(flow)

        viewMvc.btnChangeVisible = false
        viewMvc.btnCenterVisible = false
        viewMvc.btnCenterText = messageHandler.getString(StringMessage.btn_add)
        viewMvc.btnNextVisible = flow.hasNext
        viewMvc.btnNextText = messageHandler.getString(StringMessage.btn_next)
        viewMvc.btnPrevVisible = flow.hasPrev
        viewMvc.btnPrevText = messageHandler.getString(StringMessage.btn_prev)
    }

}
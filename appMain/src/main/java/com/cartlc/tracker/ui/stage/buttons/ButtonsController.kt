package com.cartlc.tracker.ui.stage.buttons

import android.view.View
import androidx.lifecycle.*
import com.callassistant.util.observable.BaseObservableImpl
import com.cartlc.tracker.model.event.Button
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.FlowUseCase
import com.cartlc.tracker.model.msg.MessageHandler
import com.cartlc.tracker.model.msg.StringMessage
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.ui.bits.SoftKeyboardDetect
import com.cartlc.tracker.ui.stage.StageHook

open class ButtonsController(
        boundFrag: BoundFrag,
        protected val viewMvc: ButtonsViewMvc
) : BaseObservableImpl<ButtonsUseCase.Listener>(), LifecycleObserver, ButtonsViewMvc.Listener, SoftKeyboardDetect.Listener, FlowUseCase.Listener {

    protected val messageHandler = boundFrag.componentRoot.messageHandler
    private var softKeyboardDetect: SoftKeyboardDetect? = null
    private val activity = boundFrag.activity
    protected val repo = boundFrag.repo

    init {
        boundFrag.bindObserver(this)
        repo.flowUseCase.registerListener(this)
    }

    // region lifecycle functions

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    open fun onCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy() {
        softKeyboardDetect?.unregisterListener(this)
        repo.flowUseCase.unregisterListener(this)
    }

    // endregion lifecycle functions

    open fun install(softKeyboardDetect: SoftKeyboardDetect) {
        this.softKeyboardDetect = softKeyboardDetect
        softKeyboardDetect.registerListener(this)
    }

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
        viewMvc.btnPrevText = messageHandler.getString(StringMessage.btn_prev)
        viewMvc.btnNextText = messageHandler.getString(StringMessage.btn_next)
        viewMvc.btnCenterText = messageHandler.getString(StringMessage.btn_add)
    }

    override fun onStageChanged(flow: Flow) {
    }

    // endregion FlowUseCase.Listener

    // region ButtonsUseCase

    // endregion ButtonsUseCase

    // region ButtonsViewMvc.Listener

    override fun onBtnPrevClicked(view: View) {
        dispatchButtonEvent(Button.BTN_PREV)
        clearSoftKeyboard(view)
    }

    override fun onBtnNextClicked(view: View) {
        dispatchButtonEvent(Button.BTN_NEXT)
        clearSoftKeyboard(view)
    }

    override fun onBtnCenterClicked(view: View) {
        dispatchButtonEvent(Button.BTN_CENTER)
    }

    override fun onBtnChangeClicked(view: View) {
        dispatchButtonEvent(Button.BTN_CHANGE)
    }

    // endregion ButtonsViewMvc.Listener

    // region SoftKeyboardDetect.Listener

    override fun onSoftKeyboardVisible() {
        viewMvc.showing = false
    }

    override fun onSoftKeyboardHidden() {
        viewMvc.showing = true
    }

    // endregion SoftKeyboardDetect.Listener

    // region support functions

    private fun clearSoftKeyboard(view: View) {
        TBApplication.hideKeyboard(activity, view)
    }

    // endregion support functions

    // region support for ButtonsUseCase

    private fun dispatchButtonEvent(action: Button) {
        for (listener in listeners) {
            listener.onButtonEvent(action)
        }
    }

    // endregion support for ButtonsUseCase
}
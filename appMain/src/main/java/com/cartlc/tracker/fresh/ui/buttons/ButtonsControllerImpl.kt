package com.cartlc.tracker.fresh.ui.buttons

import android.view.View
import androidx.lifecycle.*
import com.cartlc.tracker.fresh.model.event.Button
import com.cartlc.tracker.fresh.model.flow.Flow
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.common.observable.BaseObservableImpl
import com.cartlc.tracker.fresh.ui.bits.HideOnSoftKeyboard

open class ButtonsControllerImpl(
        boundAct: BoundAct,
        private val viewMvc: ButtonsViewMvc
) : BaseObservableImpl<ButtonsController.Listener>(),
        ButtonsController,
        LifecycleObserver,
        ButtonsViewMvc.Listener,
        HideOnSoftKeyboard.Listener
{

    private val messageHandler = boundAct.componentRoot.messageHandler
    protected val repo = boundAct.repo

    init {
        boundAct.bindObserver(this)
    }

    // region lifecycle functions

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    open fun onCreate() {
        viewMvc.registerListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy() {
        hideOnSoftKeyboard?.unregisterListener(this)
        viewMvc.unregisterListener(this)
    }

    // endregion lifecycle functions

    override fun reset() {
        viewMvc.btnPrevText = messageHandler.getString(StringMessage.btn_prev)
        viewMvc.btnNextText = messageHandler.getString(StringMessage.btn_next)
        viewMvc.btnCenterText = messageHandler.getString(StringMessage.btn_add)
        viewMvc.btnNextVisible = false
        viewMvc.btnCenterVisible = false
        viewMvc.btnPrevVisible = false
        viewMvc.btnChangeVisible = false
    }

    override fun reset(flow: Flow) {
        reset()
        viewMvc.btnNextVisible = flow.hasNext
        viewMvc.btnPrevVisible = flow.hasPrev
    }

    // region ButtonsViewMvc.Listener

    override fun onBtnPrevClicked(view: View) {
        wasSkip = false
        wasNext = false
        wasPrev = true
        if (confirmButton(Button.BTN_PREV)) {
            onPrev()
        }
        clearSoftKeyboard(view)
    }

    private fun onPrev() {
        wasNext = false
        wasPrev = true
        dispatchButtonEvent(Button.BTN_PREV)
    }

    override fun onBtnNextClicked(view: View) {
        wasSkip = false
        wasNext = true
        wasPrev = false
        if (confirmButton(Button.BTN_NEXT)) {
            onNext()
        }
        clearSoftKeyboard(view)
    }

    private fun onNext() {
        wasNext = true
        wasPrev = false
        dispatchButtonEvent(Button.BTN_NEXT)
    }

    override fun onBtnCenterClicked(view: View) {
        wasNext = false
        wasPrev = false
        dispatchButtonEvent(Button.BTN_CENTER)
    }

    override fun onBtnChangeClicked(view: View) {
        wasNext = false
        wasPrev = false
        dispatchButtonEvent(Button.BTN_CHANGE)
    }

    // endregion ButtonsViewMvc.Listener

    // region ButtonsController.Listener

    private fun dispatchButtonEvent(action: Button) {
        for (listener in listeners) {
            listener.onButtonEvent(action)
        }
    }

    private fun confirmButton(action: Button): Boolean {
        var flag = true
        for (listener in listeners) {
            if (!listener.onButtonConfirm(action)) {
                flag = false
            }
        }
        return flag
    }

    override var hideOnSoftKeyboard: HideOnSoftKeyboard? = null
        set(value) {
            value?.let {
                field?.unregisterListener(this)
                value.registerListener(this)
            } ?: run {
                field?.unregisterListener(this)
            }
            field = value
        }

    private fun clearSoftKeyboard(view: View) {
        hideOnSoftKeyboard?.hideKeyboard(view)
    }

    // endregion ButtonsController.Listener

    // region SoftKeyboardDetect.Listener

    override fun onSoftKeyboardVisible() {
        viewMvc.showing = false
    }

    override fun onSoftKeyboardHidden() {
        viewMvc.showing = true
    }

    // endregion SoftKeyboardDetect.Listener

    // region support for ButtonsUseCase

    override var wasNext = false
    override var wasSkip = false
    override var wasPrev = false

    override var nextVisible: Boolean
        get() = viewMvc.btnNextVisible
        set(value) {
            viewMvc.btnNextVisible = value
        }
    override var prevVisible: Boolean
        get() = viewMvc.btnPrevVisible
        set(value) {
            viewMvc.btnPrevVisible = value
        }
    override var centerVisible: Boolean
        get() = viewMvc.btnCenterVisible
        set(value) {
            viewMvc.btnCenterVisible = value
        }

    override var nextText: String?
        get() = viewMvc.btnNextText
        set(value) {
            viewMvc.btnNextText = value
        }
    override var prevText: String?
        get() = viewMvc.btnPrevText
        set(value) {
            viewMvc.btnPrevText = value
        }
    override var centerText: String?
        get() = viewMvc.btnCenterText
        set(value) {
            viewMvc.btnCenterText = value
        }

    override fun skip() {
        wasSkip = true
        if (wasNext) {
            onNext()
        } else {
            onPrev()
        }
    }

    override fun prev() {
        dispatch(Button.BTN_PREV)
    }

    override fun next() {
        dispatch(Button.BTN_NEXT)
    }

    override fun center() {
        dispatch(Button.BTN_CENTER)
    }
    override fun back() {
        wasNext = false
        skip()
    }

    // endregion support for ButtonsUseCase

    private fun dispatch(button: Button) {
        dispatchButtonEvent(button)
    }

}
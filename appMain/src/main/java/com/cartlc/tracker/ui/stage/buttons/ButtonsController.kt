package com.cartlc.tracker.ui.stage.buttons

import android.content.Context
import android.view.View
import androidx.lifecycle.*
import com.callassistant.util.observable.BaseObservableImpl
import com.cartlc.tracker.model.event.Button
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.FlowUseCase
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.msg.StringMessage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.ui.bits.SoftKeyboardDetect

open class ButtonsController(
        boundAct: BoundAct,
        private val viewMvc: ButtonsViewMvc,
        private val prefHelper: PrefHelper
) : BaseObservableImpl<ButtonsUseCase.Listener>(), ButtonsUseCase, LifecycleObserver, ButtonsViewMvc.Listener, SoftKeyboardDetect.Listener, FlowUseCase.Listener {

    protected val messageHandler = boundAct.componentRoot.messageHandler
    private var softKeyboardDetect: SoftKeyboardDetect? = null
    private val activity = boundAct.act
    protected val repo = boundAct.repo
    private val curFlowValue: Flow
        get() = repo.curFlowValue
    private val isCenterButtonEdit: Boolean
        get() = curFlowValue.stage == Stage.COMPANY && prefHelper.isLocalCompany

    init {
        boundAct.bindObserver(this)
    }

    // region lifecycle functions

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    open fun onCreate() {
        repo.flowUseCase.registerListener(this)
        viewMvc.registerListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy() {
        softKeyboardDetect?.unregisterListener(this)
        repo.flowUseCase.unregisterListener(this)
        viewMvc.unregisterListener(this)
    }

    // endregion lifecycle functions

    open fun install(softKeyboardDetect: SoftKeyboardDetect) {
        this.softKeyboardDetect = softKeyboardDetect
        softKeyboardDetect.registerListener(this)
    }

    fun uninstall() {
        softKeyboardDetect?.unregisterListener(this)
        softKeyboardDetect = null
    }

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
        viewMvc.btnPrevText = messageHandler.getString(StringMessage.btn_prev)
        viewMvc.btnNextText = messageHandler.getString(StringMessage.btn_next)
        viewMvc.btnCenterText = messageHandler.getString(StringMessage.btn_add)
        viewMvc.btnNextVisible = false
        viewMvc.btnCenterVisible = false
        viewMvc.btnPrevVisible = false
        viewMvc.btnChangeVisible = false
        viewMvc.btnNextVisible = flow.hasNext
        viewMvc.btnPrevVisible = flow.hasPrev
    }

    override fun onStageChanged(flow: Flow) {
    }

    // endregion FlowUseCase.Listener

    // region ButtonsViewMvc.Listener

    override fun onBtnPrevClicked(view: View) {
        wasSkip = false
        if (confirmButton(Button.BTN_PREV)) {
            onPrev()
        }
        clearSoftKeyboard(view)
    }

    private fun onPrev() {
        wasNext = false
        val currentCurFlowValue = curFlowValue.stage
        dispatchButtonEvent(Button.BTN_PREV)
        if (currentCurFlowValue == curFlowValue.stage) {
            curFlowValue.prev()
        }
    }

    override fun onBtnNextClicked(view: View) {
        wasSkip = false
        if (confirmButton(Button.BTN_NEXT)) {
            onNext()
        }
        clearSoftKeyboard(view)
    }

    private fun onNext() {
        wasNext = true
        val currentCurFlowValue = curFlowValue.stage
        dispatchButtonEvent(Button.BTN_NEXT)
        // TODO: Clean this up so I don't have both dispatch AND the normal next button.
        if (currentCurFlowValue == curFlowValue.stage) {
            curFlowValue.next()
        }
    }

    override fun onBtnCenterClicked(view: View) {
        wasNext = false
        dispatchButtonEvent(Button.BTN_CENTER)
        curFlowValue.center()
    }

    override fun onBtnChangeClicked(view: View) {
        wasNext = false
        dispatchButtonEvent(Button.BTN_CHANGE)
    }

    private fun dispatchButtonEvent(action: Button) {
        val live = mutableListOf<ButtonsUseCase.Listener>()
        for (listener in listeners) {
            if (listener.onButtonLive) {
                live.add(listener)
            }
        }
        for (listener in live) {
            listener.onButtonEvent(action)
        }
    }

    private fun confirmButton(action: Button): Boolean {
        var okay = true
        for (listener in listeners) {
            if (!listener.onButtonConfirm(action)) {
                okay = false
            }
        }
        return okay
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
        TBApplication.hideKeyboard(activity as Context, view)
    }

    // endregion support functions

    // region support for ButtonsUseCase

    override var wasNext = false
    override var wasSkip = false

    override var nextVisible: Boolean
        get() = viewMvc.btnNextVisible
        set(value) { viewMvc.btnNextVisible = value }
    override var prevVisible: Boolean
        get() = viewMvc.btnPrevVisible
        set(value) { viewMvc.btnPrevVisible = value }
    override var centerVisible: Boolean
        get() = viewMvc.btnCenterVisible
        set(value) { viewMvc.btnCenterVisible = value }

    override var nextText: String?
        get() = viewMvc.btnNextText
        set(value) { viewMvc.btnNextText = value }
    override var prevText: String?
        get() = viewMvc.btnPrevText
        set(value) { viewMvc.btnPrevText = value }
    override var centerText: String?
        get() = viewMvc.btnCenterText
        set(value) { viewMvc.btnCenterText = value }

    override fun skip() {
        wasSkip = true
        if (wasNext) {
            onNext()
        } else {
            onPrev()
        }
    }

    override fun dispatch(button: Button) {
        dispatchButtonEvent(button)
    }

    override fun checkCenterButtonIsEdit() {
        viewMvc.btnCenterText = if (isCenterButtonEdit) {
            messageHandler.getString(StringMessage.btn_edit)
        } else {
            messageHandler.getString(StringMessage.btn_add)
        }
    }

    // endregion support for ButtonsUseCase
}
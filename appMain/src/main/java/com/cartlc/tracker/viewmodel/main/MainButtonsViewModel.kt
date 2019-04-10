/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.viewmodel.main

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.BuildConfig
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.data.DataNote
import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.model.event.Button
import com.cartlc.tracker.model.flow.*
import com.cartlc.tracker.model.msg.ErrorMessage
import com.cartlc.tracker.model.msg.StringMessage
import com.cartlc.tracker.model.misc.TruckStatus
import com.cartlc.tracker.model.msg.MessageHandler
import com.cartlc.tracker.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.viewmodel.frag.ButtonsViewModel

class MainButtonsViewModel(
        boundFrag: BoundFrag,
        private val messageHandler: MessageHandler
) : ButtonsViewModel(boundFrag.repo, messageHandler), LifecycleObserver, FlowUseCase.Listener {

    val isLocalCompany: Boolean
        get() = db.tableAddress.isLocalCompanyOnly(prefHelper.company)

    private var curFlowValue: Flow
        get() = repo.curFlowValue
        set(value) { repo.curFlowValue = value }

    private val isCenterButtonEdit: Boolean
        get() = curFlowValue.stage == Stage.COMPANY && isLocalCompany

    var save: (isNext: Boolean) -> Boolean = { false }

    internal var wasNext: Boolean = false
    internal var didAutoSkip: Boolean = false

    init {
        boundFrag.bindObserver(this)
        repo.flowUseCase.registerListener(this)
    }

    // region lifecycle

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        repo.flowUseCase.unregisterListener(this)
    }

    // endregion lifecycle

    fun dispatchActionEvent(action: Action) {
        repo.dispatchActionEvent(action)
    }

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
        showChangeButtonValue = false
        showCenterButtonValue = false
        centerTextValue = messageHandler.getString(StringMessage.btn_add)
        showNextButtonValue = flow.hasNext
        nextTextValue = messageHandler.getString(StringMessage.btn_next)
        showPrevButtonValue = flow.hasPrev
        prevTextValue = messageHandler.getString(StringMessage.btn_prev)
    }

    override fun onStageChanged(flow: Flow) {
    }

    // region FlowUseCase.Listener

    fun checkCenterButtonIsEdit() {
        centerTextValue = if (isCenterButtonEdit) {
            messageHandler.getString(StringMessage.btn_edit)
        } else {
            messageHandler.getString(StringMessage.btn_add)
        }
    }

    private fun btnPrev() {
        if (confirmPrev()) {
            wasNext = false
            repo.companyEditing = null
            curFlowValue.prev()
        }
    }

    private fun confirmPrev(): Boolean {
        return save(false)
    }

    private fun btnNext(wasAutoSkip: Boolean = false) {
        if (confirmNext()) {
            didAutoSkip = wasAutoSkip
            repo.companyEditing = null
            advance()
        }
    }

    private fun confirmNext(): Boolean {
        return save(true)
    }

    private fun btnCenter() {
        if (confirmCenter()) {
            wasNext = false
            curFlowValue.center()
        }
    }

    private fun confirmCenter(): Boolean {
        when (curFlowValue.stage) {
            Stage.LOGIN -> dispatchActionEvent(Action.SAVE_LOGIN_INFO)
            else -> {}
        }
        return true
    }

    fun btnProfile() {
        save(false)
        curFlowValue = LoginFlow()
    }

    internal fun skip() {
        if (wasNext) {
            btnNext(true)
        } else {
            btnPrev()
        }
    }

    private fun advance() {
        wasNext = true
        curFlowValue.next()
    }

    fun showNoteErrorOk() {
        if (BuildConfig.DEBUG) {
            advance()
        } else {
            btnNext()
        }
    }

    fun onButtonDispatch(button: Button) {
        when (button) {
            Button.BTN_PREV -> btnPrev()
            Button.BTN_NEXT -> btnNext()
            Button.BTN_CENTER -> btnCenter()
            else -> {
            }
        }
    }

}
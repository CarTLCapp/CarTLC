/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.viewmodel.frag

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.data.DataEntry
import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.model.flow.*
import com.cartlc.tracker.model.msg.MessageHandler
import com.cartlc.tracker.model.msg.StringMessage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.viewmodel.BaseViewModel

class ConfirmationViewModel(
        private val boundFrag: BoundFrag,
        private val messageHandler: MessageHandler
) : BaseViewModel(), LifecycleObserver, FlowUseCase.Listener {

    private val repo = boundFrag.repo

    private val prefHelper: PrefHelper
        get() = repo.prefHelper

    private var curFlowValue: Flow
        get() = repo.curFlowValue
        set(value) {
            repo.curFlowValue = value
        }

    var showing = ObservableBoolean(false)

    var showingValue: Boolean
        get() = showing.get()
        set(value) {
            showing.set(value)
        }

    private var curEntry: DataEntry? = null

    var dispatchActionEvent: (action: Action) -> Unit = {}

    lateinit var buttonsViewModel: ButtonsViewModel
    lateinit var titleViewModel: TitleViewModel

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

    fun onConfirmOkay() {
        repo.add(curEntry!!)
        prefHelper.clearLastEntry()
        curEntry = null
        curFlowValue = CurrentProjectFlow()
        dispatchActionEvent(Action.PING)
    }

    // region FlowUseCase.Listener
    override fun onStageChangedAboutTo(flow: Flow) {
        showingValue = false
    }

    override fun onStageChanged(flow: Flow) {
        when (flow.stage) {
            Stage.STATUS -> {
                curEntry = null
            }
            Stage.CONFIRM -> {
                buttonsViewModel.nextTextValue = messageHandler.getString(StringMessage.btn_confirm)
                showingValue = true
                titleViewModel.titleValue = messageHandler.getString(StringMessage.title_confirmation)
                curEntry = prefHelper.saveEntry()
                curEntry?.let { entry -> dispatchActionEvent(Action.CONFIRMATION_FILL(entry)) }
                dispatchActionEvent(Action.STORE_ROTATION)
            }
            else -> {}
        }
    }

    // endregion FlowUseCase.Listener
}
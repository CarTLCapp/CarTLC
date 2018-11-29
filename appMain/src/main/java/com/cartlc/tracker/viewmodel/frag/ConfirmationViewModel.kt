/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.viewmodel.frag

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.data.DataEntry
import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.model.flow.CurrentProjectFlow
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.LoginFlow
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.misc.StringMessage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.viewmodel.BaseViewModel

class ConfirmationViewModel(private val repo: CarRepository) : BaseViewModel() {

    private val prefHelper: PrefHelper
        get() = repo.prefHelper

    private val curFlow: MutableLiveData<Flow>
        get() = repo.curFlow

    private var curFlowValue: Flow
        get() = curFlow.value ?: LoginFlow()
        set(value) {
            curFlow.value = value
        }

    var showing = ObservableBoolean(false)

    var showingValue: Boolean
        get() = showing.get()
        set(value) {
            showing.set(value)
        }

    private var curEntry: DataEntry? = null

    var dispatchActionEvent: (action: Action) -> Unit = {}
    var getString: (msg: StringMessage) -> String = { "" }

    lateinit var buttonsViewModel: ButtonsViewModel
    lateinit var titleViewModel: TitleViewModel

    fun onConfirmOkay() {
        repo.add(curEntry!!)
        prefHelper.clearLastEntry()
        curEntry = null
        curFlowValue = CurrentProjectFlow()
        dispatchActionEvent(Action.PING)
    }

    fun onStageChanged(flow: Flow) {
        when (flow.stage) {
            Stage.STATUS -> {
                curEntry = null
            }
            Stage.CONFIRM -> {
                buttonsViewModel.nextTextValue = getString(StringMessage.btn_confirm)
                showingValue = true
                titleViewModel.titleValue = getString(StringMessage.title_confirmation)
                curEntry = prefHelper.saveEntry()
                curEntry?.let { entry -> dispatchActionEvent(Action.CONFIRMATION_FILL(entry)) }
                dispatchActionEvent(Action.STORE_ROTATION)
            }
            else -> {}
        }
    }
}
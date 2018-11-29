package com.cartlc.tracker.viewmodel.frag

import android.app.Activity
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.data.DataEntry
import com.cartlc.tracker.model.flow.*
import com.cartlc.tracker.model.misc.StringMessage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.viewmodel.BaseViewModel
import javax.inject.Inject

class ConfirmationViewModel(private val act: Activity) : BaseViewModel() {

    @Inject
    lateinit var repo: CarRepository

    val db: DatabaseTable
        get() = repo.db

    val prefHelper: PrefHelper
        get() = repo.prefHelper

    private val app: TBApplication
        get() = act.applicationContext as TBApplication

    val curFlow: MutableLiveData<Flow>
        get() = repo.curFlow

    private var curFlowValue: Flow
        get() = curFlow.value ?: LoginFlow()
        set(value) {
            curFlow.value = value
        }

    init {
        app.carRepoComponent.inject(this)
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
        curFlowValue = CurrentProjectFlow()
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
        }
    }
}
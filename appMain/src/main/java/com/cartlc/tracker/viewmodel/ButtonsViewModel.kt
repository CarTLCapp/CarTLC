package com.cartlc.tracker.viewmodel

import android.app.Activity
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.R
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.flow.Action
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.LoginFlow
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.misc.StringMessage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.app.TBApplication
import javax.inject.Inject

class ButtonsViewModel(private val act: Activity) : BaseViewModel() {

    @Inject
    lateinit var repo: CarRepository

    private val app: TBApplication
        get() = act.applicationContext as TBApplication

    init {
        app.carRepoComponent.inject(this)
    }

    var showing = ObservableBoolean(true)
    var prevText = ObservableField<String>(act.getString(R.string.btn_prev))
    var nextText = ObservableField<String>(act.getString(R.string.btn_next))
    var centerText = ObservableField<String>(act.getString(R.string.btn_add))
    var showPrevButton = ObservableBoolean(false)
    var showNextButton = ObservableBoolean(false)
    var showCenterButton = ObservableBoolean(false)
    var showChangeButton = ObservableBoolean(false)

    var showingValue: Boolean
        get() = showing.get()
        set(value) = showing.set(value)
    var prevTextValue: String?
        get() = prevText.get()
        set(value) = prevText.set(value)
    var nextTextValue: String?
        get() = nextText.get()
        set(value) = nextText.set(value)
    var centerTextValue: String?
        get() = centerText.get()
        set(value) = centerText.set(value)
    var showPrevButtonValue: Boolean
        get() = showPrevButton.get()
        set(value) = showPrevButton.set(value)
    var showNextButtonValue: Boolean
        get() = showNextButton.get()
        set(value) = showNextButton.set(value)
    var showCenterButtonValue: Boolean
        get() = showCenterButton.get()
        set(value) = showCenterButton.set(value)
    var showChangeButtonValue: Boolean
        get() = showChangeButton.get()
        set(value) = showChangeButton.set(value)

    private val db: DatabaseTable
        get() = repo.db

    private val prefHelper: PrefHelper
        get() = repo.prefHelper

    private val curFlow: MutableLiveData<Flow>
        get() = repo.curFlow

    private var curFlowValue: Flow
        get() = curFlow.value ?: LoginFlow()
        set(value) {
            curFlow.value = value
        }

    val isLocalCompany: Boolean
        get() = db.tableAddress.isLocalCompanyOnly(prefHelper.company)

    val isCenterButtonEdit: Boolean
        get() = curFlowValue.stage == Stage.COMPANY && isLocalCompany

    var getString: (msg: StringMessage) -> String = { "" }
    var dispatchButtonEvent: (action: Action) -> Unit = {}

    fun reset(flow: Flow) {
        showChangeButtonValue = false
        showCenterButtonValue = false
        centerTextValue = getString(StringMessage.btn_add)
        showNextButtonValue = flow.next != null
        nextTextValue = getString(StringMessage.btn_next)
        showPrevButtonValue = flow.prev != null
        prevTextValue = getString(StringMessage.btn_prev)
    }

    fun checkCenterButtonIsEdit() {
        if (isCenterButtonEdit) {
            centerTextValue = getString(StringMessage.btn_edit)
        } else {
            centerTextValue = getString(StringMessage.btn_add)
        }
    }
}
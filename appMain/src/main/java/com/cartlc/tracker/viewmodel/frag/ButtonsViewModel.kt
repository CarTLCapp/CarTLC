/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.viewmodel.frag

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.event.Button
import com.cartlc.tracker.model.event.ButtonEvent
import com.cartlc.tracker.model.flow.*
import com.cartlc.tracker.model.misc.StringMessage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.viewmodel.BaseViewModel

open class ButtonsViewModel(protected val repo: CarRepository) : BaseViewModel() {

    var getString: (msg: StringMessage) -> String = { "" }

    var showing = ObservableBoolean(true)
    var prevText = ObservableField<String>(getString(StringMessage.btn_prev))
    var nextText = ObservableField<String>(getString(StringMessage.btn_next))
    var centerText = ObservableField<String>(getString(StringMessage.btn_add))
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

    protected val db: DatabaseTable
        get() = repo.db

    protected val prefHelper: PrefHelper
        get() = repo.prefHelper

    protected val curFlow: MutableLiveData<Flow>
        get() = repo.curFlow

    protected var curFlowValue: Flow
        get() = curFlow.value ?: LoginFlow()
        set(value) {
            curFlow.value = value
        }

    private val handleButton: MutableLiveData<ButtonEvent> by lazy {
        MutableLiveData<ButtonEvent>()
    }

    fun handleButtonEvent(): LiveData<ButtonEvent> = handleButton

    fun dispatchButtonEvent(action: Button) {
        handleButton.value = ButtonEvent(action)
    }

    fun reset() {
        prevText.set(getString(StringMessage.btn_prev))
        nextText.set(getString(StringMessage.btn_next))
        centerText.set(getString(StringMessage.btn_add))
    }

}
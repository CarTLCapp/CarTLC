package com.cartlc.tracker.viewmodel

import android.app.Activity
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cartlc.tracker.R
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.flow.Action
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.ui.app.TBApplication
import kotlinx.android.synthetic.main.frag_buttons.view.*
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

    fun dispatchButtonEvent(action: Action) {
        dispatchActionEvent(action)
    }
}
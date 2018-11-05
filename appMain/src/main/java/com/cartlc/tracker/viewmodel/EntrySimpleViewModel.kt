package com.cartlc.tracker.viewmodel

import android.app.Activity
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.cartlc.tracker.R
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.ui.app.TBApplication
import javax.inject.Inject

class EntrySimpleViewModel(private val act: Activity) : BaseViewModel() {

    @Inject
    lateinit var repo: CarRepository

    private val app: TBApplication
        get() = act.applicationContext as TBApplication

    init {
        app.carRepoComponent.inject(this)
    }

    fun dispatchReturnPressedEvent(arg: String) {
        dispatchGenericEvent(arg)
    }

    var showing = ObservableBoolean(true)
    var simpleText = ObservableField<String>("")
    var simpleHint = ObservableField<String>()
    var helpText = ObservableField<String>()
    var simpleEms = ObservableInt(20)
    var title = ObservableField<String>()

    var showingValue: Boolean
        get() = showing.get()
        set(value) = showing.set(value)
    var simpleTextValue: String?
        get() = simpleText.get()
        set(value) = simpleText.set(value)
    var simpleHintValue: String?
        get() = simpleHint.get()
        set(value) = simpleHint.set(value)
    var helpTextValue: String?
        get() = helpText.get()
        set(value) = helpText.set(value)
    var simpleEmsValue: Int
        get() = simpleEms.get()
        set(value) = simpleEms.set(value)
    var titleValue: String?
        get() = title.get()
        set(value) = title.set(value)

    init {
        simpleEmsValue = act.resources.getInteger(R.integer.entry_simple_ems)
    }
}
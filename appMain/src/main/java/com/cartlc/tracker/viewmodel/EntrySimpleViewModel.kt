package com.cartlc.tracker.viewmodel

import android.app.Activity
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
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

    var showing = ObservableBoolean(false)
    var simpleText = ObservableField<String>("")
    var simpleHint = ObservableField<String>()
    var helpText = ObservableField<String>()

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
}
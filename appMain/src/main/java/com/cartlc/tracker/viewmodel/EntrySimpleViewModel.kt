package com.cartlc.tracker.viewmodel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.databinding.FragEntrySimpleBinding
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.misc.EntrySimpleReturnEvent
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.ui.app.TBApplication
import javax.inject.Inject

class EntrySimpleViewModel(
        private val act: Activity,
        private val binding: FragEntrySimpleBinding
) : BaseViewModel() {

    @Inject
    lateinit var repo: CarRepository

    private val app: TBApplication
        get() = act.applicationContext as TBApplication

    init {
        app.carRepoComponent.inject(this)
    }

    private val _handleEntrySimpleReturnEvent: MutableLiveData<EntrySimpleReturnEvent> by lazy {
        MutableLiveData<EntrySimpleReturnEvent>()
    }

    fun handleEntrySimpleReturnEvent(): LiveData<EntrySimpleReturnEvent> = _handleEntrySimpleReturnEvent

    fun invokeEntrySimpleReturnEvent(value: String) {
        _handleEntrySimpleReturnEvent.value = EntrySimpleReturnEvent(value)
    }

    var showing: Boolean = false
        set(value) {
            field = value
            binding.invalidateAll()
        }

    var simpleText: String = ""
        set(value) {
            field = value
            binding.invalidateAll()
        }

    var simpleHint: String? = null
        set(value) {
            field = value
            binding.invalidateAll()
        }

    var helpText: String? = null
        set(value) {
            field = value
            binding.invalidateAll()
        }

}
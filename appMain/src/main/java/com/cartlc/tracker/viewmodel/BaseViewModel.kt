package com.cartlc.tracker.viewmodel

import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cartlc.tracker.model.flow.Action
import com.cartlc.tracker.model.event.ActionEvent
import com.cartlc.tracker.model.misc.EntryHint
import com.cartlc.tracker.model.misc.ErrorMessage
import com.cartlc.tracker.model.event.GenericEvent

open class BaseViewModel : ViewModel(), Observable {

    val error: MutableLiveData<ErrorMessage> by lazy {
        MutableLiveData<ErrorMessage>()
    }
    val entryHint: MutableLiveData<EntryHint> by lazy {
        MutableLiveData<EntryHint>()
    }
    var errorValue: ErrorMessage
        get() = error.value!!
        set(value) {
            error.value = value
        }

    private val callbacks: PropertyChangeRegistry = PropertyChangeRegistry()

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }

    /**
     * Notifies listeners that all properties of this instance have changed.
     */
    fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    /**
     * Notifies listeners that a specific property has changed. The getter for the property
     * that changes should be marked with [Bindable] to generate a field in
     * `BR` to be used as `fieldId`.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }

    // ActionEvent

    protected val _handleAction: MutableLiveData<ActionEvent> by lazy {
        MutableLiveData<ActionEvent>()
    }

    fun handleActionEvent(): LiveData<ActionEvent> = _handleAction

    protected fun dispatchActionEvent(action: Action) {
        _handleAction.value = ActionEvent(action)
    }

    // Generic Event

    protected val _handleGeneric: MutableLiveData<GenericEvent> by lazy {
        MutableLiveData<GenericEvent>()
    }

    fun handleGenericEvent(): LiveData<GenericEvent> = _handleGeneric

    protected fun dispatchGenericEvent(arg: String) {
        _handleGeneric.value = GenericEvent(arg)
    }

}
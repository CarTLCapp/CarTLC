package com.cartlc.tracker.viewmodel

import android.app.Activity
import android.text.InputType
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.R
import com.cartlc.tracker.model.flow.Action
import com.cartlc.tracker.model.flow.ButtonDialog
import com.cartlc.tracker.ui.app.TBApplication

class EntrySimpleViewModel(private val act: Activity) : BaseViewModel() {

    private val app: TBApplication
        get() = act.applicationContext as TBApplication

    fun dispatchReturnPressedEvent(arg: String) {
        dispatchActionEvent(Action.RETURN_PRESSED(arg))
    }

    var showing = ObservableBoolean(true)
    var simpleText = ObservableField<String>("")
    var simpleHint = ObservableField<String>()
    var helpText = ObservableField<String>()
    var simpleEms = ObservableInt(20)
    var title = ObservableField<String>()
    var checkedButton = ObservableInt(0)
    var showChecked = ObservableBoolean(false)
    var showEditText = ObservableBoolean(true)

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
    var showCheckedValue: Boolean
        get() = showChecked.get()
        set(value) { showChecked.set(value) }
    var showEditTextValue: Boolean
        get() = showEditText.get()
        set(value) = showEditText.set(value)
    private var checkedButtonValue: Int
        get() = checkedButton.get()
        set(value) = checkedButton.set(value)
    var checkedButtonBooleanValue: Boolean?
        get() {
            if (checkedButtonValue == R.id.entry_radio_yes) {
                return true
            }
            if (checkedButtonValue == R.id.entry_radio_no) {
                return false
            }
            return null
        }
        set(value) {
            if (value == null) {
                checkedButtonValue = 0
                showEditTextValue = false
            } else if (value) {
                checkedButtonValue = R.id.entry_radio_yes
                showEditTextValue = true
            } else {
                checkedButtonValue = R.id.entry_radio_no
                showEditTextValue = true
            }
        }

    val hasCheckedValue: Boolean
        get() = checkedButtonValue != 0

    var afterTextChangedListener: (value: String) -> Unit = {}

    val inputType: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    var inputTypeValue: Int
        get() = inputType.value ?: InputType.TYPE_CLASS_TEXT
        set(value) {
            inputType.value = value
        }

    var dispatchActionEvent: (action: Action) -> Unit = {}

    init {
        simpleEmsValue = act.resources.getInteger(R.integer.entry_simple_ems)
    }

    fun afterTextChanged(s: CharSequence) {
        afterTextChangedListener(s.toString())
    }

    fun onCheckedChanged(id: Int) {
        if (id == R.id.entry_radio_yes) {
            showEditTextValue = true
            dispatchActionEvent(Action.BUTTON_DIALOG(ButtonDialog.YES))
        } else if (id == R.id.entry_radio_no) {
            showEditTextValue = false
            dispatchActionEvent(Action.BUTTON_DIALOG(ButtonDialog.NO))
        } else {
            showEditTextValue = false
        }
    }

    fun reset() {
        showing.set(false)
        helpText.set(null)
        inputTypeValue = InputType.TYPE_TEXT_FLAG_CAP_WORDS
    }

}
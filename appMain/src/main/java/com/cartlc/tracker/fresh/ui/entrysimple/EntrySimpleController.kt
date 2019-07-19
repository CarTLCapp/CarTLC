package com.cartlc.tracker.fresh.ui.entrysimple

import android.text.InputType
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.event.Action
import com.cartlc.tracker.fresh.model.event.ButtonDialog
import com.cartlc.tracker.fresh.model.flow.Flow
import com.cartlc.tracker.fresh.model.flow.FlowUseCase
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleViewMvc.YesNo

class EntrySimpleController(
        boundAct: BoundAct,
        private val view: EntrySimpleViewMvc
) : LifecycleObserver, EntrySimpleUseCase, EntrySimpleViewMvc.Listener, FlowUseCase.Listener {

    private val repo = boundAct.repo

    init {
        boundAct.bindObserver(this)
        repo.flowUseCase.registerListener(this)
    }

    // region lifecycle

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        view.registerListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        view.unregisterListener(this)
        repo.flowUseCase.unregisterListener(this)
    }

    // endregion lifecycle

    override var checkedButtonBooleanValue: Boolean?
        get() {
            return when (view.checkedButton) {
                R.id.entry_radio_yes -> true
                R.id.entry_radio_no -> false
                else -> null
            }
        }
        set(value) {
            when {
                value == null -> {
                    view.checkedButton = 0
                    view.entryEditTextVisible = false
                }
                value -> {
                    view.checkedButton = R.id.entry_radio_yes
                    view.entryEditTextVisible = true
                }
                else -> {
                    view.checkedButton = R.id.entry_radio_no
                    view.entryEditTextVisible = true
                }
            }
        }

    override val hasCheckedValue: Boolean
        get() = view.checkedButton > 0

    override var afterTextChangedListener: (value: String) -> Unit = {}
    override var dispatchActionEvent: (action: Action) -> Unit = {}
    override var entryTextValue: String?
        get() = view.entryEditTextValue
        set(value) { view.entryEditTextValue = value }
    override var emsValue: Int
        get() = view.entryEditTextEms
        set(value) { view.entryEditTextEms = value }
    override var helpValue: String?
        get() = view.entryHelpTextValue
        set(value) {
            view.entryHelpTextValue = value
            view.entryHelpTextVisible = true
        }
    override var hintValue: String
        get() = view.entryEditTextHint
        set(value) { view.entryEditTextHint = value }
    override var inputType: Int
        get() = view.entryEditTextInputType
        set(value) { view.entryEditTextInputType = value }
    override var showCheckedValue: Boolean
        get() = view.entryCheckedVisible
        set(value) { view.entryCheckedVisible = value }
    override var showing: Boolean
        get() = view.showing
        set(value) { view.showing = value }
    override var showEditTextValue: Boolean
        get() = view.entryEditTextVisible
        set(value) { view.entryEditTextVisible = value }
    override var titleValue: String?
        get() = view.title
        set(value) {
            view.titleVisible = true
            view.title = value
        }

    override fun editTextAfterTextChanged(value: String) {
        afterTextChangedListener.invoke(value)
    }

    override fun checkButtonChecked(checked: YesNo) {
        when (checked) {
            YesNo.YES -> {
                view.entryEditTextVisible = true
                dispatchActionEvent(Action.BUTTON_DIALOG(ButtonDialog.YES))
            }
            YesNo.NO -> {
                view.entryEditTextVisible = false
                dispatchActionEvent(Action.BUTTON_DIALOG(ButtonDialog.NO))
            }
            YesNo.NONE -> {
                view.entryEditTextVisible = false
            }
        }
    }

    private fun reset() {
        view.showing = false
        view.entryHelpTextValue = null
        view.entryHelpTextVisible = false
        view.entryCheckedVisible = false
        view.entryEditTextInputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
        simpleTextClear()
    }

    override fun simpleTextClear() {
        view.entryEditTextValue = " "
        view.entryEditTextValue = ""
    }

    override fun onEditTextReturn() {
        dispatchActionEvent(Action.RETURN_PRESSED(view.entryEditTextValue ?: ""))
    }

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
        reset()
    }

    override fun onStageChanged(flow: Flow) {
    }

    // endregion FlowUseCase.Listener
}
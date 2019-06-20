package com.cartlc.tracker.fresh.ui.entrysimple

import com.cartlc.tracker.model.event.Action

interface EntrySimpleUseCase {

    val hasCheckedValue: Boolean
    var afterTextChangedListener: (value: String) -> Unit
    var checkedButtonBooleanValue: Boolean?
    var dispatchActionEvent: (action: Action) -> Unit
    var entryTextValue: String?
    var emsValue: Int
    var helpValue: String?
    var hintValue: String
    var inputType: Int
    var showCheckedValue: Boolean
    var showEditTextValue: Boolean
    var showing: Boolean
    var titleValue: String?

    fun simpleTextClear()

}
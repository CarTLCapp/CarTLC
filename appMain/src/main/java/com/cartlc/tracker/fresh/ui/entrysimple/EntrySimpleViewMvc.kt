package com.cartlc.tracker.fresh.ui.entrysimple

import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvc

interface EntrySimpleViewMvc : ObservableViewMvc<EntrySimpleViewMvc.Listener> {

    enum class YesNo {
        YES,
        NO,
        NONE
    }

    interface Listener {

        fun editTextAfterTextChanged(value: String)
        fun checkButtonChecked(checked: YesNo)
        fun onEditTextReturn()

    }

    var showing: Boolean
    var title: String?
    var titleVisible: Boolean
    var entryCheckedVisible: Boolean
    var entryEditTextVisible: Boolean
    var entryHelpTextVisible: Boolean

    var entryEditTextEms: Int
    var entryEditTextHint: String
    var entryEditTextValue: String?
    var entryHelpTextValue: String?

    var checkedButton: Int
    var entryEditTextInputType: Int

}
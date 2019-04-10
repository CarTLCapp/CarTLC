package com.cartlc.tracker.ui.bits.entrysimple

import androidx.annotation.IdRes
import com.callassistant.util.viewmvc.ObservableViewMvc

interface EntrySimpleViewMvc : ObservableViewMvc<EntrySimpleViewMvc.Listener> {

    interface Listener {

        fun editTextAfterTextChanged(value: String)
        fun checkButtonChecked(@IdRes checkedId: Int)
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
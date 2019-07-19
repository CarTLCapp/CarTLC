package com.cartlc.tracker.fresh.ui.mainlist

import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.ui.common.observable.BaseObservable
import com.cartlc.tracker.fresh.model.misc.EntryHint

interface MainListUseCase : BaseObservable<MainListUseCase.Listener> {

    interface Listener {
        fun onEntryHintChanged(entryHint: EntryHint)
        fun onKeyValueChanged(key: String, keyValue: String?)
        fun onProjectGroupSelected(projectGroup: DataProjectAddressCombo)
    }

    val areNotesComplete: Boolean
    val notes: List<DataNote>

    var visible: Boolean
    var key: String?
    var keyValue: String?
    var simpleItems: List<String>

}
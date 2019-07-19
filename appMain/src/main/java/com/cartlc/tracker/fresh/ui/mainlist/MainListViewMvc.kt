package com.cartlc.tracker.fresh.ui.mainlist

import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvc
import com.cartlc.tracker.fresh.model.misc.EntryHint

interface MainListViewMvc : ObservableViewMvc<MainListViewMvc.Listener> {

    interface Listener {
        fun onEntryHintChanged(entryHint: EntryHint)
        fun onSimpleItemClicked(position: Int, value: String)
        fun onProjectGroupSelected(projectGroup: DataProjectAddressCombo)
        fun onRadioItemSelected(text: String)
    }

    enum class Adapter {
        SIMPLE,
        PROJECT,
        EQUIPMENT,
        RADIO,
        NOTE_ENTRY
    }

    val numNotes: Int
    val notes: List<DataNote>

    var visible: Boolean
    var emptyVisible: Boolean
    var simpleItems: List<String>
    var radioItems: List<String>
    var radioSelectedText: String?
    var adapter: Adapter

    var scrollToPosition: Int

    fun setSimpleNoneSelected()
    fun setSimpleSelected(value: String): Int

}
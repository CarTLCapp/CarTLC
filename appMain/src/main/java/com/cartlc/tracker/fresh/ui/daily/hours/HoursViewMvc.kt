package com.cartlc.tracker.fresh.ui.daily.hours

import androidx.annotation.StringRes
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvc
import com.cartlc.tracker.fresh.ui.daily.project.ProjectSelect
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvc
import com.cartlc.tracker.fresh.ui.title.TitleViewMvc

interface HoursViewMvc : ObservableViewMvc<HoursViewMvc.Listener> {

    interface Listener {
        fun editTextAfterTextChanged(value: String)
        fun onEditTextReturn()
        fun onDateButtonClicked()
        fun onDateEntered(value: Long)
        fun onTimeEntered(value: Long)
        fun onProjectTypeSelected(which: ProjectSelect)
        fun onItemSelected(position: Int, item: String)
        fun isItemSelected(position: Int, item: String): Boolean
    }

    val titleViewMvc: TitleViewMvc
    val buttonsViewMvc: ButtonsViewMvc
    var buttonsViewVisible: Boolean
    var dayVisible: Boolean
    var dayTextValue: String?
    var startTimeVisible: Boolean
    var startTimeValue: String?
    var endTimeVisible: Boolean
    var endTimeValue: String?
    var entryVisible: Boolean
    var entryEditTextValue: String?
    var projectSelectVisible: Boolean
    var projectsSelectRecentEnabled: Boolean
    var projectsSelectAllEnabled: Boolean
    var projectSelectWhich: ProjectSelect
    var itemListVisible: Boolean
    fun setInstruction(@StringRes text: Int)
    fun invokeDatePicker()
    fun invokeTimePicker()
    fun prepareItemList(items: List<String>)
    fun refreshItemList()

}

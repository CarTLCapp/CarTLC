package com.cartlc.tracker.fresh.ui.daar

import androidx.annotation.StringRes
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvc
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvc
import com.cartlc.tracker.fresh.ui.title.TitleViewMvc

interface DaarViewMvc : ObservableViewMvc<DaarViewMvc.Listener> {

    enum class ProjectSelect {
        PROJECT_RECENT,
        PROJECT_ALL,
        PROJECT_OTHER
    }

    interface Listener {
        fun editTextAfterTextChanged(value: String)
        fun onEditTextReturn()
        fun onDateButtonClicked()
        fun onDateEntered(value: Long)
        fun onTimeEntered(value: Long)
        fun onProjectTypeSelected(which: ProjectSelect)
        fun onProjectItemSelected(position: Int, item: String)
        fun isProjectSelected(position: Int, item: String): Boolean
    }

    val titleViewMvc: TitleViewMvc
    val buttonsViewMvc: ButtonsViewMvc
    var buttonsViewVisible: Boolean
    var timeDateVisible: Boolean
    var timeDateTextValue: String?
    var entryVisible: Boolean
    var entryEditTextValue: String?
    var projectSelectVisible: Boolean
    var projectsSelectRecentEnabled: Boolean
    var projectsSelectAllEnabled: Boolean
    var projectSelectWhich: ProjectSelect
    var projectListVisible: Boolean
    fun setInstruction(@StringRes text: Int)
    fun invokeDatePicker()
    fun invokeTimePicker()
    fun prepareProjectList(items: List<String>)
    fun refreshProjectList()

}
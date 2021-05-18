package com.cartlc.tracker.fresh.ui.daily.hours

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.service.endpoint.post.DCPostUseCase
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.bits.HideOnSoftKeyboard
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvc
import com.cartlc.tracker.fresh.ui.daily.hours.data.HoursDataProjectsImpl
import com.cartlc.tracker.fresh.ui.daily.hours.data.HoursUIData
import com.cartlc.tracker.fresh.ui.daily.hours.data.HoursUIData.Stage.*
import com.cartlc.tracker.fresh.ui.daily.hours.data.HoursUIDataImpl
import com.cartlc.tracker.fresh.ui.daily.project.ProjectSelect
import com.cartlc.tracker.fresh.ui.title.TitleViewMvc

class HoursController(
    private val boundAct: BoundAct,
    private val viewMvc: HoursViewMvc,
    private val titleViewMvc: TitleViewMvc,
    private val buttonsViewMvc: ButtonsViewMvc,
    dm: DatabaseTable
) : LifecycleObserver,
    HoursViewMvc.Listener,
    ButtonsViewMvc.Listener,
    HideOnSoftKeyboard.Listener {

    private val messageHandler = boundAct.componentRoot.messageHandler
    private var hoursUIData: HoursUIData = HoursUIDataImpl(dm, messageHandler)
    private val db: DatabaseTable = boundAct.repo.db
    private val screenNavigator = boundAct.screenNavigator
    private val postUseCase: DCPostUseCase
        get() = boundAct.componentRoot.postUseCase
    private var isSaved = false
    private val hoursDataProjects = HoursDataProjectsImpl(dm)

    init {
        boundAct.bindObserver(this)
    }

    // endregion SoftKeyboardDetect.Listener

    // region LifecycleObserver

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        viewMvc.registerListener(this)
        buttonsViewMvc.registerListener(this)
        buttonsViewMvc.btnCenterVisible = false
        titleViewMvc.mainTitleText = messageHandler.getString(StringMessage.daar_title)
        titleViewMvc.subTitleText = messageHandler.getString(StringMessage.daar_sub_title)
        viewMvc.invokeDatePicker()
        hoursUIData.first()
        initializeProjects()
        loadCurrentEdit()
        applyCurStage()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        if (!isSaved) {
            preserve()
        }
        viewMvc.unregisterListener(this)
        buttonsViewMvc.unregisterListener(this)
        hideOnSoftKeyboard?.unregisterListener(this)
    }

    // endregion LifecycleObserver

    // region DaarViewMvc.Listener

    override fun editTextAfterTextChanged(value: String) {
        hoursUIData.storeValueToStage(value)
        buttonsViewMvc.btnNextVisible = value.isNotBlank()
        hoursUIData.curStage?.let { data ->
            if (data is StageProject && hoursDataProjects.selectedProjectTab != ProjectSelect.PROJECT_OTHER) {
                buttonsViewMvc.btnNextVisible = hoursDataProjects.isReady
            }
        }
    }

    override fun onEditTextReturn() {
    }

    override fun onDateButtonClicked() {
        if (hoursUIData.curStage is StageBreak) {
            viewMvc.invokeTimePicker()
        } else {
            viewMvc.invokeDatePicker()
        }
    }

    override fun onDateEntered(value: Long) {
        viewMvc.dayTextValue = hoursUIData.storeValueToStage(value)
        applyCurStage()
    }

    override fun onTimeEntered(value: Long) {
        viewMvc.dayTextValue = hoursUIData.storeValueToStage(value)
        applyCurStage()
    }

    override fun onProjectTypeSelected(which: ProjectSelect) {
        var showEntry = false
        when (which) {
            ProjectSelect.PROJECT_RECENT -> {
                viewMvc.itemListVisible = true
                hoursDataProjects.selectingRootName = false
                prepareItemList(hoursDataProjects.mostRecentProjects)
            }
            ProjectSelect.PROJECT_ALL -> {
                viewMvc.itemListVisible = true
                hoursDataProjects.selectingRootName = true
                prepareItemList(hoursDataProjects.rootProjectNames)
            }
            ProjectSelect.PROJECT_OTHER -> {
                showEntry = true
                viewMvc.itemListVisible = false
                hoursDataProjects.selectingRootName = true
                hoursUIData.storeValueToStage(hoursUIData.data.projectDesc)
            }
        }
        hoursDataProjects.selectedProjectTab = which
        viewMvc.entryVisible = showEntry
    }

    override fun onItemSelected(position: Int, item: String) {
        when (val stage = hoursUIData.curStage) {
            is StageProject -> {
                with(hoursDataProjects) {
                    when {
                        selectedProjectTab == ProjectSelect.PROJECT_RECENT -> {
                            selectedRecentPosition = position
                            buttonsViewMvc.btnNextVisible = true
                            selectedProjectId?.let {
                                stage.storeValue(it)
                            }
                        }
                        selectingRootName -> {
                            selectedRootProjectName = item
                            selectedSubProjectName = null
                            selectingRootName = false
                            prepareItemList(hoursDataProjects.subProjectsOf(item))
                        }
                        else -> {
                            selectedSubProjectName = item
                            buttonsViewMvc.btnNextVisible = true
                            selectedProjectId?.let {
                                stage.storeValue(it)
                            }
                        }
                    }
                }
            }
            is StageBreak -> {
                stage.enteredValue = stage.list[position]
            }
        }
    }

    override fun isItemSelected(position: Int, item: String): Boolean {
        return when (val stage = hoursUIData.curStage) {
            is StageProject -> {
                with(hoursDataProjects) {
                    return when {
                        selectedProjectTab == ProjectSelect.PROJECT_RECENT -> {
                            selectedRecentProjectName == item
                        }
                        selectingRootName -> {
                            selectedRootProjectName == item
                        }
                        else -> {
                            selectedSubProjectName == item
                        }
                    }
                }
            }
            is StageBreak -> {
                stage.enteredValue == stage.list[position]
            }
            else -> false
        }
    }

    private fun prepareItemList(items: List<String>): Boolean {
        if (items.isEmpty()) {
            return false
        }
        viewMvc.prepareItemList(items)
        return true
    }

    // endregion DaarViewMvc.Listener

    // region ButtonsViewMvc.Listener

    override fun onBtnPrevClicked(view: View) {
        hoursUIData.prev()
        applyCurStage()
        clearSoftKeyboard(view)
    }

    override fun onBtnNextClicked(view: View) {
        if (hoursUIData.isLast) {
            save()
            screenNavigator.finish()
        } else {
            hoursUIData.next()
            applyCurStage()
            clearSoftKeyboard(view)
        }
    }

    override fun onBtnCenterClicked(view: View) {
    }

    override fun onBtnChangeClicked(view: View) {
    }

    // endregion ButtonsViewMvc.Listener

    // region SoftKeyboard

    var hideOnSoftKeyboard: HideOnSoftKeyboard? = null
        set(value) {
            value?.let {
                field?.unregisterListener(this)
                value.registerListener(this)
            } ?: run {
                field?.unregisterListener(this)
            }
            field = value
        }

    private fun clearSoftKeyboard(view: View) {
        hideOnSoftKeyboard?.hideKeyboard(view)
    }

    // region SoftKeyboardDetect.Listener

    override fun onSoftKeyboardVisible() {
        viewMvc.buttonsViewVisible = false
    }

    override fun onSoftKeyboardHidden() {
        viewMvc.buttonsViewVisible = true
    }

    // endregion SoftKeyboard

    private fun loadCurrentEdit() {
        db.tableHours.queryNotReady().firstOrNull()?.let { entry ->
            hoursUIData.data = entry
        }
    }

    private fun applyCurStage() {
        titleViewMvc.subTitleVisible = hoursUIData.isFirst
        val hasPrev = hoursUIData.hasPrev
        var hasNext = hoursUIData.hasNext

        hoursUIData.curStage?.let { data ->
            val resetProject = !hasPrev
            if (resetProject) {
                hoursDataProjects.selectingRootName = true
                hoursDataProjects.selectedRootProjectName = null
                hoursDataProjects.selectedSubProjectName = null
            }
            viewMvc.setInstruction(data.instruction)

            viewMvc.entryVisible = false
            viewMvc.dayVisible = false
            viewMvc.startTimeVisible = false
            viewMvc.endTimeVisible = false
            viewMvc.projectSelectVisible = false
            viewMvc.itemListVisible = false
            viewMvc.projectSelectVisible = false

            when (data) {
                is StageString -> {
                    viewMvc.entryVisible = true
                    viewMvc.entryEditTextValue = hoursUIData.getStringValueOf(data)
                }
                is StageDay -> {
                    viewMvc.dayVisible = true
                    viewMvc.dayTextValue = hoursUIData.getStringValueOf(data)
                }
                is StageProject -> {
                    viewMvc.projectSelectVisible = true
                    viewMvc.itemListVisible =
                        viewMvc.projectSelectWhich != ProjectSelect.PROJECT_OTHER
                    onProjectTypeSelected(viewMvc.projectSelectWhich)
                }
                is StageBreak -> {
                    viewMvc.itemListVisible = true
                    prepareItemList(data.stringList(messageHandler))
                }
                is StageHoursWorked -> {
                    viewMvc.startTimeVisible = true
                    viewMvc.startTimeValue = hoursUIData.getStringValueOf(data, 0)
                    viewMvc.endTimeVisible = true
                    viewMvc.startTimeValue = hoursUIData.getStringValueOf(data, 1)
                }
            }
            if (!hoursUIData.isComplete(data)) {
                hasNext = false
            } else if (hoursUIData.isLast && hoursUIData.isComplete) {
                hasNext = true
            }
            buttonsViewMvc.btnNextText =
                messageHandler.getString(if (hoursUIData.hasSave) StringMessage.btn_save else StringMessage.btn_next)
            buttonsViewMvc.btnNextVisible = hasNext
            buttonsViewMvc.btnPrevVisible = hasPrev
        }
        titleViewMvc.mainTitleText = messageHandler.getString(StringMessage.daar_title)
    }

    private fun save() {
        isSaved = true
        val data = hoursUIData.data
        data.isReady = hoursUIData.isComplete
        db.tableHours.save(data)
        postUseCase.ping()
    }

    private fun preserve() {
        db.tableHours.save(hoursUIData.data)
    }

    private fun initializeProjects() {
        val hasRecentProjects = hoursDataProjects.mostRecentProjects.isNotEmpty()
        viewMvc.projectsSelectRecentEnabled = hasRecentProjects
        if (hasRecentProjects) {
            viewMvc.projectSelectWhich = ProjectSelect.PROJECT_RECENT
        } else {
            viewMvc.projectSelectWhich = ProjectSelect.PROJECT_ALL
        }
        hoursUIData.first()
        hoursUIData.storeValueToStage(hoursDataProjects.mostRecentDate)
    }
}

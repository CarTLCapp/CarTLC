package com.cartlc.tracker.fresh.ui.daar

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.service.ServiceUseCase
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.bits.HideOnSoftKeyboard
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvc
import com.cartlc.tracker.fresh.ui.daar.data.DaarDataProjectsImpl
import com.cartlc.tracker.fresh.ui.daar.data.DaarUIData
import com.cartlc.tracker.fresh.ui.daar.data.DaarUIDataImpl
import com.cartlc.tracker.fresh.ui.title.TitleViewMvc

class DaarController(
        private val boundAct: BoundAct,
        private val daarViewMvc: DaarViewMvc,
        private val titleViewMvc: TitleViewMvc,
        private val buttonsViewMvc: ButtonsViewMvc,
        dm: DatabaseTable
) : LifecycleObserver,
        DaarViewMvc.Listener,
        ButtonsViewMvc.Listener,
        HideOnSoftKeyboard.Listener {

    private val messageHandler = boundAct.componentRoot.messageHandler
    private var daarUIData: DaarUIData = DaarUIDataImpl(dm, messageHandler)
    private val db: DatabaseTable = boundAct.repo.db
    private val screenNavigator = boundAct.screenNavigator
    private val serviceUseCase: ServiceUseCase
        get() = boundAct.componentRoot.serviceUseCase
    private var isSaved = false
    private val daarDataProjects = DaarDataProjectsImpl(dm)

    init {
        boundAct.bindObserver(this)
    }

    // endregion SoftKeyboardDetect.Listener

    // region LifecycleObserver

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        daarViewMvc.registerListener(this)
        buttonsViewMvc.registerListener(this)
        buttonsViewMvc.btnCenterVisible = false
        titleViewMvc.mainTitleText = messageHandler.getString(StringMessage.daar_title)
        titleViewMvc.subTitleText = messageHandler.getString(StringMessage.daar_sub_title)
        daarViewMvc.invokeDatePicker()
        daarUIData.first()
        initializeProjects()
        loadCurrentEdit()
        applyCurStage()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        if (!isSaved) {
            preserve()
        }
        daarViewMvc.unregisterListener(this)
        buttonsViewMvc.unregisterListener(this)
        hideOnSoftKeyboard?.unregisterListener(this)
    }

    // endregion LifecycleObserver

    // region DaarViewMvc.Listener

    override fun editTextAfterTextChanged(value: String) {
        daarUIData.storeValueToStage(value)
        buttonsViewMvc.btnNextVisible = value.isNotBlank()
        daarUIData.curStage?.let { data ->
            if (data is DaarUIData.Stage.StageProject && daarDataProjects.selectedProjectTab != DaarViewMvc.ProjectSelect.PROJECT_OTHER) {
                buttonsViewMvc.btnNextVisible = daarDataProjects.isReady
            }
        }
    }

    override fun onEditTextReturn() {
    }

    override fun onDateButtonClicked() {
        if (daarUIData.isStageInTime) {
            daarViewMvc.invokeTimePicker()
        } else {
            daarViewMvc.invokeDatePicker()
        }
    }

    override fun onDateEntered(value: Long) {
        daarViewMvc.timeDateTextValue = daarUIData.storeValueToStage(value)
        applyCurStage()
    }

    override fun onTimeEntered(value: Long) {
        daarViewMvc.timeDateTextValue = daarUIData.storeValueToStage(value)
        applyCurStage()
    }

    override fun onProjectTypeSelected(which: DaarViewMvc.ProjectSelect) {
        var showEntry = false
        when (which) {
            DaarViewMvc.ProjectSelect.PROJECT_RECENT -> {
                daarViewMvc.projectListVisible = true
                daarDataProjects.selectingRootName = false
                prepareProjectList(daarDataProjects.mostRecentProjects)
            }
            DaarViewMvc.ProjectSelect.PROJECT_ALL -> {
                daarViewMvc.projectListVisible = true
                daarDataProjects.selectingRootName = true
                prepareProjectList(daarDataProjects.rootProjectNames)
            }
            DaarViewMvc.ProjectSelect.PROJECT_OTHER -> {
                showEntry = true
                daarViewMvc.projectListVisible = false
                daarDataProjects.selectingRootName = true
                daarUIData.storeValueToStage(daarUIData.data.projectDesc)
            }
        }
        daarDataProjects.selectedProjectTab = which
        daarViewMvc.entryVisible = showEntry
    }

    private fun prepareProjectList(items: List<String>): Boolean {
        if (items.isEmpty()) {
            return false
        }
        daarViewMvc.prepareProjectList(items)
        return true
    }

    override fun onProjectItemSelected(position: Int, item: String) {
        if (daarDataProjects.selectingRootName) {
            daarDataProjects.selectedRootProjectName = item
            daarDataProjects.selectedSubProjectName = null
            daarDataProjects.selectingRootName = false
            prepareProjectList(daarDataProjects.subProjectsOf(item))
        } else {
            daarDataProjects.selectedSubProjectName = item
            buttonsViewMvc.btnNextVisible = true
            daarDataProjects.selectedProjectId?.let { daarUIData.projectStage.storeValue(it) }
        }
    }

    override fun isProjectSelected(position: Int, item: String): Boolean {
        return if (daarDataProjects.selectingRootName) {
            daarDataProjects.selectedRootProjectName == item
        } else {
            daarDataProjects.selectedSubProjectName == item
        }
    }

    // endregion DaarViewMvc.Listener

    // region ButtonsViewMvc.Listener

    override fun onBtnPrevClicked(view: View) {
        daarUIData.prev()
        applyCurStage()
        clearSoftKeyboard(view)
    }

    override fun onBtnNextClicked(view: View) {
        if (daarUIData.isLast) {
            save()
            screenNavigator.finish()
        } else {
            daarUIData.next()
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
        daarViewMvc.buttonsViewVisible = false
    }

    override fun onSoftKeyboardHidden() {
        daarViewMvc.buttonsViewVisible = true
    }

    // endregion SoftKeyboard

    private fun loadCurrentEdit() {
        db.tableDaar.queryNotReady().firstOrNull()?.let { entry ->
            daarUIData.data = entry
        }
    }

    private fun applyCurStage() {
        titleViewMvc.subTitleVisible = daarUIData.isFirst
        val hasPrev = daarUIData.hasPrev
        var hasNext = daarUIData.hasNext

        daarUIData.curStage?.let { data ->
            val resetProject = !hasPrev
            if (resetProject) {
                daarDataProjects.selectingRootName = true
                daarDataProjects.selectedRootProjectName = null
                daarDataProjects.selectedSubProjectName = null
            }
            daarViewMvc.setInstruction(data.instruction)
            when (data) {
                is DaarUIData.Stage.StageString -> {
                    daarViewMvc.entryVisible = true
                    daarViewMvc.timeDateVisible = false
                    daarViewMvc.projectSelectVisible = false
                    daarViewMvc.projectListVisible = false
                    daarViewMvc.entryEditTextValue = daarUIData.getStringValueOf(data)
                }
                is DaarUIData.Stage.StageDate -> {
                    daarViewMvc.timeDateVisible = true
                    daarViewMvc.entryVisible = false
                    daarViewMvc.projectSelectVisible = false
                    daarViewMvc.projectListVisible = false
                    daarViewMvc.timeDateTextValue = daarUIData.getStringValueOf(data)
                }
                is DaarUIData.Stage.StageProject -> {
                    daarViewMvc.timeDateVisible = false
                    daarViewMvc.projectSelectVisible = true
                    daarViewMvc.entryVisible = false
                    daarViewMvc.projectListVisible = daarViewMvc.projectSelectWhich != DaarViewMvc.ProjectSelect.PROJECT_OTHER
                    onProjectTypeSelected(daarViewMvc.projectSelectWhich)
                }
            }
            if (!daarUIData.isComplete(data)) {
                hasNext = false
            } else if (daarUIData.isLast && daarUIData.isComplete) {
                hasNext = true
            }
            buttonsViewMvc.btnNextText = messageHandler.getString(if (daarUIData.hasSave) StringMessage.btn_save else StringMessage.btn_next)
            buttonsViewMvc.btnNextVisible = hasNext
            buttonsViewMvc.btnPrevVisible = hasPrev
        }
        titleViewMvc.mainTitleText = messageHandler.getString(StringMessage.daar_title)
    }

    private fun save() {
        isSaved = true
        val data = daarUIData.data
        data.isReady = daarUIData.isComplete
        db.tableDaar.save(data)
        serviceUseCase.ping()
    }

    private fun preserve() {
        db.tableDaar.save(daarUIData.data)
    }

    private fun initializeProjects() {
        val hasRecentProjects = daarDataProjects.mostRecentProjects.isNotEmpty()
        daarViewMvc.projectsSelectRecentEnabled = hasRecentProjects
        if (hasRecentProjects) {
            daarViewMvc.projectSelectWhich = DaarViewMvc.ProjectSelect.PROJECT_RECENT
        } else {
            daarViewMvc.projectSelectWhich = DaarViewMvc.ProjectSelect.PROJECT_ALL
        }
        daarUIData.first()
        daarUIData.storeValueToStage(daarDataProjects.mostRecentDate)
    }
}
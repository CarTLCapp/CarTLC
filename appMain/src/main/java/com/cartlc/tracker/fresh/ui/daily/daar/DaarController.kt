package com.cartlc.tracker.fresh.ui.daily.daar

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
import com.cartlc.tracker.fresh.ui.daily.project.ProjectSelect
import com.cartlc.tracker.fresh.ui.daily.daar.data.DaarDataProjectsImpl
import com.cartlc.tracker.fresh.ui.daily.daar.data.DaarUIData
import com.cartlc.tracker.fresh.ui.daily.daar.data.DaarUIDataImpl
import com.cartlc.tracker.fresh.ui.title.TitleViewMvc

class DaarController(
    private val boundAct: BoundAct,
    private val viewMvc: DaarViewMvc,
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
    private val postUseCase: DCPostUseCase
        get() = boundAct.componentRoot.postUseCase
    private var isSaved = false
    private val daarDataProjects = DaarDataProjectsImpl(dm)

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
        viewMvc.unregisterListener(this)
        buttonsViewMvc.unregisterListener(this)
        hideOnSoftKeyboard?.unregisterListener(this)
    }

    // endregion LifecycleObserver

    // region DaarViewMvc.Listener

    override fun editTextAfterTextChanged(value: String) {
        daarUIData.storeValueToStage(value)
        buttonsViewMvc.btnNextVisible = value.isNotBlank()
        daarUIData.curStage?.let { data ->
            if (data is DaarUIData.Stage.StageProject && daarDataProjects.selectedProjectTab != ProjectSelect.PROJECT_OTHER) {
                buttonsViewMvc.btnNextVisible = daarDataProjects.isReady
            }
        }
    }

    override fun onEditTextReturn() {
    }

    override fun onDateButtonClicked() {
        if (daarUIData.isStageInTime) {
            viewMvc.invokeTimePicker()
        } else {
            viewMvc.invokeDatePicker()
        }
    }

    override fun onDateEntered(value: Long) {
        viewMvc.timeDateTextValue = daarUIData.storeValueToStage(value)
        applyCurStage()
    }

    override fun onTimeEntered(value: Long) {
        viewMvc.timeDateTextValue = daarUIData.storeValueToStage(value)
        applyCurStage()
    }

    override fun onProjectTypeSelected(which: ProjectSelect) {
        var showEntry = false
        when (which) {
            ProjectSelect.PROJECT_RECENT -> {
                viewMvc.projectListVisible = true
                daarDataProjects.selectingRootName = false
                prepareProjectList(daarDataProjects.mostRecentProjects)
            }
            ProjectSelect.PROJECT_ALL -> {
                viewMvc.projectListVisible = true
                daarDataProjects.selectingRootName = true
                prepareProjectList(daarDataProjects.rootProjectNames)
            }
            ProjectSelect.PROJECT_OTHER -> {
                showEntry = true
                viewMvc.projectListVisible = false
                daarDataProjects.selectingRootName = true
                daarUIData.storeValueToStage(daarUIData.data.projectDesc)
            }
        }
        daarDataProjects.selectedProjectTab = which
        viewMvc.entryVisible = showEntry
    }

    private fun prepareProjectList(items: List<String>): Boolean {
        if (items.isEmpty()) {
            return false
        }
        viewMvc.prepareProjectList(items)
        return true
    }

    override fun onProjectItemSelected(position: Int, item: String) {
        with(daarDataProjects) {
            when {
                selectedProjectTab == ProjectSelect.PROJECT_RECENT -> {
                    selectedRecentPosition = position
                    buttonsViewMvc.btnNextVisible = true
                    selectedProjectId?.let { daarUIData.projectStage.storeValue(it) }
                }
                selectingRootName -> {
                    selectedRootProjectName = item
                    selectedSubProjectName = null
                    selectingRootName = false
                    prepareProjectList(subProjectsOf(item))
                }
                else -> {
                    selectedSubProjectName = item
                    buttonsViewMvc.btnNextVisible = true
                    selectedProjectId?.let { daarUIData.projectStage.storeValue(it) }
                }
            }
        }
    }

    override fun isProjectSelected(position: Int, item: String): Boolean {
        with(daarDataProjects) {
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
        viewMvc.buttonsViewVisible = false
    }

    override fun onSoftKeyboardHidden() {
        viewMvc.buttonsViewVisible = true
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
            viewMvc.setInstruction(data.instruction)
            when (data) {
                is DaarUIData.Stage.StageString -> {
                    viewMvc.entryVisible = true
                    viewMvc.timeDateVisible = false
                    viewMvc.projectSelectVisible = false
                    viewMvc.projectListVisible = false
                    viewMvc.entryEditTextValue = daarUIData.getStringValueOf(data)
                }
                is DaarUIData.Stage.StageDate -> {
                    viewMvc.timeDateVisible = true
                    viewMvc.entryVisible = false
                    viewMvc.projectSelectVisible = false
                    viewMvc.projectListVisible = false
                    viewMvc.timeDateTextValue = daarUIData.getStringValueOf(data)
                }
                is DaarUIData.Stage.StageProject -> {
                    viewMvc.timeDateVisible = false
                    viewMvc.projectSelectVisible = true
                    viewMvc.entryVisible = false
                    viewMvc.projectListVisible =
                        viewMvc.projectSelectWhich != ProjectSelect.PROJECT_OTHER
                    onProjectTypeSelected(viewMvc.projectSelectWhich)
                }
            }
            if (!daarUIData.isComplete(data)) {
                hasNext = false
            } else if (daarUIData.isLast && daarUIData.isComplete) {
                hasNext = true
            }
            buttonsViewMvc.btnNextText =
                messageHandler.getString(if (daarUIData.hasSave) StringMessage.btn_save else StringMessage.btn_next)
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
        postUseCase.ping()
    }

    private fun preserve() {
        db.tableDaar.save(daarUIData.data)
    }

    private fun initializeProjects() {
        val hasRecentProjects = daarDataProjects.mostRecentProjects.isNotEmpty()
        viewMvc.projectsSelectRecentEnabled = hasRecentProjects
        if (hasRecentProjects) {
            viewMvc.projectSelectWhich = ProjectSelect.PROJECT_RECENT
        } else {
            viewMvc.projectSelectWhich = ProjectSelect.PROJECT_ALL
        }
        daarUIData.first()
        daarUIData.storeValueToStage(daarDataProjects.mostRecentDate)
    }
}

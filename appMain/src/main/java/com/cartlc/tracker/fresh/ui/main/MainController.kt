/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main

import android.app.Activity
import android.content.Intent
import android.location.Address
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.BuildConfig
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.flow.*
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.buttons.ButtonsUseCase
import com.cartlc.tracker.fresh.ui.main.process.*
import com.cartlc.tracker.fresh.ui.mainlist.MainListUseCase
import com.cartlc.tracker.fresh.ui.picture.PictureListUseCase
import com.cartlc.tracker.fresh.model.event.Action
import com.cartlc.tracker.fresh.model.event.Button
import com.cartlc.tracker.fresh.model.event.EventError
import com.cartlc.tracker.fresh.model.event.EventRefreshProjects
import com.cartlc.tracker.model.flow.*
import com.cartlc.tracker.fresh.model.misc.EntryHint
import com.cartlc.tracker.fresh.model.misc.TruckStatus
import com.cartlc.tracker.fresh.model.msg.ErrorMessage
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.ui.bits.SoftKeyboardDetect
import com.cartlc.tracker.ui.util.CheckError
import com.cartlc.tracker.ui.util.helper.DialogHelper
import com.cartlc.tracker.ui.util.helper.LocationHelper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.File

class MainController(
        private val boundAct: BoundAct,
        private val viewMvc: MainViewMvc
) : LifecycleObserver,
        MainViewMvc.Listener,
        FlowUseCase.Listener,
        ActionUseCase.Listener,
        ButtonsUseCase.Listener,
        PictureListUseCase.Listener,
        MainListUseCase.Listener {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_EDIT_ENTRY = 2

        const val RESULT_EDIT_ENTRY = 2
        const val RESULT_EDIT_PROJECT = 3
        const val RESULT_DELETE_PROJECT = 4
    }

    private val repo = boundAct.repo
    private val prefHelper: PrefHelper = repo.prefHelper
    private val db: DatabaseTable = repo.db

    private val contextWrapper = boundAct.componentRoot.contextWrapper
    private val messageHandler = boundAct.componentRoot.messageHandler
    private val serviceUseCase = boundAct.componentRoot.serviceUseCase
    private val locationUseCase = boundAct.locationUseCase
    private val screenNavigator = boundAct.screenNavigator
    private val dialogHelper = boundAct.dialogHelper
    private val dialogNavigator = boundAct.dialogNavigator

    private val buttonsUseCase = viewMvc.buttonsUseCase
    private val titleUseCase = viewMvc.titleUseCase
    private val pictureUseCase = viewMvc.pictureUseCase
    private val mainListUseCase = viewMvc.mainListUseCase
    private val entrySimpleUseCase = viewMvc.entrySimpleUseCase

    private val eventController = boundAct.componentRoot.eventController
    private val actionUseCase = repo.actionUseCase

    init {
        boundAct.bindObserver(this)

        Flow.processActionEvent = { action -> processActionEvent(action) }
        Flow.processStageEvent = { flow -> curFlowValue = flow }

        error.observe(boundAct.act, Observer<ErrorMessage> { message -> showError(message) })
    }

    var softKeyboardDetect: SoftKeyboardDetect? = null
        set(value) {
            field = value
            viewMvc.buttonsUseCase.softKeyboardDetect = value
        }

    // region support variables

    private var curFlowValue: Flow
        get() = repo.curFlowValue
        set(value) {
            repo.curFlowValue = value
        }

    private val isFinishing: Boolean
        get() = boundAct.isFinishing

    private var fabAddress: Address? = null

    private val editProjectHint: String
        get() {
            val sbuf = StringBuilder()
            sbuf.append(messageHandler.getString(StringMessage.entry_hint_edit_project))
            val name = prefHelper.projectDashName
            if (name.isNotEmpty()) {
                sbuf.append("\n")
                sbuf.append(name)
            }
            val address = prefHelper.address
            if (address.isNotEmpty()) {
                sbuf.append("\n")
                sbuf.append(address)
            }
            return sbuf.toString()
        }

    private val curProjectHint: String
        get() {
            val sbuf = StringBuilder()
            val name = prefHelper.projectDashName
            sbuf.append(name)
            sbuf.append("\n")
            sbuf.append(prefHelper.address)
            return sbuf.toString()
        }

    private val hasCurrentProject: Boolean
        get() = prefHelper.currentProjectGroup != null

    private val statusHint: String
        get() {
            val sbuf = StringBuilder()
            val countPictures = prefHelper.numPicturesTaken
            val maxEquip = prefHelper.numEquipPossible
            val checkedEquipment = db.tableEquipment.queryChecked().size
            sbuf.append(messageHandler.getString(StringMessage.status_installed_equipments(checkedEquipment, maxEquip)))
            sbuf.append("\n")
            sbuf.append(messageHandler.getString(StringMessage.status_installed_pictures(countPictures)))
            return sbuf.toString()
        }

    private var autoNarrowOkay = true

    private val isAutoNarrowOkay: Boolean
        get() = buttonsUseCase.wasNext && autoNarrowOkay

    private var addButtonVisible: Boolean
        get() = viewMvc.addButtonVisible
        set(value) {
            viewMvc.addButtonVisible = value
        }

    // endregion support variables

    // region Shared

    inner class Shared {

        private val main = this@MainController

        val repo = main.repo
        val db: DatabaseTable = repo.db
        val prefHelper: PrefHelper = repo.prefHelper

        val locationUseCase = main.locationUseCase
        val serviceUseCase = main.serviceUseCase
        val messageHandler = main.messageHandler
        val screenNavigator = main.screenNavigator

        val buttonsUseCase = viewMvc.buttonsUseCase
        val titleUseCase = viewMvc.titleUseCase
        val pictureUseCase = viewMvc.pictureUseCase
        val mainListUseCase = viewMvc.mainListUseCase
        val entrySimpleUseCase = viewMvc.entrySimpleUseCase

        val editProjectHint: String
            get() = main.editProjectHint

        val curProjectHint: String
            get() = main.curProjectHint

        var curFlowValue: Flow
            get() = main.curFlowValue
            set(value) {
                main.curFlowValue = value
            }

        var picturesVisible: Boolean
            get() = viewMvc.picturesVisible
            set(value) {
                viewMvc.picturesVisible = value
            }

        val isAutoNarrowOkay: Boolean
            get() = main.isAutoNarrowOkay

        var fabAddress: Address?
            get() = main.fabAddress
            set(value) {
                main.fabAddress = value
            }

        val hasCompanyName: Boolean
            get() = !prefHelper.company.isNullOrBlank()

        val hasProjectRootName: Boolean
            get() = prefHelper.projectRootName != null

        val hasProjectSubName: Boolean
            get() = prefHelper.projectSubName != null

        val hasCurrentProject: Boolean
            get() = main.hasCurrentProject

        var errorValue: ErrorMessage
            get() = repo.errorValue
            set(value) {
                repo.errorValue = value
            }

        val isFinishing: Boolean
            get() = main.isFinishing

        fun checkCenterButtonIsEdit() = main.checkCenterButtonIsEdit()

        fun getLocation() = locationUseCase.getLocation { address -> fabAddress = address }

        fun onEditEntry() = main.onEditEntry()
    }

    private val shared = Shared()
    private val stageStreet = StageStreet(shared)
    private val stageCity = StageCity(shared)
    private val stageState = StageState(shared)
    private val stageCompany = StageCompany(shared)
    private val stageCurrentProject = StageCurrentProject(shared)
    private val stageSelectProject = StageSelectProject(shared)
    private val stageTruck = StageTruck(shared)
    private val stageEquipment = StageEquipment(shared)
    private val taskPicture = TaskPicture(shared)
    private val stagePicture = StagePicture(shared, taskPicture)
    private val stageConfirm = StageConfirm(shared)

    // endregion Shared

    // region Lifecycle

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        repo.flowUseCase.registerListener(this)
        pictureUseCase.registerListener(this)
        mainListUseCase.registerListener(this)
        entrySimpleUseCase.emsValue = contextWrapper.getInteger(R.integer.entry_simple_ems)
        entrySimpleUseCase.dispatchActionEvent = { event -> dispatchActionEvent(event) }
        entrySimpleUseCase.afterTextChangedListener = { value -> onEntryValueChanged(value) }
        eventController.register(this)
        shared.getLocation()
        actionUseCase.registerListener(this)
        prefHelper.onCurrentProjecGroupChanged = { checkAddButtonVisible() }
        prefHelper.setFromCurrentProjectId()
        buttonsUseCase.registerListener(this)
        viewMvc.registerListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        repo.flowUseCase.notifyListeners()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        repo.flowUseCase.unregisterListener(this)
        pictureUseCase.unregisterListener(this)
        mainListUseCase.unregisterListener(this)
        prefHelper.onCurrentProjecGroupChanged = {}
        eventController.unregister(this)
        CheckError.instance.cleanup()
        LocationHelper.instance.onDestroy()
        actionUseCase.unregisterListener(this)
        buttonsUseCase.unregisterListener(this)
        viewMvc.unregisterListener(this)
    }

    // endregion Lifecycle

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
        mainListUseCase.visible = false
        viewMvc.picturesVisible = false
        viewMvc.fragmentVisible =
                when (flow.stage) {
                    Stage.LOGIN -> {
                        MainViewMvc.FragmentType.LOGIN
                    }
                    Stage.CONFIRM -> {
                        MainViewMvc.FragmentType.CONFIRM
                    }
                    else -> {
                        MainViewMvc.FragmentType.NONE
                    }
                }

        buttonsUseCase.reset(flow)

        when (flow.stage) {
            Stage.ROOT_PROJECT,
            Stage.COMPANY,
            Stage.ADD_COMPANY,
            Stage.STATE,
            Stage.ADD_STATE,
            Stage.CITY,
            Stage.ADD_CITY,
            Stage.STREET,
            Stage.ADD_STREET,
            Stage.CONFIRM_ADDRESS -> {
            }
            else -> {
            }
        }
    }

    override fun onStageChanged(flow: Flow) {
        checkAddButtonVisible()
        when (flow.stage) {
            Stage.ROOT_PROJECT, Stage.SUB_PROJECT -> {
                stageSelectProject.process(flow)
            }
            Stage.COMPANY, Stage.ADD_COMPANY -> {
                stageCompany.process(flow)
            }
            Stage.STATE, Stage.ADD_STATE -> {
                stageState.process(flow)
            }
            Stage.CITY, Stage.ADD_CITY -> {
                stageCity.process(flow)
            }
            Stage.STREET, Stage.ADD_STREET -> {
                stageStreet.process(flow)
            }
            Stage.CONFIRM_ADDRESS -> {
                titleUseCase.subTitleText = curProjectHint
                titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_confirmation)
                titleUseCase.mainTitleVisible = true
                titleUseCase.subTitleVisible = true
                buttonsUseCase.centerVisible = false
                buttonsUseCase.nextVisible = true
            }
            Stage.CURRENT_PROJECT -> {
                stageCurrentProject.process()
            }
            Stage.TRUCK -> {
                stageTruck.process()
            }
            Stage.EQUIPMENT, Stage.ADD_EQUIPMENT -> {
                stageEquipment.process(flow)
            }
            Stage.NOTES -> {
                titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_notes)
                mainListUseCase.visible = true
            }
            Stage.PICTURE_1,
            Stage.PICTURE_2,
            Stage.PICTURE_3 -> {
                stagePicture.process(flow)
            }
            Stage.STATUS -> {
                buttonsUseCase.nextText = messageHandler.getString(StringMessage.btn_done)
                mainListUseCase.visible = true
                titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_status)
                titleUseCase.subTitleText = statusHint
            }
            Stage.CONFIRM -> {
                stageConfirm.process()
            }
            else -> {
            }
        }
    }

    // endregion FlowUseCase.Listener

    // region ButtonsUseCase.Listener

    override fun onButtonConfirm(action: Button): Boolean {
        when (curFlowValue.stage) {
            Stage.CONFIRM_ADDRESS -> {
                prefHelper.saveProjectAndAddressCombo(repo.editProject)
                repo.editProject = false
            }
            else -> {
            }
        }
        return when (action) {
            Button.BTN_NEXT -> save(true)
            Button.BTN_PREV -> save(false)
            else -> true
        }
    }

    override fun onButtonEvent(action: Button) {
        when (curFlowValue.stage) {
            Stage.LOGIN -> {
            }
            else -> {
                when (action) {
                    Button.BTN_NEXT -> repo.companyEditing = null
                    Button.BTN_CHANGE -> btnChangeCompany()
                    else -> {
                    }
                }
                curFlowValue.process(action)
            }
        }
    }

    private fun btnChangeCompany() {
        autoNarrowOkay = false
        buttonsUseCase.wasNext = false
        repo.onCompanyChanged()
    }

    private fun save(isNext: Boolean): Boolean {
        val entryText = entrySimpleUseCase.entryTextValue ?: ""
        when (curFlowValue.stage) {
            Stage.TRUCK ->
                if (entryText.isEmpty()) {
                    if (isNext) {
                        errorValue = ErrorMessage.NEED_A_TRUCK
                        return false
                    }
                    // For debugging purposes only.
                    prefHelper.truckNumber = null
                    prefHelper.licensePlate = null
                    prefHelper.doErrorCheck = true
                } else {
                    prefHelper.parseTruckValue(entryText)
                }
            Stage.ADD_CITY -> prefHelper.city = entryText
            Stage.ADD_STREET -> prefHelper.street = entryText
            Stage.ADD_EQUIPMENT -> {
                if (entryText.isNotEmpty()) {
                    val group = prefHelper.currentProjectGroup
                    if (group != null) {
                        db.tableCollectionEquipmentProject.addLocal(entryText, group.projectNameId)
                    }
                }
            }
            Stage.EQUIPMENT ->
                if (isNext) {
                    if (db.tableEquipment.countChecked() == 0) {
                        errorValue = ErrorMessage.NEED_EQUIPMENT
                        return false
                    }
                }
            Stage.ADD_COMPANY -> {
                val newCompanyName = entryText.trim { it <= ' ' }
                if (isNext) {
                    if (newCompanyName.isEmpty()) {
                        errorValue = ErrorMessage.NEED_NEW_COMPANY
                        return false
                    }
                    prefHelper.company = newCompanyName
                    repo.companyEditing?.let {
                        val companies = db.tableAddress.queryByCompanyName(it)
                        for (address in companies) {
                            address.company = newCompanyName
                            db.tableAddress.update(address)
                        }
                    }
                }
            }
            Stage.COMPANY ->
                if (isNext) {
                    if (prefHelper.company.isNullOrBlank()) {
                        errorValue = ErrorMessage.NEED_COMPANY
                        return false
                    }
                }
            Stage.NOTES ->
                if (isNext) {
                    if (!mainListUseCase.areNotesComplete) {
                        dialogNavigator.showNoteError(mainListUseCase.notes) {
                            showNoteErrorOk()
                        }
                        return false
                    }
                }
            Stage.STATUS ->
                if (isNext) {
                    if (prefHelper.status === TruckStatus.UNKNOWN) {
                        errorValue = ErrorMessage.NEED_STATUS
                        return false
                    }
                }
            Stage.CONFIRM ->
                if (isNext) {
                    showConfirmDialog()
                    return false
                }
            else -> {
            }
        }
        return true
    }

    private fun showNoteErrorOk() {
        if (BuildConfig.DEBUG) {
            buttonsUseCase.skip()
        }
    }

    // endregion ButtonsUseCase.Listener

    // region MainListViewMvc.Listener

    override fun onAddClicked() {
        if (prefHelper.currentEditEntryId != 0L) {
            prefHelper.clearLastEntry()
        }
        curFlowValue = SubProjectFlow()
    }

    // endregion MainListViewMvc.Listener

    // region support functions

    private fun checkCenterButtonIsEdit() {
        buttonsUseCase.centerText = if (isCenterButtonEdit) {
            messageHandler.getString(StringMessage.btn_edit)
        } else {
            messageHandler.getString(StringMessage.btn_add)
        }
    }

    private val isCenterButtonEdit: Boolean
        get() = curFlowValue.stage == Stage.COMPANY && prefHelper.isLocalCompany

    private fun onEntryValueChanged(value: String) {
        when (curFlowValue.stage) {
            Stage.COMPANY,
            Stage.STREET,
            Stage.CITY,
            Stage.ADD_COMPANY,
            Stage.ADD_STREET,
            Stage.ADD_CITY,
            Stage.ADD_EQUIPMENT,
            Stage.ADD_STATE -> buttonsUseCase.nextVisible = value.isNotBlank()
            else -> {
            }
        }
    }

    private fun checkAddButtonVisible() {
        addButtonVisible = prefHelper.currentProjectGroup != null &&
                db.tableProjectAddressCombo.count() > 0 &&
                curFlowValue.stage == Stage.CURRENT_PROJECT
    }

    // endregion support functions

    // region error

    val error: MutableLiveData<ErrorMessage>
        get() = repo.error

    private var errorValue: ErrorMessage
        get() = repo.errorValue
        set(value) {
            repo.errorValue = value
        }

    private fun showError(error: ErrorMessage) {
        return showError(messageHandler.getErrorMessage(error))
    }

    private fun showError(error: String) {
        dialogHelper.showError(error, object : DialogHelper.DialogListener {
            override fun onOkay() {
                onErrorDialogOkay()
            }

            override fun onCancel() {}
        })
    }

    private fun onErrorDialogOkay() {
        when (curFlowValue.stage) {
            Stage.PICTURE_1,
            Stage.PICTURE_2,
            Stage.PICTURE_3 -> buttonsUseCase.dispatch(Button.BTN_NEXT)
            else -> {
            }
        }
    }

    // endregion error

    // region Action Events

    fun dispatchActionEvent(action: Action) = repo.dispatchActionEvent(action)

    private fun processActionEvent(action: Action) {
        when (action) {
            Action.NEW_PROJECT -> onNewProject()
            Action.ADD_PICTURE -> taskPicture.dispatchPictureRequest()
            is Action.RETURN_PRESSED -> doSimpleEntryReturn(action.text)
            else -> dispatchActionEvent(action)
        }
    }

    private fun onNewProject() {
        autoNarrowOkay = true
        prefHelper.clearCurProject()
        curFlowValue = RootProjectFlow()
    }

    private fun doSimpleEntryReturn(value: String) {
        when (curFlowValue.stage) {
            Stage.LOGIN -> {
                buttonsUseCase.dispatch(Button.BTN_CENTER)
                return
            }
            Stage.COMPANY, Stage.ADD_COMPANY -> prefHelper.company = value
            Stage.CITY, Stage.ADD_CITY -> prefHelper.city = value
            Stage.STREET, Stage.ADD_STREET -> prefHelper.street = value
            else -> {
            }
        }
        buttonsUseCase.dispatch(Button.BTN_NEXT)
    }

    // endregion Action Events

    // region onActivityResult

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_EDIT_ENTRY ->
                when (resultCode) {
                    RESULT_EDIT_ENTRY -> onEditEntry()
                    RESULT_EDIT_PROJECT -> onEditProject()
                    RESULT_DELETE_PROJECT -> onDeletedProject()
                    else -> onAbort()
                }
            REQUEST_IMAGE_CAPTURE ->
                if (resultCode == Activity.RESULT_OK) {
                    taskPicture.rotatePicture()
                    taskPicture.onPictureRequestComplete()
                }
            else -> onAbort()
        }
    }

    private fun onAbort() {
        curFlowValue = CurrentProjectFlow()
    }

    private fun onEditEntry() {
        curFlowValue = TruckFlow()
    }

    private fun onEditProject() {
        repo.editProject = true
        curFlowValue = RootProjectFlow()
    }

    private fun onDeletedProject() {
        prefHelper.clearCurProject()
        curFlowValue = CurrentProjectFlow()
    }

    fun onRestoreInstanceState(path: String?) {
        if (path != null) {
            taskPicture.takingPictureFile = File(path)
        }
    }

    fun onSaveInstanceState(): String? {
        return taskPicture.takingPictureFile?.absolutePath
    }

    // endregion onActivityResult

    // region MenuItem

    fun onOptionsItemSelected(itemId: Int): Boolean {
        when (itemId) {
            R.id.profile -> btnProfile()
            R.id.upload -> {
                serviceUseCase.reloadFromServer()
            }
            R.id.fleet_vehicles -> {
                onVehiclesPressed()
            }
            R.id.privacy -> {
                screenNavigator.showPrivacyPolicy()
            }
            else -> {
                return false
            }
        }
        return true
    }

    private fun btnProfile() {
        save(false)
        curFlowValue = LoginFlow()
    }

    private fun onVehiclesPressed() {
        if (repo.hasInsectingList) {
            screenNavigator.showVehiclesActivity()
        } else {
            screenNavigator.showVehiclesPendingDialog()
        }
    }

    // endregion MenuItem

    // region MainListUseCase.Listener

    override fun onEntryHintChanged(entryHint: EntryHint) {
        viewMvc.entryHint = MainViewMvc.EntryHint(
                entryHint.message,
                if (entryHint.isError) R.color.entry_error_color else android.R.color.white
        )
    }

    override fun onKeyValueChanged(key: String, keyValue: String?) {
        when (curFlowValue.stage) {
            Stage.ROOT_PROJECT,
            Stage.SUB_PROJECT,
            Stage.CITY,
            Stage.STATE,
            Stage.STREET,
            Stage.ADD_CITY,
            Stage.ADD_STATE,
            Stage.ADD_STREET -> {
                buttonsUseCase.nextVisible = true
            }
            Stage.COMPANY -> {
                buttonsUseCase.nextVisible = true
            }
            Stage.TRUCK -> {
                entrySimpleUseCase.entryTextValue = keyValue
            }
            Stage.CURRENT_PROJECT -> {
                buttonsUseCase.prevVisible = hasCurrentProject
            }
            else -> {
            }
        }
    }

    override fun onProjectGroupSelected(projectGroup: DataProjectAddressCombo) {
        buttonsUseCase.prevVisible = hasCurrentProject
    }

    // endregion MainListUseCase.Listener

    // region PictureListUseCase.Listener

    override fun onPictureRemoveDone(remaining: Int) {
        titleUseCase.setPhotoTitleCount(remaining)
    }

    override fun onPictureNoteAdded(picture: DataPicture) {
        db.tablePictureCollection.update(picture, prefHelper.currentPictureCollectionId)
    }

    // endregion PictureListUseCase.Listener

    // region ActionUseCase.Listener

    override fun onActionChanged(action: Action) {
        when (action) {
            Action.VIEW_PROJECT -> screenNavigator.showViewProjectActivity(REQUEST_EDIT_ENTRY)
        }
    }

    private fun showConfirmDialog() {
        dialogHelper.showConfirmDialog(object : DialogHelper.DialogListener {
            override fun onOkay() {
                onConfirmOkay()
            }

            override fun onCancel() {}
        })
    }

    private fun onConfirmOkay() {
        viewMvc.confirmUseCase?.onConfirmOkay()
        serviceUseCase.ping()
    }

    // endregion ActionUseCase.Listener

    // region EventController

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EventRefreshProjects) {
        Timber.d("onEvent(EventRefreshProjects)")
        repo.flowUseCase.notifyListeners()
    }

    private var showServerError = true

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EventError) {
        if (showServerError) {
            showServerError(event.toString())
        }
    }

    private fun showServerError(message: String?) {
        dialogHelper.showServerError(message
                ?: "server error", object : DialogHelper.DialogListener {
            override fun onOkay() {}

            override fun onCancel() {
                showServerError = false
            }
        })
    }

    // endregion EventController

}
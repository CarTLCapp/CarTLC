/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main

import android.app.Activity
import android.content.ComponentCallbacks2
import android.content.Intent
import android.location.Address
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataNote
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
import com.cartlc.tracker.fresh.model.misc.EntryHint
import com.cartlc.tracker.fresh.model.misc.TruckStatus
import com.cartlc.tracker.fresh.model.msg.ErrorMessage
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.fresh.ui.bits.SoftKeyboardDetect
import com.cartlc.tracker.fresh.ui.common.PermissionHelper
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

        const val RESULT_EDIT_PROJECT_ENTRY = 2
        const val RESULT_EDIT_PROJECT = 3
        const val RESULT_DELETE_PROJECT = 4
    }

    private val repo = boundAct.repo
    private val prefHelper: PrefHelper = repo.prefHelper
    private val db: DatabaseTable = repo.db
    private val componentRoot = boundAct.componentRoot
    private val contextWrapper = componentRoot.contextWrapper
    private val messageHandler = componentRoot.messageHandler
    private val serviceUseCase = componentRoot.serviceUseCase
    private val locationUseCase = boundAct.locationUseCase
    private val screenNavigator = boundAct.screenNavigator
    private val deviceHelper = componentRoot.deviceHelper
    private val dialogHelper = boundAct.dialogHelper
    private val dialogNavigator = boundAct.dialogNavigator
    private val permissionHelper = componentRoot.permissionHelper
    private val bitmapHelper = componentRoot.bitmapHelper
    private val instaBugUseCase = componentRoot.instaBugUseCase

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

    val versionedTitle: String
        get() {
            val sbuf = StringBuilder()
            sbuf.append(messageHandler.getString(StringMessage.app_name))
            sbuf.append(" - ")
            try {
                sbuf.append(deviceHelper.version)
            } catch (ex: Exception) {
                TBApplication.ReportError(ex, MainController::class.java, "versionedTitle", "main")
            }
            return sbuf.toString()
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
        val dialogNavigator = main.dialogNavigator

        val buttonsUseCase = viewMvc.buttonsUseCase
        val titleUseCase = viewMvc.titleUseCase
        val pictureUseCase = viewMvc.pictureUseCase
        val mainListUseCase = viewMvc.mainListUseCase
        val entrySimpleUseCase = viewMvc.entrySimpleUseCase

        val editProjectHint: String
            get() = main.editProjectHint

        val curProjectHint: String
            get() = main.curProjectHint

        var progress: String?
            get() = viewMvc.customProgress
            set(value) {
                viewMvc.customProgress = value
            }

        var picturesVisible: Boolean
            get() = viewMvc.picturesVisible
            set(value) {
                viewMvc.picturesVisible = value
            }

        var curFlowValue: Flow
            get() = main.curFlowValue
            set(value) {
                main.curFlowValue = value
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

        fun onEditEntry() = main.onEditProjectEntry()
    }

    private val shared = Shared()
    private val taskPicture = TaskPicture(shared)
    private val stageStreet = StageStreet(shared)
    private val stageCity = StageCity(shared)
    private val stageState = StageState(shared)
    private val stageCompany = StageCompany(shared)
    private val stageCurrentProject = StageCurrentProject(shared)
    private val stageSelectProject = StageSelectProject(shared)
    private val stageTruckNumber = StageTruckNumber(shared, taskPicture)
    private val stageTruckDamage = StageTruckDamage(shared, taskPicture)
    private val stageEquipment = StageEquipment(shared)
    private val stageCustom = StageCustom(shared, taskPicture)
    private val stageConfirm = StageFinalConfirm(shared)
    private val stageStatus = StageStatus(shared)

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
        actionUseCase.registerListener(this)
        prefHelper.onCurrentProjecGroupChanged = { checkAddButtonVisible() }
        prefHelper.setFromCurrentProjectId()
        buttonsUseCase.registerListener(this)
        viewMvc.registerListener(this)
        permissionHelper.checkPermissions(boundAct.act, TBApplication.PERMISSIONS,
                object : PermissionHelper.PermissionListener {
                    override fun onGranted(permission: String) {
                        shared.getLocation()
                    }

                    override fun onDenied(permission: String) {}
                })
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
        viewMvc.fragmentVisible = MainViewMvc.FragmentType.NONE
        pictureUseCase.clearCache()
    }

    fun onTrimMemory(level: Int) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            Timber.e("onTrimMemory(): Ran critically low on memory: so no cache and push back to current project flow")
            curFlowValue = CurrentProjectFlow()
            bitmapHelper.cacheOkay = false
        } else if (bitmapHelper.cacheOkay) {
            Timber.e("onTrimMemory($level): disabled cache")
            bitmapHelper.cacheOkay = false
        }
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
        viewMvc.customProgress = null
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
            Stage.TRUCK_NUMBER_PICTURE -> {
                stageTruckNumber.process()
            }
            Stage.TRUCK_DAMAGE_PICTURE -> {
                stageTruckDamage.process()
            }
            Stage.EQUIPMENT, Stage.ADD_EQUIPMENT -> {
                stageEquipment.process(flow)
            }
            is Stage.CUSTOM_FLOW -> {
                stageCustom.process()
            }
            Stage.STATUS -> {
                stageStatus.process()
            }
            Stage.CONFIRM -> {
                stageConfirm.process()
            }
            else -> {
            }
        }
        repo.prefHelper.activityDetect()
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
            is Stage.CUSTOM_FLOW -> {
                when (action) {
                    Button.BTN_CENTER -> {
                        stageCustom.center()
                    }
                    Button.BTN_PREV -> {
                        if (!stageCustom.prev()) {
                            curFlowValue.process(action)
                        }
                    }
                    Button.BTN_NEXT -> {
                        if (!stageCustom.next()) {
                            curFlowValue.process(action)
                        }
                    }
                    else -> {
                    }
                }
            }
            Stage.TRUCK_NUMBER_PICTURE -> {
                if (action == Button.BTN_CENTER) {
                    stageTruckNumber.center()
                } else {
                    curFlowValue.process(action)
                }
            }
            Stage.TRUCK_DAMAGE_PICTURE -> {
                if (action == Button.BTN_CENTER) {
                    stageTruckDamage.center()
                } else {
                    curFlowValue.process(action)
                }
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
        return when (curFlowValue.stage) {
            Stage.ADD_CITY -> stageCity.saveAdd()
            Stage.ADD_STREET -> stageStreet.saveAdd()
            Stage.ADD_EQUIPMENT -> stageEquipment.saveAdd()
            Stage.EQUIPMENT -> stageEquipment.save(isNext)
            Stage.ADD_COMPANY -> stageCompany.saveAdd(isNext)
            Stage.COMPANY -> stageCompany.save(isNext)
            Stage.TRUCK_NUMBER_PICTURE -> stageTruckNumber.save()
            Stage.TRUCK_DAMAGE_PICTURE -> stageTruckDamage.save()
            is Stage.CUSTOM_FLOW -> stageCustom.save(isNext)
            Stage.STATUS ->
                return if (isNext) {
                    if (prefHelper.status === TruckStatus.UNKNOWN) {
                        errorValue = ErrorMessage.NEED_STATUS
                        false
                    } else true
                } else true
            Stage.CONFIRM -> stageConfirm.save(viewMvc, isNext)
            else -> true
        }
    }

    // endregion ButtonsUseCase.Listener

    // region MainListViewMvc.Listener

    override fun onAddClicked() {
        if (prefHelper.currentEditEntryId != 0L) {
            prefHelper.clearLastEntry()
        }
        if (hasSubProjects()) {
            curFlowValue = SubProjectFlow()
        } else {
            screenNavigator.showToast(messageHandler.getString(StringMessage.error_has_no_flows))
        }
    }

    private fun hasSubProjects(): Boolean {
        return prefHelper.projectRootName?.let { rootName ->
            db.tableFlow.filterHasFlow(db.tableProjects.querySubProjects(rootName)).isNotEmpty()
        } ?: false
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
                db.tableProjectAddressCombo.count() > 0 && hasSubProjects() &&
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
                // TODO: Do I still need to do something like this?
//                onErrorDialogOkay()
            }

            override fun onCancel() {}
        })
    }

//    private fun onErrorDialogOkay() {
//        when (curFlowValue.stage) {
//            Stage.PICTURE_1,
//            Stage.PICTURE_2,
//            Stage.PICTURE_3 -> buttonsUseCase.dispatch(Button.BTN_NEXT)
//            else -> {
//            }
//        }
//    }

    // endregion error

    // region Action Events

    private fun dispatchActionEvent(action: Action) = repo.dispatchActionEvent(action)

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
                buttonsUseCase.center()
                return
            }
            Stage.COMPANY, Stage.ADD_COMPANY -> prefHelper.company = value
            Stage.CITY, Stage.ADD_CITY -> prefHelper.city = value
            Stage.STREET, Stage.ADD_STREET -> prefHelper.street = value
            else -> {
            }
        }
        buttonsUseCase.next()
    }

    // endregion Action Events

    // region onActivityResult

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_EDIT_ENTRY ->
                when (resultCode) {
                    RESULT_EDIT_PROJECT_ENTRY -> onEditProjectEntry()
                    RESULT_EDIT_PROJECT -> onEditProject()
                    RESULT_DELETE_PROJECT -> onDeletedProject()
                    else -> onAbort()
                }
            REQUEST_IMAGE_CAPTURE ->
                if (resultCode == Activity.RESULT_OK) {
                    taskPicture.rotatePicture()
                    taskPicture.onPictureRequestComplete()
                } else {
                    taskPicture.onPictureRequestAbort()
                }
            else -> onAbort()
        }
    }

    private fun onAbort() {
        curFlowValue = CurrentProjectFlow()
    }

    // The user wants to change some data from a previously entered & uploaded entry
    private fun onEditProjectEntry() {
        curFlowValue = TruckNumberPictureFlow()
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

    // region handlePermissionResult

    fun handlePermissionResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionHelper.onHandlePermissionResult(requestCode, permissions, grantResults)
    }

    // endregion handlePermissionResult

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
            R.id.feedback -> {
                instaBugUseCase.show()
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

    override fun onConfirmItemChecked(isAllChecked: Boolean) {
        buttonsUseCase.nextVisible = isAllChecked
    }

    // endregion MainListUseCase.Listener

    // region PictureListUseCase.Listener

    override fun onPictureRemoveDone(numPictures: Int) {
        if (curFlowValue.stage == Stage.TRUCK_DAMAGE_PICTURE) {
            stageTruckDamage.clearDamage()
        } else {
            pictureStateChanged()
        }
    }

    override fun onPictureNoteChanged(note: DataNote) {
        pictureStateChanged()
    }

    private fun pictureStateChanged() {
        when (curFlowValue.stage) {
            Stage.TRUCK_NUMBER_PICTURE -> stageTruckNumber.pictureStateChanged()
            Stage.TRUCK_DAMAGE_PICTURE -> stageTruckDamage.pictureStateChanged()
            is Stage.CUSTOM_FLOW -> stageCustom.pictureStateChanged()
        }
    }

    // endregion PictureListUseCase.Listener

    // region ActionUseCase.Listener

    override fun onActionChanged(action: Action) {
        when (action) {
            Action.VIEW_PROJECT -> screenNavigator.showViewProjectActivity(REQUEST_EDIT_ENTRY)
        }
    }

    // endregion ActionUseCase.Listener

    // region EventController

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EventRefreshProjects) {
        Timber.d("onEvent(EventRefreshProjects)")
        repo.flowUseCase.notifyListeners()
        checkAddButtonVisible()
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

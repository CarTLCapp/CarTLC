/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.viewmodel.main

import android.location.Address
import android.text.InputType
import androidx.lifecycle.*
import com.cartlc.tracker.BuildConfig
import com.cartlc.tracker.model.event.*
import com.cartlc.tracker.model.flow.*
import com.cartlc.tracker.model.misc.*
import com.cartlc.tracker.model.msg.ErrorMessage
import com.cartlc.tracker.model.msg.StringMessage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.buttons.ButtonsUseCase
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleUseCase
import com.cartlc.tracker.fresh.ui.title.TitleUseCase
import com.cartlc.tracker.ui.stage.newproject.NewProjectVMHolder
import com.cartlc.tracker.ui.util.helper.BitmapHelper
import com.cartlc.tracker.ui.util.CheckError
import com.cartlc.tracker.viewmodel.frag.*
import java.io.File

class MainVMHolder(
        boundAct: BoundAct,
        private val newProjectHolder: NewProjectVMHolder,
        val buttonsUseCase: ButtonsUseCase,
        private val mainListViewModel: MainListViewModel,
        private val confirmationViewModel: ConfirmationViewModel,
        private val titleUseCase: TitleUseCase,
        private val entrySimpleControl: EntrySimpleUseCase
) : LifecycleObserver, FlowUseCase.Listener, ButtonsUseCase.Listener {

    companion object {
        private val ALLOW_EMPTY_TRUCK = BuildConfig.DEBUG // true=Debugging only
    }

    private val repo = boundAct.repo
    private val messageHandler = boundAct.componentRoot.messageHandler

    private var curFlowValue: Flow
        get() = repo.curFlowValue
        set(value) {
            repo.curFlowValue = value
        }

    val isPictureStage: Boolean
        get() = curFlowValue.isPictureStage

    val error: MutableLiveData<ErrorMessage>
        get() = repo.error

    var fabAddress: Address?
        get() = newProjectHolder.fabAddress
        set(value) {
            newProjectHolder.fabAddress = value
        }

    val addButtonVisible: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    val framePictureVisible: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }


    var notes: () -> List<DataNote> = { emptyList() }

    // Private

    private val db: DatabaseTable
        get() = repo.db

    private val prefHelper: PrefHelper
        get() = repo.prefHelper

    private var errorValue: ErrorMessage
        get() = repo.errorValue
        set(value) {
            repo.errorValue = value
        }

    private var addButtonVisibleValue: Boolean
        get() = addButtonVisible.value ?: false
        set(value) {
            addButtonVisible.value = value
        }

    private var framePictureVisibleValue: Boolean
        get() = framePictureVisible.value ?: false
        set(value) {
            framePictureVisible.value = value
        }

    private var editProject: Boolean = false
    private var takingPictureFile: File? = null

    private val hasProjectSubName: Boolean
        get() = prefHelper.projectSubName != null

    private val curProjectHint: String
        get() {
            val sbuf = StringBuilder()
            val name = prefHelper.projectDashName
            sbuf.append(name)
            sbuf.append("\n")
            sbuf.append(prefHelper.address)
            return sbuf.toString()
        }

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

    init {
        boundAct.bindObserver(this)

        Flow.processActionEvent = { action -> processActionEvent(action) }
        Flow.processStageEvent = { flow -> curFlowValue = flow }

        repo.flowUseCase.registerListener(this)
    }

    // region lifecycle & Setup

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        entrySimpleControl.dispatchActionEvent = { event -> dispatchActionEvent(event) }
        entrySimpleControl.afterTextChangedListener = { value -> onEntryValueChanged(value) }
        confirmationViewModel.dispatchActionEvent = entrySimpleControl.dispatchActionEvent
        confirmationViewModel.buttonsUseCase = buttonsUseCase
        confirmationViewModel.titleUseCase = titleUseCase
        mainListViewModel.onCurrentProjectGroupChanged = { checkAddButtonVisible() }
        prefHelper.setFromCurrentProjectId()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        repo.flowUseCase.unregisterListener(this)
    }

    fun onRestoreInstanceState(path: String?) {
        if (path != null) {
            takingPictureFile = File(path)
        }
    }

    fun onSaveInstanceState(): String? {
        return takingPictureFile?.absolutePath
    }

    // endregion lifecycle & setup

    // region ACTION OBJECT

    fun dispatchActionEvent(action: Action) = repo.dispatchActionEvent(action)

    private fun btnChangeCompany() {
        newProjectHolder.autoNarrowOkay = false
        buttonsUseCase.wasNext = false
        repo.onCompanyChanged()
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

    // endregion ACTION OBJECT

    // region OUTSIDE EVENT

    fun onDeletedProject() {
        prefHelper.clearCurProject()
        curFlowValue = CurrentProjectFlow()
    }

    fun onAbort() {
        curFlowValue = CurrentProjectFlow()
    }

    fun onEditEntry() {
        curFlowValue = TruckFlow()
    }

    fun onEditProject() {
        editProject = true
        curFlowValue = RootProjectFlow()
    }

    fun onVehiclesPressed() {
        if (repo.hasInsectingList) {
            dispatchActionEvent(Action.VEHICLES)
        } else {
            dispatchActionEvent(Action.VEHICLES_PENDING)
        }
    }

    fun onErrorDialogOkay() {
        when (curFlowValue.stage) {
            Stage.PICTURE_1,
            Stage.PICTURE_2,
            Stage.PICTURE_3 -> buttonsUseCase.dispatch(Button.BTN_NEXT)
            else -> {
            }
        }
    }

    // endregion OUTSIDE EVENT

    // region BUTTONS

    override fun onButtonConfirm(action: Button): Boolean {
        return when (action) {
            Button.BTN_NEXT -> save(true)
            Button.BTN_PREV -> save(false)
            else -> true
        }
    }

    override fun onButtonEvent(action: Button) {
        when (action) {
            Button.BTN_CHANGE -> btnChangeCompany()
            else -> { curFlowValue.process(action) }
        }
    }

    private fun processActionEvent(action: Action) {
        when (action) {
            Action.NEW_PROJECT -> onNewProject()
            Action.PING -> dispatchActionEvent(Action.PING)
            Action.ADD_PICTURE -> dispatchPictureRequest()
            is Action.RETURN_PRESSED -> doSimpleEntryReturn(action.text)
            else -> dispatchActionEvent(action)
        }
    }

    private fun onNewProject() {
        newProjectHolder.autoNarrowOkay = true
        prefHelper.clearCurProject()
        curFlowValue = RootProjectFlow()
    }

    fun btnPlus() {
        if (prefHelper.currentEditEntryId != 0L) {
            prefHelper.clearLastEntry()
        }
        curFlowValue = SubProjectFlow()
    }

    fun btnProfile() {
        save(false)
        curFlowValue = LoginFlow()
    }

    // endregion BUTTONS

    // region PICTURE

    fun incAutoRotatePicture(commonRotation: Int) {
        prefHelper.incAutoRotatePicture(commonRotation)
    }

    fun clearAutoRotatePicture() {
        prefHelper.clearAutoRotatePicture()
    }

    fun autoRotatePictureResult() {
        if (takingPictureFile != null && takingPictureFile!!.exists()) {
            val degrees = prefHelper.autoRotatePicture
            if (degrees != 0) {
                BitmapHelper.rotate(takingPictureFile!!, degrees)
            }
        }
    }

    fun onPictureRequestComplete() {
        repo.flowUseCase.notifyListeners()
    }

    private fun dispatchPictureRequest() {
        val pictureFile = prefHelper.genFullPictureFile()
        db.tablePictureCollection.add(pictureFile, prefHelper.currentPictureCollectionId)
        takingPictureFile = pictureFile
        dispatchActionEvent(Action.PICTURE_REQUEST(pictureFile))
    }

    fun dispatchPictureRequestFailure() {
        takingPictureFile = null
        errorValue = ErrorMessage.CANNOT_TAKE_PICTURE
    }

    // endregion PICTURE

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
        addButtonVisibleValue = false
        framePictureVisibleValue = false
        when (flow.stage) {
            Stage.CURRENT_PROJECT,
            Stage.SUB_PROJECT,
            Stage.TRUCK,
            Stage.EQUIPMENT,
            Stage.ADD_EQUIPMENT,
            Stage.NOTES,
            Stage.PICTURE_1,
            Stage.PICTURE_2,
            Stage.PICTURE_3,
            Stage.STATUS-> {
                buttonsUseCase.reset(flow)
                buttonsUseCase.listener = this
            }
        }
    }

    override fun onStageChanged(flow: Flow) {
        when (flow.stage) {
            Stage.CURRENT_PROJECT -> {
                if (!repo.flowUseCase.wasFromNotify) {
                    dispatchActionEvent(Action.PING)
                }
                prefHelper.saveProjectAndAddressCombo(editProject)
                editProject = false

                checkErrors()
                checkAddButtonVisible()

                mainListViewModel.showingValue = true
                titleUseCase.separatorVisible = true
                titleUseCase.mainTitleVisible = true
                buttonsUseCase.centerVisible = true
                buttonsUseCase.prevText = messageHandler.getString(StringMessage.btn_edit)
                buttonsUseCase.centerText = messageHandler.getString(StringMessage.btn_new_project)
                titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_current_project)
            }
            Stage.SUB_PROJECT -> {
                titleUseCase.mainTitleVisible = true
                prefHelper.projectRootName?.let { rootName ->
                    mainListViewModel.showingValue = true
                    titleUseCase.subTitleText = curProjectHint
                    buttonsUseCase.nextVisible = hasProjectSubName
                    setList(StringMessage.title_sub_project, PrefHelper.KEY_SUB_PROJECT, db.tableProjects.querySubProjectNames(rootName))
                } ?: run {
                    curFlowValue = RootProjectFlow()
                }
            }
            Stage.TRUCK -> {

                prefHelper.saveProjectAndAddressCombo(modifyCurrent = false, needsValidServerId = true)

                entrySimpleControl.showing = true
                entrySimpleControl.hintValue = messageHandler.getString(StringMessage.title_truck)
                entrySimpleControl.helpValue = messageHandler.getString(StringMessage.entry_hint_truck)
                entrySimpleControl.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                mainListViewModel.showingValue = true

                val hint: String?
                if (prefHelper.currentProjectGroup != null) {
                    hint = prefHelper.currentProjectGroup?.hintLine
                } else {
                    hint = null
                }
                val trucks = db.tableTruck.queryStrings(prefHelper.currentProjectGroup)
                entrySimpleControl.entryTextValue = prefHelper.truckValue
                setList(StringMessage.title_truck, PrefHelper.KEY_TRUCK, trucks)
                titleUseCase.subTitleText = hint
                titleUseCase.subTitleVisible = true
            }
            Stage.EQUIPMENT -> {
                titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_equipment_installed)
                mainListViewModel.showingValue = true
                buttonsUseCase.centerVisible = true
            }
            Stage.ADD_EQUIPMENT -> {
                titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_equipment)
                entrySimpleControl.showing = true
                entrySimpleControl.hintValue = messageHandler.getString(StringMessage.title_equipment)
                entrySimpleControl.simpleTextClear()
            }
            Stage.NOTES -> {
                titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_notes)
                mainListViewModel.showingValue = true
            }
            Stage.PICTURE_1,
            Stage.PICTURE_2,
            Stage.PICTURE_3 -> {
                val pictureCount = prefHelper.numPicturesTaken
                var showToast = false
                if (buttonsUseCase.wasNext) {
                    showToast = true
                    buttonsUseCase.wasNext = false
                }
                val pictures = db.tablePictureCollection.removeNonExistant(
                        db.tablePictureCollection.queryPictures(prefHelper.currentPictureCollectionId
                        )).toMutableList()
                if (showToast) {
                    dispatchActionEvent(Action.SHOW_PICTURE_TOAST(pictureCount))
                }
                titleUseCase.setPhotoTitleCount(pictureCount)
                buttonsUseCase.nextVisible = false
                buttonsUseCase.centerVisible = true
                buttonsUseCase.centerText = messageHandler.getString(StringMessage.btn_another)
                framePictureVisibleValue = true
                val pictureFlow = flow as PictureFlow
                if (pictureCount < pictureFlow.expected) {
                    dispatchPictureRequest()
                } else {
                    buttonsUseCase.nextVisible = true
                    setList(pictures)
                }
            }
            Stage.STATUS -> {
                buttonsUseCase.nextText = messageHandler.getString(StringMessage.btn_done)
                mainListViewModel.showingValue = true
                titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_status)
                titleUseCase.subTitleText = statusHint
            }
            else -> {
            }
        }
    }

    private fun checkErrors() {
        if (!prefHelper.doErrorCheck) {
            return
        }
        val entry = checkEntryErrors()
        if (entry != null) {
            dispatchActionEvent(Action.SHOW_TRUCK_ERROR(entry, object : CheckError.CheckErrorResult {
                override fun doEdit() {
                    onEditEntry()
                }

                override fun doDelete(entry: DataEntry) {
                    db.tableEntry.remove(entry)
                }

                override fun setFromEntry(entry: DataEntry) {
                    prefHelper.setFromEntry(entry)
                }
            }))
        } else {
            prefHelper.doErrorCheck = false
        }
    }

    private fun checkEntryErrors(): DataEntry? = repo.checkEntryErrors()

    private fun checkAddButtonVisible() {
        addButtonVisibleValue = prefHelper.currentProjectGroup != null && db.tableProjectAddressCombo.count() > 0
    }

    // endregion FlowUseCase.Listener

    // region SET LIST

    private fun setList(msg: StringMessage, key: String, list: List<String>) {
        titleUseCase.mainTitleText = messageHandler.getString(msg)
        mainListViewModel.curKey = key
        if (list.isEmpty()) {
            mainListViewModel.showingValue = false
            onEmptyList()
        } else {
            mainListViewModel.showingValue = true
            dispatchActionEvent(Action.SET_MAIN_LIST(list))
        }
    }

    private fun onEmptyList() {
        when (curFlowValue.stage) {
            Stage.COMPANY,
            Stage.STREET,
            Stage.CITY,
            Stage.EQUIPMENT,
            Stage.STATE -> buttonsUseCase.dispatch(Button.BTN_CENTER)
            else -> {
            }
        }
    }

    private fun setList(list: List<DataPicture>) {
        dispatchActionEvent(Action.SET_PICTURE_LIST(list))
    }

    // endregion SET LIST

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

    // region save

    fun save(isNext: Boolean): Boolean {
        val entryText = entrySimpleControl.entryTextValue ?: ""
        when (curFlowValue.stage) {
            Stage.TRUCK ->
                if (entryText.isEmpty()) {
                    if (isNext) {
                        errorValue = ErrorMessage.NEED_A_TRUCK
                    }
                    if (!ALLOW_EMPTY_TRUCK) {
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
                    if (!repo.isNotesComplete(notes())) {
                        dispatchActionEvent(Action.SHOW_NOTE_ERROR)
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
                    dispatchActionEvent(Action.CONFIRM_DIALOG)
                    return false
                }
            else -> {
            }
        }
        return true
    }

    // endregion save

    fun showNoteErrorOk() {
        if (BuildConfig.DEBUG) {
            buttonsUseCase.skip()
        }
    }
}
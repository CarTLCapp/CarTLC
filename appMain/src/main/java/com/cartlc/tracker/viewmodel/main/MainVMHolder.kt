/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.viewmodel.main

import android.location.Address
import android.text.InputType
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.R
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.data.*
import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.model.event.Button
import com.cartlc.tracker.model.flow.*
import com.cartlc.tracker.model.misc.*
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.util.BitmapHelper
import com.cartlc.tracker.ui.util.CheckError
import com.cartlc.tracker.viewmodel.frag.*
import java.io.File

class MainVMHolder(val repo: CarRepository) {

    private val db: DatabaseTable
        get() = repo.db

    private val prefHelper: PrefHelper
        get() = repo.prefHelper

    val curFlow: MutableLiveData<Flow>
        get() = repo.curFlow

    private var curFlowValue: Flow
        get() = repo.curFlowValue
        set(value) {
            repo.curFlowValue = value
        }

    val isPictureStage: Boolean
        get() = curFlowValue.isPictureStage

    val error: MutableLiveData<ErrorMessage>
        get() = repo.error

    var fab_address: Address?
        get() = newProjectHolder.fab_address
        set(value) {
            newProjectHolder.fab_address = value
        }

    private var errorValue: ErrorMessage
        get() = repo.errorValue
        set(value) {
            repo.errorValue = value
        }

    val addButtonVisible: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    private var addButtonVisibleValue: Boolean
        get() = addButtonVisible.value ?: false
        set(value) {
            addButtonVisible.value = value
        }

    val framePictureVisible: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private var framePictureVisibleValue: Boolean
        get() = framePictureVisible.value ?: false
        set(value) {
            framePictureVisible.value = value
        }

    var getString: (msg: StringMessage) -> String = { "" }

    private var editProject: Boolean = false
    private var takingPictureFile: File? = null

    private val statusHint: String
        get() {
            val sbuf = StringBuilder()
            val countPictures = prefHelper.numPicturesTaken
            val maxEquip = prefHelper.numEquipPossible
            val checkedEquipment = db.tableEquipment.queryChecked().size
            sbuf.append(getString(StringMessage.status_installed_equipments(checkedEquipment, maxEquip)))
            sbuf.append("\n")
            sbuf.append(getString(StringMessage.status_installed_pictures(countPictures)))
            return sbuf.toString()
        }

    lateinit var buttonsViewModel: MainButtonsViewModel
    lateinit var loginViewModel: LoginViewModel
    lateinit var mainListViewModel: MainListViewModel
    lateinit var confirmationViewModel: ConfirmationViewModel
    lateinit var titleViewModel: TitleViewModel
    lateinit var entrySimpleViewModel: EntrySimpleViewModel

    private val newProjectHolder = NewProjectVMHolder(this)

    init {
        Flow.processActionEvent = { action -> processActionEvent(action) }
        Flow.processStageEvent = { flow -> curFlowValue = flow }
    }

    fun onCreate() {
        loginViewModel.error = error
        loginViewModel.buttonsViewModel = buttonsViewModel
        loginViewModel.getString = getString
        loginViewModel.dispatchActionEvent = { event -> dispatchActionEvent(event) }
        entrySimpleViewModel.dispatchActionEvent = { event -> dispatchActionEvent(event) }
        confirmationViewModel.dispatchActionEvent = entrySimpleViewModel.dispatchActionEvent
        confirmationViewModel.buttonsViewModel = buttonsViewModel
        confirmationViewModel.titleViewModel = titleViewModel
        confirmationViewModel.getString = getString
        titleViewModel.getString = getString
        buttonsViewModel.getString = getString
        mainListViewModel.onCurrentProjectGroupChanged = { checkAddButtonVisible() }
        prefHelper.setFromCurrentProjectId()
    }

    fun onRestoreInstanceState(path: String?) {
        if (path != null) {
            takingPictureFile = File(path)
        }
    }

    fun onSaveInstanceState(): String? {
        return takingPictureFile?.absolutePath
    }

    // ACTION OBJECT

    fun dispatchActionEvent(action: Action) = repo.dispatchActionEvent(action)

    fun handleActionEvent() = repo.handleActionEvent()

    fun btnChangeCompany() {
        newProjectHolder.autoNarrowOkay = false
        buttonsViewModel.wasNext = false
        repo.onCompanyChanged()
    }

    private fun doSimpleEntryReturn(value: String) {
        when (curFlowValue.stage) {
            Stage.LOGIN -> {
                buttonsViewModel.onButtonDispatch(Button.BTN_CENTER)
                return
            }
            Stage.COMPANY, Stage.ADD_COMPANY -> prefHelper.company = value
            Stage.CITY, Stage.ADD_CITY -> prefHelper.city = value
            Stage.STREET, Stage.ADD_STREET -> prefHelper.street = value
            else -> {
            }
        }
        buttonsViewModel.onButtonDispatch(Button.BTN_NEXT)
    }

    // ACTION OBJECT END

    // OUTSIDE EVENT

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
        curFlowValue = ProjectFlow()
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
            Stage.PICTURE_3 -> buttonsViewModel.onButtonDispatch(Button.BTN_NEXT)
            else -> {
            }
        }
    }

    // OUTSIDE EVENT END

    // BUTTONS

    fun onButtonDispatch(button: Button) {
        when(button) {
            Button.BTN_CHANGE -> btnChangeCompany()
            else -> buttonsViewModel.onButtonDispatch(button)
        }
    }

    private fun processActionEvent(action: Action) {
        when (action) {
            Action.NEW_PROJECT -> onNewProject()
            Action.PING -> dispatchActionEvent(Action.PING)
            Action.ADD_PICTURE -> dispatchPictureRequest()
            Action.PREVIOUS_FLOW -> repo.onPreviousFlow()
            is Action.RETURN_PRESSED -> doSimpleEntryReturn(action.text)
            else -> dispatchActionEvent(action)
        }
    }

    private fun onNewProject() {
        newProjectHolder.autoNarrowOkay = true
        prefHelper.clearCurProject()
        curFlowValue = ProjectFlow()
    }

    fun btnPlus() {
        if (prefHelper.currentEditEntryId != 0L) {
            prefHelper.clearLastEntry()
        }
        curFlowValue = TruckFlow()
    }

    // BUTTONS END


    // PICTURE

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
        // TODO: Find better way to trigger onStageChanged()
        curFlowValue = curFlowValue
    }

    fun dispatchPictureRequest() {
        val pictureFile = prefHelper.genFullPictureFile()
        db.tablePictureCollection.add(pictureFile, prefHelper.currentPictureCollectionId)
        takingPictureFile = pictureFile
        dispatchActionEvent(Action.PICTURE_REQUEST(pictureFile))
    }

    fun dispatchPictureRequestFailure() {
        takingPictureFile = null
        errorValue = ErrorMessage.CANNOT_TAKE_PICTURE
    }

    // END PICTURE

    // ON STAGE CHANGED

    fun refresh(flow: Flow) {
        onStageChanged(flow, false)
    }

    fun onStageChanged(flow: Flow) {
        onStageChanged(flow, true)
    }

    fun onStageChanged(flow: Flow, pingOkay: Boolean) {

        addButtonVisibleValue = false
        framePictureVisibleValue = false
        buttonsViewModel.reset(flow)
        mainListViewModel.entryHintValue = EntryHint("", false)
        mainListViewModel.showingValue = false
        mainListViewModel.showEmptyValue = false
        confirmationViewModel.showingValue = false
        titleViewModel.showSeparatorValue = false
        titleViewModel.subTitleValue = null
        entrySimpleViewModel.reset()

        loginViewModel.onStageChanged(flow)
        confirmationViewModel.onStageChanged(flow)
        newProjectHolder.onStageChanged(flow)

        when (flow.stage) {
            Stage.CURRENT_PROJECT -> {
                if (pingOkay) {
                    dispatchActionEvent(Action.PING)
                }
                prefHelper.saveProjectAndAddressCombo(editProject)
                editProject = false
                checkErrors()
                checkAddButtonVisible()

                mainListViewModel.showingValue = true
                titleViewModel.showSeparatorValue = true
                buttonsViewModel.showCenterButtonValue = true
                buttonsViewModel.prevTextValue = getString(StringMessage.btn_edit)
                buttonsViewModel.centerTextValue = getString(StringMessage.btn_new_project)
                titleViewModel.titleValue = getString(StringMessage.title_current_project)
            }
            Stage.TRUCK -> {
                entrySimpleViewModel.showingValue = true
                entrySimpleViewModel.simpleHintValue = getString(StringMessage.title_truck)
                entrySimpleViewModel.helpTextValue = getString(StringMessage.entry_hint_truck)
                entrySimpleViewModel.inputTypeValue = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                mainListViewModel.showingValue = true

                var hint: String?
                if (prefHelper.currentProjectGroup != null) {
                    hint = prefHelper.currentProjectGroup?.hintLine
                } else {
                    hint = null
                }
                val trucks = db.tableTruck.queryStrings(prefHelper.currentProjectGroup)
                entrySimpleViewModel.simpleTextValue = prefHelper.truckValue
                setList(StringMessage.title_truck, PrefHelper.KEY_TRUCK, trucks)
                titleViewModel.subTitleValue = hint
            }
            Stage.EQUIPMENT -> {
                titleViewModel.titleValue = getString(StringMessage.title_equipment_installed)
                mainListViewModel.showingValue = true
                buttonsViewModel.showCenterButtonValue = true
            }
            Stage.ADD_EQUIPMENT -> {
                titleViewModel.titleValue = getString(StringMessage.title_equipment)
                entrySimpleViewModel.showingValue = true
                entrySimpleViewModel.simpleHintValue = getString(StringMessage.title_equipment)
                entrySimpleViewModel.simpleTextValue = ""
            }
            Stage.NOTES -> {
                titleViewModel.titleValue = getString(StringMessage.title_notes)
                mainListViewModel.showingValue = true
            }
            Stage.PICTURE_1,
            Stage.PICTURE_2,
            Stage.PICTURE_3 -> {
                val pictureCount = prefHelper.numPicturesTaken
                var showToast = false
                if (buttonsViewModel.wasNext) {
                    showToast = true
                    buttonsViewModel.wasNext = false
                }
                val pictures = db.tablePictureCollection.removeNonExistant(
                        db.tablePictureCollection.queryPictures(prefHelper.currentPictureCollectionId
                        )).toMutableList()
                if (showToast) {
                    dispatchActionEvent(Action.SHOW_PICTURE_TOAST(pictureCount))
                }
                titleViewModel.setPhotoTitleCount(pictureCount)
                buttonsViewModel.showNextButtonValue = false
                buttonsViewModel.showCenterButtonValue = true
                buttonsViewModel.centerTextValue = getString(StringMessage.btn_another)
                framePictureVisibleValue = true
                val pictureFlow = flow as PictureFlow
                if (pictureCount < pictureFlow.expected) {
                    dispatchPictureRequest()
                } else {
                    buttonsViewModel.showNextButtonValue = true
                    setList(pictures)
                }
            }
            Stage.STATUS -> {
                buttonsViewModel.nextTextValue = getString(StringMessage.btn_done)
                mainListViewModel.showingValue = true
                titleViewModel.titleValue = getString(StringMessage.title_status)
                titleViewModel.subTitleValue = statusHint
            }
            else -> {}
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

    fun checkAddButtonVisible() {
        addButtonVisibleValue = prefHelper.currentProjectGroup != null && db.tableProjectAddressCombo.count() > 0
    }

    // ON STAGE CHANGED END

    // SET LIST

    internal fun setList(msg: StringMessage, key: String, list: List<String>) {
        titleViewModel.titleValue = getString(msg)
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
            Stage.STATE -> buttonsViewModel.onButtonDispatch(Button.BTN_CENTER)
            else -> {
            }
        }
    }

    private fun setList(list: List<DataPicture>) {
        dispatchActionEvent(Action.SET_PICTURE_LIST(list))
    }

    // SET LIST END
}
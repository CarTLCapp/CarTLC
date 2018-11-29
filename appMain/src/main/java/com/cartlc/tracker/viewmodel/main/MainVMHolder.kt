package com.cartlc.tracker.viewmodel.main

import android.location.Address
import android.text.InputType
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.BuildConfig
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.data.*
import com.cartlc.tracker.model.flow.*
import com.cartlc.tracker.model.misc.*
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.util.BitmapHelper
import com.cartlc.tracker.ui.util.CheckError
import com.cartlc.tracker.ui.util.LocationHelper
import com.cartlc.tracker.viewmodel.frag.*
import java.io.File
import java.util.*

class MainVMHolder(val repo: CarRepository) {

    companion object {
        private val ALLOW_EMPTY_TRUCK = BuildConfig.DEBUG // true=Debugging only
    }

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

    internal var companyEditing: String? = null
    internal var wasNext: Boolean = false
    internal var didAutoSkip: Boolean = false

    var detectNoteError: () -> Boolean = { false }
    var detectLoginError: () -> Boolean = { false }
    var entryTextValue: () -> String = { "" }
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

    lateinit var buttonsViewModel: ButtonsViewModel
    lateinit var loginViewModel: LoginViewModel
    lateinit var mainListViewModel: MainListViewModel
    lateinit var confirmationViewModel: ConfirmationViewModel
    lateinit var titleViewModel: TitleViewModel
    lateinit var entrySimpleViewModel: EntrySimpleViewModel

    private val newProjectHolder = NewProjectVMHolder(this)

    // INITIALIZATION

    fun onCreate() {
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

    // INITIALIZATION END

    // ACTION OBJECT

    fun dispatchActionEvent(action: Action) = repo.dispatchActionEvent(action)

    fun handleActionEvent() = repo.handleActionEvent()

    private fun processActionEvent(action: Action) {
        when (action) {
            Action.NEW_PROJECT -> {
                onNewProject()
            }
            Action.BTN_PREV -> btnPrev()
            Action.BTN_CENTER -> btnCenter()
            Action.BTN_NEXT -> btnNext()
            Action.BTN_CHANGE -> btnChangeCompany()
            Action.PING -> dispatchActionEvent(Action.PING)
            is Action.RETURN_PRESSED -> doSimpleEntryReturn(action.text)
            else -> dispatchActionEvent(action)
        }
    }

    private fun btnChangeCompany() {
        newProjectHolder.autoNarrowOkay = false
        wasNext = false
        repo.onCompanyChanged()
    }

    private fun doSimpleEntryReturn(value: String) {
        when (curFlowValue.stage) {
            Stage.LOGIN -> {
                btnCenter()
                return
            }
            Stage.COMPANY, Stage.ADD_COMPANY -> prefHelper.company = value
            Stage.CITY, Stage.ADD_CITY -> prefHelper.city = value
            Stage.STREET, Stage.ADD_STREET -> prefHelper.street = value
            else -> {
            }
        }
        btnNext()
    }

    private fun onNewProject() {
        newProjectHolder.autoNarrowOkay = true
        prefHelper.clearCurProject()
        curFlowValue = ProjectFlow()
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
            Stage.PICTURE_3,
            Stage.ADD_PICTURE -> {
                btnNext()
            }
            else -> {
            }
        }
    }

    // OUTSIDE EVENT END

    // BUTTONS

    private fun btnPrev() {
        if (confirmPrev()) {
            wasNext = false
            companyEditing = null
            process(curFlowValue.prev)
        }
    }

    private fun confirmPrev(): Boolean {
        return save(false)
    }

    private fun btnNext(wasAutoSkip: Boolean = false) {
        if (confirmNext()) {
            didAutoSkip = wasAutoSkip
            companyEditing = null
            advance()
        }
    }

    private fun advance() {
        wasNext = true
        process(curFlowValue.next)
    }

    private fun confirmNext(): Boolean {
        return save(true)
    }

    internal fun skip() {
        if (wasNext) {
            btnNext(true)
        } else {
            btnPrev()
        }
    }

    fun btnPlus() {
        if (prefHelper.currentEditEntryId != 0L) {
            prefHelper.clearLastEntry()
        }
        curFlowValue = TruckFlow()
    }

    fun showNoteErrorOk() {
        if (BuildConfig.DEBUG) {
            advance()
        } else {
            btnNext()
        }
    }

    private fun process(action: ActionBundle?) {
        when (action) {
            is StageArg -> curFlowValue = Flow.from(action.stage)
            is ActionArg -> processActionEvent(action.action)
        }
    }

    fun btnProfile() {
        save(false)
        curFlowValue = LoginFlow()
    }

    private fun save(isNext: Boolean): Boolean {
        val entryText = entryTextValue()
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
            Stage.ADD_CITY ->
                prefHelper.city = entryText

            Stage.ADD_STREET ->
                prefHelper.street = entryText
            Stage.ADD_EQUIPMENT -> {
                val name = entryText
                if (!name.isEmpty()) {
                    val group = prefHelper.currentProjectGroup
                    if (group != null) {
                        db.tableCollectionEquipmentProject.addLocal(name, group.projectNameId)
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
                    companyEditing?.let {
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
                    if (detectNoteError()) {
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

    // BUTTONS END

    // BUTTON CENTER

    private fun btnCenter() {
        if (confirmCenter()) {
            wasNext = false
            process(curFlowValue.center)
        }
    }

    private fun confirmCenter(): Boolean {
        when (curFlowValue.stage) {
            Stage.LOGIN -> {
                if (detectLoginError()) {
                    return false
                }
                onProfileUpdated()
                // TODO: advance how?
            }
            Stage.PICTURE_1,
            Stage.PICTURE_2,
            Stage.PICTURE_3 -> {
                dispatchPictureRequest()
                return false
            }
            else -> {
            }
        }
        return true
    }

    private fun onProfileUpdated() {
        curFlowValue = CurrentProjectFlow()
    }

    // BUTTON CENTER END

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

    // END PICTURE

    // ON STAGE CHANGED

    fun onStageChanged(flow: Flow) {
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
                dispatchActionEvent(Action.PING)
                prefHelper.saveProjectAndAddressCombo(editProject)
                editProject = false
                checkErrors()
                if (db.tableProjectAddressCombo.count() > 0) {
                    addButtonVisibleValue = true
                }
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
                if (wasNext) {
                    showToast = true
                    wasNext = false
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
            Stage.STATE -> {
                btnCenter()
            }
            else -> {
            }
        }
    }

    private fun setList(list: List<DataPicture>) {
        dispatchActionEvent(Action.SET_PICTURE_LIST(list))
    }

    // SET LIST END
}
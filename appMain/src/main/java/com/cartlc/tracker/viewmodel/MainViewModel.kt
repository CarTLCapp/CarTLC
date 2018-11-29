package com.cartlc.tracker.viewmodel

import android.location.Address
import android.text.InputType
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.BuildConfig
import com.cartlc.tracker.R
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.data.*
import com.cartlc.tracker.model.flow.*
import com.cartlc.tracker.model.misc.*
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.util.BitmapHelper
import com.cartlc.tracker.ui.util.CheckError
import com.cartlc.tracker.ui.util.LocationHelper
import java.io.File
import java.util.*

class MainViewModel(val repo: CarRepository) : BaseViewModel() {

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
        set(value) { repo.curFlowValue = value }

    val isPictureStage: Boolean
        get() = curFlowValue.isPictureStage

    val error: MutableLiveData<ErrorMessage>
        get() = repo.error

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

    private var companyEditing: String? = null
    private var wasNext: Boolean = false
    private var didAutoSkip: Boolean = false

    var detectNoteError: () -> Boolean = { false }
    var detectLoginError: () -> Boolean = { false }
    var entryTextValue: () -> String = { "" }
    var getString: (msg: StringMessage) -> String = { "" }

    private var editProject: Boolean = false
    private var autoNarrowOkay = true
    private var takingPictureFile: File? = null

    private val curProjectHint: String
        get() {
            val sbuf = StringBuilder()
            val name = prefHelper.projectName
            if (name != null) {
                sbuf.append(name)
                sbuf.append("\n")
            }
            sbuf.append(prefHelper.address)
            return sbuf.toString()
        }

    private val editProjectHint: String
        get() {
            val sbuf = StringBuilder()
            sbuf.append(getString(StringMessage.entry_hint_edit_project))
            sbuf.append("\n")
            val name = prefHelper.projectName
            if (name != null) {
                sbuf.append(name)
                sbuf.append("\n")
            }
            sbuf.append(prefHelper.address)
            return sbuf.toString()
        }

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

    private val hasProjectName: Boolean
        get() = prefHelper.projectName != null

    private val isAutoNarrowOkay: Boolean
        get() = wasNext && autoNarrowOkay

    var fab_address: Address? = null
    var fab_addressConfirmOkay = false

    lateinit var buttonsViewModel: ButtonsViewModel
    lateinit var loginViewModel: LoginViewModel
    lateinit var mainListViewModel: MainListViewModel
    lateinit var confirmationViewModel: ConfirmationViewModel
    lateinit var titleViewModel: TitleViewModel
    lateinit var entrySimpleViewModel: EntrySimpleViewModel

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
            Action.NEW_PROJECT -> { onNewProject() }
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
        autoNarrowOkay = false
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
        autoNarrowOkay = true
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

    private fun skip() {
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

        when (flow.stage) {
            Stage.PROJECT -> {
                mainListViewModel.showingValue = true
                titleViewModel.subTitleValue = curProjectHint
                buttonsViewModel.showNextButtonValue = hasProjectName
                setList(StringMessage.title_project, PrefHelper.KEY_PROJECT, db.tableProjects.query(true))
                dispatchActionEvent(Action.GET_LOCATION)
            }
            Stage.COMPANY -> {
                titleViewModel.subTitleValue = editProjectHint
                mainListViewModel.showingValue = true
                buttonsViewModel.showCenterButtonValue = true
                val companies = db.tableAddress.query()
                autoNarrowCompanies(companies.toMutableList())
                val companyNames = getNames(companies)
                if (companyNames.size == 1 && autoNarrowOkay) {
                    prefHelper.company = companyNames[0]
                    skip()
                } else {
                    setList(StringMessage.title_company, PrefHelper.KEY_COMPANY, companyNames)
                    buttonsViewModel.checkCenterButtonIsEdit()
                }
            }
            Stage.ADD_COMPANY -> {
                titleViewModel.titleValue = getString(StringMessage.title_company)
                entrySimpleViewModel.showingValue = true
                entrySimpleViewModel.simpleHintValue = getString(StringMessage.title_company)
                if (buttonsViewModel.isLocalCompany) {
                    companyEditing = prefHelper.company
                    entrySimpleViewModel.simpleTextValue = companyEditing ?: ""
                } else {
                    entrySimpleViewModel.simpleTextValue = ""
                }
            }
            Stage.STATE, Stage.ADD_STATE -> { processStates(flow) }
            Stage.CITY, Stage.ADD_CITY -> { processCities(flow) }
            Stage.STREET, Stage.ADD_STREET -> { processStreets(flow) }
            Stage.CONFIRM_ADDRESS -> {
                if (fab_addressConfirmOkay) {
                    fab_addressConfirmOkay = false
                    titleViewModel.subTitleValue = curProjectHint
                    checkChangeCompanyButtonVisible()
                } else {
                    skip()
                }
            }
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
        }
    }

    private fun processStates(flow: Flow) {
        var isEditing = flow.stage == Stage.ADD_STATE
        titleViewModel.subTitleValue = if (isEditing) editProjectHint else curProjectHint
        mainListViewModel.showingValue = true
        buttonsViewModel.showNextButtonValue = false
        val company = prefHelper.company
        val zipcode = prefHelper.zipCode
        var states: MutableList<String> = db.tableAddress.queryStates(company!!, zipcode).toMutableList()
        if (states.size == 0) {
            val state = zipcode?.let { db.tableZipCode.queryState(it) }
            if (state != null) {
                states = ArrayList()
                states.add(state)
            } else {
                isEditing = true
            }
        }
        val hint: String?
        if (isEditing) {
            states = DataStates.getUnusedStates(states).toMutableList()
            prefHelper.state = null
            hint = null
        } else {
            autoNarrowStates(states)
            if (states.size == 1 && autoNarrowOkay) {
                prefHelper.state = states[0]
                skip()
                return
            } else {
                hint = prefHelper.address
            }
        }
        if (isEditing) {
            setList(StringMessage.title_state, PrefHelper.KEY_STATE, states)
        } else {
            entrySimpleViewModel.helpTextValue = hint
            entrySimpleViewModel.showingValue = true
            setList(StringMessage.title_state, PrefHelper.KEY_STATE, states)

            if (mainListViewModel.keyValue == null) {
                buttonsViewModel.showNextButtonValue = true
            }
            buttonsViewModel.showCenterButtonValue = true
            checkChangeCompanyButtonVisible()
        }
    }

    private fun processCities(flow: Flow) {
        var isEditing = flow.stage == Stage.ADD_CITY
        titleViewModel.subTitleValue = if (isEditing) editProjectHint else curProjectHint
        buttonsViewModel.showNextButtonValue = false
        val company = prefHelper.company
        val zipcode = prefHelper.zipCode
        val state = prefHelper.state
        var cities: MutableList<String> = db.tableAddress.queryCities(company!!, zipcode, state!!).toMutableList()
        if (cities.isEmpty()) {
            val city = zipcode?.let { db.tableZipCode.queryCity(zipcode) }
            if (city != null) {
                cities = ArrayList()
                cities.add(city)
            } else {
                isEditing = true
            }
        }
        val hint: String?
        if (isEditing) {
            cities = mutableListOf()
            hint = null
        } else {
            autoNarrowCities(cities)
            if (cities.size == 1 && autoNarrowOkay) {
                prefHelper.city = cities[0]
                skip()
                return
            } else {
                hint = prefHelper.address
            }
        }
        if (isEditing) {
            entrySimpleViewModel.showingValue = true
            titleViewModel.titleValue = getString(StringMessage.title_city)
            entrySimpleViewModel.simpleTextValue = ""
            entrySimpleViewModel.simpleHintValue = getString(StringMessage.title_city)
        } else {
            entrySimpleViewModel.helpTextValue = hint
            mainListViewModel.showingValue = true
            setList(StringMessage.title_city, PrefHelper.KEY_CITY, cities)
            if (mainListViewModel.keyValue == null) {
                buttonsViewModel.showNextButtonValue = true
            }
            buttonsViewModel.showCenterButtonValue = true
            checkChangeCompanyButtonVisible()
        }
    }

    private fun processStreets(flow: Flow) {
        var isEditing = flow.stage == Stage.ADD_STREET
        buttonsViewModel.showNextButtonValue = false
        titleViewModel.subTitleValue = if (isEditing) editProjectHint else curProjectHint
        var streets = db.tableAddress.queryStreets(
                prefHelper.company!!,
                prefHelper.city!!,
                prefHelper.state!!,
                prefHelper.zipCode)
        if (streets.isEmpty()) {
            isEditing = true
        }
        val hint: String?
        if (isEditing) {
            streets = mutableListOf()
            hint = null
        } else {
            autoNarrowStreets(streets)
            if (streets.size == 1 && autoNarrowOkay) {
                prefHelper.street = streets[0]
                fab_addressConfirmOkay = true
                skip()
                return
            } else {
                hint = prefHelper.address
            }
        }
        if (isEditing) {
            titleViewModel.titleValue = getString(StringMessage.title_street)
            entrySimpleViewModel.showingValue = true
            entrySimpleViewModel.simpleTextValue = ""
            entrySimpleViewModel.simpleHintValue = getString(StringMessage.title_street)
            entrySimpleViewModel.inputTypeValue = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        } else {
            entrySimpleViewModel.helpTextValue = hint
            buttonsViewModel.showCenterButtonValue = true
            mainListViewModel.showingValue = true
            setList(StringMessage.title_street, PrefHelper.KEY_STREET, streets)
            if (mainListViewModel.keyValue == null) {
                buttonsViewModel.showNextButtonValue = true
            }
            checkChangeCompanyButtonVisible()
        }
    }

    private fun autoNarrowCompanies(companies: MutableList<DataAddress>) {
        if (!isAutoNarrowOkay) {
            return
        }
        val companyNames = getNames(companies)
        if (companyNames.size == 1) {
            return
        }
        val address = fab_address
        if (address == null) {
            return
        }
        val reduced = ArrayList<DataAddress>()
        for (company in companies) {
            if (LocationHelper.instance.matchCompany(address, company)) {
                reduced.add(company)
            }
        }
        if (reduced.size == 0) {
            return
        }
        companies.clear()
        companies.addAll(reduced)
    }

    private fun getNames(companies: List<DataAddress>): List<String> {
        val list = ArrayList<String>()
        for (address in companies) {
            if (!list.contains(address.company)) {
                list.add(address.company)
            }
        }
        Collections.sort(list)
        return list
    }

    private fun autoNarrowStates(states: MutableList<String>) {
        if (!isAutoNarrowOkay) {
            return
        }
        if (states.size == 1) {
            return
        }
        if (fab_address == null) {
            return
        }
        val state = LocationHelper.instance.matchState(fab_address!!, states)
        if (state != null) {
            states.clear()
            states.add(state)
        }
    }

    private fun autoNarrowCities(cities: MutableList<String>) {
        if (!isAutoNarrowOkay) {
            return
        }
        if (cities.size == 1) {
            return
        }
        if (fab_address == null) {
            return
        }
        val city = LocationHelper.instance.matchCity(fab_address!!, cities)
        if (city != null) {
            cities.clear()
            cities.add(city)
        }
    }

    private fun autoNarrowStreets(streets: List<String>) {
        if (!isAutoNarrowOkay) {
            return
        }
        if (streets.size == 1) {
            return
        }
        if (fab_address == null) {
            return
        }
        LocationHelper.instance.reduceStreets(fab_address!!, streets.toMutableList())
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

    private fun checkChangeCompanyButtonVisible() {
        if (didAutoSkip) {
            buttonsViewModel.showCenterButtonValue = true
        }
    }

    // ON STAGE CHANGED END

    // SET LIST

    private fun setList(msg: StringMessage, key: String, list: List<String>) {
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
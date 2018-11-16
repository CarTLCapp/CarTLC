package com.cartlc.tracker.viewmodel

import android.location.Address
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
        get() = curFlow.value ?: LoginFlow()
        set(value) {
            curFlow.value = value
        }

    private val isLocalCompany: Boolean
        get() = db.tableAddress.isLocalCompanyOnly(prefHelper.company)

    val isCenterButtonEdit: Boolean
        get() = curFlowValue.stage == Stage.COMPANY && isLocalCompany

    private var companyEditing: String? = null
    private var wasNext: Boolean = false
    var didAutoSkip: Boolean = false
    val isPictureStage: Boolean
        get() = curFlowValue.isPictureStage
    var detectNoteError: () -> Boolean = { false }
    var entryTextValue: () -> String = { "" }
    var detectLoginError: () -> Boolean = { false }
    var getString: (msg: StringMessage) -> Unit = {}
    private var curEntry: DataEntry? = null
    private var editProject: Boolean = false
    private var autoNarrowOkay = true
    private var takingPictureFile: File? = null

    val curProjectHint: String
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

    val editProjectHint: String
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

    val statusHint: String
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

    val hasProjectName: Boolean
        get() = prefHelper.projectName != null

    private val isAutoNarrowOkay: Boolean
        get() = wasNext && autoNarrowOkay

    var fab_address: Address? = null
    var fab_addressConfirmOkay = false

    private fun showConfirmDialog() {
        dispatchActionEvent(Action.CONFIRM_DIALOG)
    }

    fun onCreate() {
        prefHelper.setFromCurrentProjectId()
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

    fun autoRotatePictureResult() {
        if (takingPictureFile != null && takingPictureFile!!.exists()) {
            val degrees = prefHelper.autoRotatePicture
            if (degrees != 0) {
                BitmapHelper.rotate(takingPictureFile!!, degrees)
            }
        }
    }

    fun onRestoreInstanceState(path: String?) {
        if (path != null) {
            takingPictureFile = File(path)
        }
    }

    fun onSaveInstanceState(): String? {
        return takingPictureFile?.absolutePath
    }

    fun checkProjectErrors(): Boolean = repo.checkProjectErrors()

    private fun checkEntryErrors(): DataEntry? = repo.checkEntryErrors()

    private fun checkErrors() {
        if (!prefHelper.doErrorCheck) {
            return
        }
        val entry = checkEntryErrors()
        if (entry != null) {
            dispatchActionEvent(Action.SHOW_TRUCK_ERROR(entry, object : CheckError.CheckErrorResult {
                override fun doEdit() {
                    dispatchActionEvent(Action.EDIT_ENTRY)
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

    fun add(entry: DataEntry) = repo.add(entry)

    fun btnPrev() {
        if (confirmPrev()) {
            wasNext = false
            companyEditing = null
            process(curFlowValue.prev)
        }
    }

    fun btnNext(wasAutoSkip: Boolean = false) {
        if (confirmNext()) {
            didAutoSkip = wasAutoSkip
            companyEditing = null
            advance()
        }
    }

    fun btnPlus() {
        if (prefHelper.currentEditEntryId != 0L) {
            prefHelper.clearLastEntry()
        }
        curFlowValue = TruckFlow()
    }

    fun skip() {
        if (wasNext) {
            btnNext(true)
        } else {
            btnPrev()
        }
    }

    fun advance() {
        wasNext = true
        process(curFlowValue.next)
    }

    private fun process(action: ActionBundle?) {
        when (action) {
            is StageArg -> curFlowValue = Flow.from(action.stage)
            is ActionArg -> dispatchActionEvent(action.action)
        }
    }

    fun btnCenter() {
        if (confirmCenter()) {
            wasNext = false
            process(curFlowValue.center)
        }
    }

    fun btnChangeCompany() {
        autoNarrowOkay = false
        wasNext = false
        curFlowValue = CompanyFlow()
        prefHelper.state = null
        prefHelper.city = null
        prefHelper.company = null
        prefHelper.street = null
        prefHelper.zipCode = null
    }

    fun btnProfile() {
        save(false)
        curFlowValue = LoginFlow()
    }

    fun doSimpleEntryReturn(value: String) {
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

    fun onConfirmOkay() {
        db.tableEntry.add(curEntry!!)
        prefHelper.clearLastEntry()
        curFlowValue = CurrentProjectFlow()
        curEntry = null
        curFlowValue = CurrentProjectFlow()
    }

    fun onProfileUpdated() {
        curFlowValue = CurrentProjectFlow()
    }

    fun onNewProject() {
        autoNarrowOkay = true
        prefHelper.clearCurProject()
        curFlowValue = ProjectFlow()
    }

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

    fun onEmptyList() {
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

    fun onPictureRequestComplete() {
        curFlowValue = curFlowValue
    }

    private fun confirmNext(): Boolean {
        return save(true)
    }

    private fun confirmPrev(): Boolean {
        return save(false)
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
                    showConfirmDialog()
                    return false
                }
            else -> {
            }
        }
        return true
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

    fun computeCurStage() {
        var inEntry = false
        if (prefHelper.lastName.isNullOrBlank()) {
            curFlowValue = LoginFlow()
        } else if (prefHelper.projectName.isNullOrBlank()) {
            curFlowValue = ProjectFlow()
        } else if (prefHelper.company.isNullOrBlank()) {
            curFlowValue = CompanyFlow()
        } else if (prefHelper.state.isNullOrBlank()) {
            curFlowValue = StateFlow()
        } else if (prefHelper.city.isNullOrBlank()) {
            curFlowValue = CityFlow()
        } else if (prefHelper.street.isNullOrBlank()) {
            curFlowValue = StreetFlow()
        } else {
            inEntry = true
        }
        if (inEntry) {
            val hasTruck = !prefHelper.truckValue.isEmpty()
            val items = computeNoteItems()
            val hasNotes = hasNotesEntered(items) && isNotesComplete(items)
            val hasEquip = hasChecked(prefHelper.currentProjectGroup)
            val hasPictures = db.tablePictureCollection.countPictures(prefHelper.currentPictureCollectionId) > 0
            if (!hasTruck && !hasNotes && !hasEquip && !hasPictures) {
                curFlowValue = CurrentProjectFlow()
            } else if (!hasTruck) {
                curFlowValue = TruckFlow()
            } else if (!hasEquip) {
                curFlowValue = EquipmentFlow()
            } else if (!hasNotes) {
                curFlowValue = NotesFlow()
            } else {
                curFlowValue = Picture2Flow()
            }
        }
    }

    private fun computeNoteItems(): List<DataNote> {
        val currentEditEntry: DataEntry? = prefHelper.currentEditEntry
        val currentProjectGroup: DataProjectAddressCombo? = prefHelper.currentProjectGroup
        var items = mutableListOf<DataNote>()
        if (currentEditEntry != null) {
            items = currentEditEntry.notesAllWithValuesOverlaid.toMutableList()
        } else if (currentProjectGroup != null) {
            items = db.tableCollectionNoteProject.getNotes(currentProjectGroup.projectNameId).toMutableList()

        }
        pushToBottom(items, "Other")
        return items
    }

    private fun pushToBottom(items: MutableList<DataNote>, name: String) {
        val others = ArrayList<DataNote>()
        for (item in items) {
            if (item.name.startsWith(name)) {
                others.add(item)
                break
            }
        }
        for (item in others) {
            items.remove(item)
            items.add(item)
        }
    }

    private fun hasNotesEntered(items: List<DataNote>): Boolean {
        for (note in items) {
            if (!note.value.isNullOrBlank()) {
                return true
            }
        }
        return false
    }

    private fun isNotesComplete(items: List<DataNote>): Boolean {
        for (note in items) {
            if (!note.value.isNullOrBlank()) {
                if (note.num_digits > 0 && note.value!!.length != note.num_digits.toInt()) {
                    return false
                }
            }
        }
        return true
    }

    private fun hasChecked(currentProjectGroup: DataProjectAddressCombo?): Boolean {
        if (currentProjectGroup != null) {
            val collection = db.tableCollectionEquipmentProject.queryForProject(currentProjectGroup.projectNameId)
            for (item in collection.equipment) {
                if (item.isChecked) {
                    return true
                }
            }
        }
        return false
    }

    fun processCompanies(action: (List<String>) -> Unit) {
        val companies = db.tableAddress.query()
        autoNarrowCompanies(companies.toMutableList())
        val companyNames = getNames(companies)
        if (companyNames.size == 1 && autoNarrowOkay) {
            prefHelper.company = companyNames[0]
            skip()
        } else {
            action(companyNames)
        }
    }

    fun processEditCompany(action: (company: String) -> Unit) {
        if (isLocalCompany) {
            companyEditing = prefHelper.company
            action(companyEditing ?: "")
        } else {
            action("")
        }
    }

    fun processStates(editing: Boolean, action: (List<String>, hint: String?, edit: Boolean) -> Unit) {
        var isEditing = editing
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
        if (isEditing) {
            states = DataStates.getUnusedStates(states).toMutableList()
            prefHelper.state = null
            action(states, null, true)
        } else {
            autoNarrowStates(states)
            if (states.size == 1 && autoNarrowOkay) {
                prefHelper.state = states[0]
                skip()
            } else {
                action(states, prefHelper.address, false)
            }
        }
    }

    fun processCities(editing: Boolean, action: (List<String>, hint: String?, edit: Boolean) -> Unit) {
        var isEditing = editing
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
        if (isEditing) {
            action(emptyList(), null, true)
        } else {
            autoNarrowCities(cities)
            if (cities.size == 1 && autoNarrowOkay) {
                prefHelper.city = cities[0]
                skip()
            } else {
                action(cities, prefHelper.address, false)
            }
        }
    }

    fun processStreets(editing: Boolean, action: (List<String>, hint: String?, edit: Boolean) -> Unit) {
        var isEditing = editing
        val streets = db.tableAddress.queryStreets(
                prefHelper.company!!,
                prefHelper.city!!,
                prefHelper.state!!,
                prefHelper.zipCode)
        if (streets.isEmpty()) {
            isEditing = true
        }
        if (isEditing) {
            action(emptyList(), null, true)
        } else {
            autoNarrowStreets(streets)
            if (streets.size == 1 && autoNarrowOkay) {
                prefHelper.street = streets[0]
                fab_addressConfirmOkay = true
                skip()
            } else {
                action(streets, prefHelper.address, false)
            }
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

    fun processProject(action: (projects: List<String>) -> Unit) {
        action(db.tableProjects.query(true))
    }

    fun processCurrentProject(showAdd: () -> Unit) {
        prefHelper.saveProjectAndAddressCombo(editProject)
        editProject = false
        checkErrors()
        if (db.tableProjectAddressCombo.count() > 0) {
            showAdd()
        }
    }

    fun processTrucks(action: (trucks: List<String>, truckValue: String, hint: String?) -> Unit) {
        var hint: String?
        if (prefHelper.currentProjectGroup != null) {
            hint = prefHelper.currentProjectGroup?.hintLine
        } else {
            hint = null
        }
        action(
                db.tableTruck.queryStrings(prefHelper.currentProjectGroup),
                prefHelper.truckValue, hint
        )
    }

    fun processStatus() {
        curEntry = null
    }

    fun processConfirm(action: (entry: DataEntry) -> Unit) {
        curEntry = prefHelper.saveEntry()
        curEntry?.let { action(it) }
    }

    fun processPictures(action: (pictures: MutableList<DataPicture>, pictureCount: Int, showToast: Boolean) -> Unit) {
        val pictureCount = prefHelper.numPicturesTaken
        var showToast = false
        if (wasNext) {
            showToast = true
            wasNext = false
        }
        val pictures = db.tablePictureCollection.removeNonExistant(
                db.tablePictureCollection.queryPictures(prefHelper.currentPictureCollectionId
                )).toMutableList()
        action(pictures, pictureCount, showToast)
    }

    fun incAutoRotatePicture(commonRotation: Int) {
        prefHelper.incAutoRotatePicture(commonRotation)
    }

    fun clearAutoRotatePicture() {
        prefHelper.clearAutoRotatePicture()
    }
}
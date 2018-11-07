package com.cartlc.tracker.viewmodel

import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.BuildConfig
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.data.DataEntry
import com.cartlc.tracker.model.data.DataNote
import com.cartlc.tracker.model.data.DataProjectAddressCombo
import com.cartlc.tracker.model.flow.*
import com.cartlc.tracker.model.misc.*
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import java.util.ArrayList

class MainViewModel(val repo: CarRepository) : BaseViewModel() {

    companion object {
        private val ALLOW_EMPTY_TRUCK = BuildConfig.DEBUG // true=Debugging only
    }

    val db: DatabaseTable
        get() = repo.db

    val prefHelper: PrefHelper
        get() = repo.prefHelper

    val curFlow: MutableLiveData<Flow>
        get() = repo.curFlow

    private var curFlowValue: Flow
        get() = curFlow.value ?: LoginFlow()
        set(value) {
            curFlow.value = value
        }

    val isLocalCompany: Boolean
        get() = db.tableAddress.isLocalCompanyOnly(prefHelper.company)

    val isCenterButtonEdit: Boolean
        get() = curFlowValue.stage == Stage.COMPANY && isLocalCompany

    var companyEditing: String? = null
    var wasNext: Boolean = false
    var didAutoSkip: Boolean = false
    val isPictureStage: Boolean
        get() = curFlowValue.isPictureStage
    var detectNoteError: () -> Boolean = { false }
    var entryTextValue: () -> String = { "" }
    var detectLoginError: () -> Boolean = { false }
    var curEntry: DataEntry? = null
    var editProject: Boolean = false
    var autoNarrowOkay = true

    private fun showConfirmDialog() {
        dispatchActionEvent(Action.CONFIRM_DIALOG)
    }

    private fun dispatchPictureRequest() {
        dispatchActionEvent(Action.PICTURE_REQUEST)
    }

    fun checkProjectErrors(): Boolean = repo.checkProjectErrors()

    fun checkEntryErrors(): DataEntry? = repo.checkEntryErrors()

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
        when(action) {
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

//    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
//        if (savedInstanceState != null) {
//            curFlowValue = Flow.from(savedInstanceState.getInt(KEY_STAGE))
//        }
//    }
//
//    fun onSaveInstanceState(outState: Bundle) {
//        outState.putInt(KEY_STAGE, curFlowValue.stage.ordinal)
//    }

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
        dispatchActionEvent(Action.VEHICLES)
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

    fun computeNoteItems(): List<DataNote> {
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

}
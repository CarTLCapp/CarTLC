package com.cartlc.tracker.model

import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.model.data.DataEntry
import com.cartlc.tracker.model.data.DataNote
import com.cartlc.tracker.model.data.DataProjectAddressCombo
import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.model.flow.*
import com.cartlc.tracker.model.msg.ErrorMessage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import java.util.ArrayList

// Open class for testing
open class CarRepository(
        val db: DatabaseTable,
        val prefHelper: PrefHelper,
        val flowUseCase: FlowUseCase
) {
    open val isDevelopment: Boolean
        get() = prefHelper.isDevelopment

    var companyEditing: String? = null

    // region Current Flow

    var curFlowValue: Flow
        get() = flowUseCase.curFlow
        set(value) { flowUseCase.curFlow = value }

    fun onPreviousFlow() {
        flowUseCase.previousFlowValue?.let {
            if (it.stage == Stage.LOGIN) {
                computeCurStage()
            } else {
                curFlowValue = it
            }
        } ?: run {
            computeCurStage()
        }
    }

    // endregion Current Flow

    // region ErrorEvent

    val error: MutableLiveData<ErrorMessage> by lazy {
        MutableLiveData<ErrorMessage>()
    }

    var errorValue: ErrorMessage
        get() = error.value!!
        set(value) {
            error.value = value
        }

    // endregion ErrorEvent

    // region Action

    val actionUseCase: ActionUseCase = ActionUseCaseImpl()

    fun dispatchActionEvent(action: Action) {
        actionUseCase.dispatchActionEvent(action)
    }

    // endregion Action

    init {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            curFlowValue = LoginFlow()
        }
    }

    val hasInsectingList: Boolean
        get() {
            return db.tableVehicleName.vehicleNames.isNotEmpty()
        }

//    fun checkProjectErrors(): Boolean {
//        val entries = db.tableProjectAddressCombo.query()
//        for (combo in entries) {
//            if (!combo.hasValidState) {
//                val address = combo.fix()
//                if (address != null) {
//                    db.tableAddress.update(address)
//                }
//            }
//        }
//        return false
//    }

    fun checkEntryErrors(): DataEntry? {
        val entries = db.tableEntry.query()
        for (entry in entries) {
            if (entry.hasError) {
                return entry
            }
        }
        return null
    }

    open fun clearUploaded() {
        db.clearUploaded()
        prefHelper.clearUploaded()
    }

    fun add(entry: DataEntry) {
        if (db.tableEntry.add(entry)) {
            prefHelper.incNextEquipmentCollectionID()
            prefHelper.incNextPictureCollectionID()
            prefHelper.incNextNoteCollectionID()
        }
    }

    fun onCompanyChanged() {
        curFlowValue = CompanyFlow()
        prefHelper.state = null
        prefHelper.city = null
        prefHelper.company = null
        prefHelper.street = null
        prefHelper.zipCode = null
    }

    // endregion ActionEvent

    // region COMPUTE

    fun computeCurStage() {
        var inEntry = false
        when {
            prefHelper.firstTechCode.isNullOrBlank() -> curFlowValue = LoginFlow()
            prefHelper.projectRootName.isNullOrBlank() -> curFlowValue = RootProjectFlow()
            prefHelper.projectSubName.isNullOrBlank() -> curFlowValue = SubProjectFlow()
            prefHelper.company.isNullOrBlank() -> curFlowValue = CompanyFlow()
            prefHelper.state.isNullOrBlank() -> curFlowValue = StateFlow()
            prefHelper.city.isNullOrBlank() -> curFlowValue = CityFlow()
            prefHelper.street.isNullOrBlank() -> curFlowValue = StreetFlow()
            else -> inEntry = true
        }
        if (inEntry) {
            val hasTruck = prefHelper.truckValue.isNotEmpty()
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

    fun isNotesComplete(items: List<DataNote>): Boolean {
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

    // endregion COMPUTE
}
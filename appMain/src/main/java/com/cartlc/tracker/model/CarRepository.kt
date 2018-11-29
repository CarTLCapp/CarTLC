package com.cartlc.tracker.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.model.data.DataEntry
import com.cartlc.tracker.model.data.DataNote
import com.cartlc.tracker.model.data.DataProjectAddressCombo
import com.cartlc.tracker.model.event.ActionEvent
import com.cartlc.tracker.model.flow.*
import com.cartlc.tracker.model.misc.ErrorMessage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.sql.DatabaseManager
import com.cartlc.tracker.model.table.DatabaseTable
import java.util.ArrayList

class CarRepository(
        val context: Context,
        private val dm: DatabaseManager,
        val prefHelper: PrefHelper
) {
    val db: DatabaseTable
        get() = dm


    /** Current Flow **/

    val curFlow: MutableLiveData<Flow> by lazy {
        MutableLiveData<Flow>()
    }

    var curFlowValue: Flow
        get() = curFlow.value ?: LoginFlow()
        set(value) {
            curFlow.value = value
        }

    /** ErrorEvent **/

    val error: MutableLiveData<ErrorMessage> by lazy {
        MutableLiveData<ErrorMessage>()
    }

    var errorValue: ErrorMessage
        get() = error.value!!
        set(value) { error.value = value }

    /** ActionEvent **/

    private val handleAction: MutableLiveData<ActionEvent> by lazy {
        MutableLiveData<ActionEvent>()
    }

    fun handleActionEvent(): LiveData<ActionEvent> = handleAction

    fun dispatchActionEvent(action: Action) {
        handleAction.value = ActionEvent(action)
    }

    init {
        curFlow.value = LoginFlow()
    }

    val hasInsectingList: Boolean
        get() {
            return dm.tableVehicleName.vehicleNames.isNotEmpty()
        }

    fun checkProjectErrors(): Boolean {
        val entries = db.tableProjectAddressCombo.query()
        for (combo in entries) {
            if (!combo.hasValidState) {
                val address = combo.fix()
                if (address != null) {
                    db.tableAddress.update(address)
                }
            }
        }
        return false
    }

    fun checkEntryErrors(): DataEntry? {
        val entries = db.tableEntry.query()
        for (entry in entries) {
            if (entry.hasError) {
                return entry
            }
        }
        return null
    }

    fun clearUploaded() {
        dm.clearUploaded()
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

    // COMPUTE

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

    // COMPUTE END
}
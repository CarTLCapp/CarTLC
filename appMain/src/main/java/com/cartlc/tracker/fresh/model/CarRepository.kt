/*
 * *
 *   * Copyright 2019, FleetTLC. All rights reserved
 *
 */

package com.cartlc.tracker.fresh.model

import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.fresh.model.core.data.*
import com.cartlc.tracker.fresh.model.event.Action
import com.cartlc.tracker.fresh.model.msg.ErrorMessage
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.flow.*

// TODO: was open class for testing
class CarRepository(
        val db: DatabaseTable,
        val prefHelper: PrefHelper,
        val flowUseCase: FlowUseCase
) {
    val isDevelopment: Boolean
        get() = prefHelper.isDevelopment

    var companyEditing: String? = null
    var editProject: Boolean = false

    // region Current Flow

    var curFlowValue: Flow
        get() = flowUseCase.curFlow
        set(value) {
            flowUseCase.curFlow = value
        }

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

    fun clearUploaded() {
        db.clearUploaded()
        prefHelper.clearUploaded()
    }

    /**
     * Add a new saved entry to the database.
     */
    fun store(entry: DataEntry) {
        if (db.tableEntry.updateOrInsert(entry)) {
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

    fun setIncomplete(entry: DataEntry) {
        entry.isComplete = false
        currentFlowElement?.let { element ->
            entry.flowProgress = (db.tableFlowElement.progress(element.id)?.first ?: 0).toShort()
        }
    }

    // endregion ActionEvent

    // region COMPUTE

    fun computeCurStage() {
        if (prefHelper.firstTechCode.isNullOrBlank()) {
            curFlowValue = LoginFlow()
        } else if (db.tableProjects.query().isEmpty()) {
            when {
                prefHelper.projectRootName.isNullOrBlank() -> curFlowValue = RootProjectFlow()
                prefHelper.company.isNullOrBlank() -> {
                    if (db.tableAddress.count > 0) {
                        curFlowValue = CompanyFlow()
                    } else {
                        curFlowValue = RootProjectFlow()
                    }
                }
                prefHelper.state.isNullOrBlank() -> curFlowValue = StateFlow()
                prefHelper.city.isNullOrBlank() -> curFlowValue = CityFlow()
                prefHelper.street.isNullOrBlank() -> curFlowValue = StreetFlow()
                else -> {
                    curFlowValue = CurrentProjectFlow()
                }
            }
        } else {
            val hasSubProject = !prefHelper.projectSubName.isNullOrBlank()
            if (hasSubProject) {
                val hasTruckNumberValue = !prefHelper.truckNumberValue.isNullOrEmpty()
                val hasTruckNumberPicture = db.tablePicture.countPictures(prefHelper.currentPictureCollectionId, Stage.TRUCK_NUMBER_PICTURE) > 0
                val hasTruckDamagePicture = db.tablePicture.countPictures(prefHelper.currentPictureCollectionId, Stage.TRUCK_DAMAGE_PICTURE) > 0
                val hasTruckDamage = prefHelper.truckHasDamage ?: false
                val hasTruckDamageValue = prefHelper.truckHasDamage != null
                val hasEquipment = prefHelper.projectId?.let { projectId ->
                    db.tableCollectionEquipmentProject.hasEquipment(projectId)
                } ?: false
                val hasEquipmentChecked = prefHelper.currentEditEntry?.equipmentCollection?.hasChecked
                        ?: false
                curFlowValue = if (!hasTruckNumberValue || !hasTruckNumberPicture) {
                    TruckNumberPictureFlow()
                } else if (!hasTruckDamageValue || (hasTruckDamageValue && hasTruckDamage && !hasTruckDamagePicture)) {
                    TruckDamagePictureFlow()
                } else if (hasEquipment && !hasEquipmentChecked) {
                    EquipmentFlow()
                } else {
                    CustomFlow(computeFirstIncompleteFlowElementId)
                }
            } else {
                curFlowValue = CurrentProjectFlow()
            }
        }
    }

    // TODO: TRANSFORM THIS
//    private fun computeNoteItems(): List<DataNote> {
//        val currentEditEntry: DataEntry? = prefHelper.currentEditEntry
//        val currentProjectGroup: DataProjectAddressCombo? = prefHelper.currentProjectGroup
//        var items = mutableListOf<DataNote>()
//        if (currentEditEntry != null) {
//            items = currentEditEntry.notesAllWithValuesOverlaid.toMutableList()
//        } else if (currentProjectGroup != null) {
//            items = db.tableCollectionNoteProject.getNotes(currentProjectGroup.projectNameId).toMutableList()
//        }
//        pushToBottom(items, "Other")
//        return items
//    }

    fun isNotesComplete(items: List<DataNote>): Boolean {
        for (note in items) {
            if (note.numDigits > 0) {
                note.value?.let { value ->
                    if (value.length < note.numDigits.toInt()) {
                        return false
                    }
                } ?: return false
            } else if (note.value.isNullOrEmpty()) {
                return false
            }
        }
        return true
    }

    // endregion COMPUTE

    // region flow element support

    val currentFlowElement: DataFlowElement?
        get() = currentFlowElementId?.let { elementId -> db.tableFlowElement.query(elementId) }

    val currentFlowElementId: Long?
        get() {
            val stage = curFlowValue.stage
            if (stage is Stage.CUSTOM_FLOW) {
                return when {
                    stage.isFirstElement -> firstFlowElementId
                    stage.isLastElement -> lastFlowElementId
                    else -> stage.flowElementId
                }
            }
            return null
        }

    private val firstFlowElementId: Long?
        get() {
            prefHelper.currentProjectGroup?.let { combo ->
                combo.project?.let { project ->
                    db.tableFlow.queryBySubProjectId(project.id.toInt())?.let { flow ->
                        return db.tableFlowElement.first(flow.id)?.let { it }
                    }
                }
            }
            return null
        }

    private val lastFlowElementId: Long?
        get() {
            prefHelper.currentProjectGroup?.let { combo ->
                combo.project?.let { project ->
                    db.tableFlow.queryBySubProjectId(project.id.toInt())?.let { flow ->
                        return db.tableFlowElement.last(flow.id)
                    }
                }
            }
            return null
        }

    val curFlowValueStage: Stage
        get() {
            val flow = curFlowValue
            if (flow.stage is Stage.CUSTOM_FLOW) {
                if (flow.stage.isFirstElement) {
                    firstFlowElementId?.let { firstId ->
                        return Stage.CUSTOM_FLOW(firstId)
                    }
                } else if (flow.stage.isLastElement) {
                    lastFlowElementId?.let { lastId ->
                        return Stage.CUSTOM_FLOW(lastId)
                    }
                }
            }
            return flow.stage
        }

    private val computeFirstIncompleteFlowElementId: Long
        get() {
            val currentEntry = prefHelper.currentEditEntry ?: return Stage.FIRST_ELEMENT
            var elementId = firstFlowElementId ?: Stage.FIRST_ELEMENT
            val lastProgressMade = currentEntry.flowProgress
            var progress = 0
            while (progress < lastProgressMade) {
                db.tableFlowElement.query(elementId)?.let { element ->
                    val stage = Stage.CUSTOM_FLOW(element.id)
                    if (element.hasImages) {
                        val currentNumPictures = db.tablePicture.countPictures(currentEntry.pictureCollectionId, stage)
                        if (currentNumPictures < element.numImages) {
                            return element.id
                        }
                    }
                    val hasNotes = db.tableFlowElementNote.hasNotes(element.id)
                    if (hasNotes) {
                        when (element.type) {
                            DataFlowElement.Type.NONE -> {
                                val notes = db.noteHelper.getNotesFromCurrentFlowElementId(element.id)
                                val notesWithValues = currentEntry.overlayNoteValues(notes)
                                if (!isNotesComplete(notesWithValues)) {
                                    return element.id
                                }
                            }
                            else -> {
                                if (element.hasImages) {
                                    val notes = db.noteHelper.getNotesOverlaidFrom(element.id, currentEntry)
                                    if (notes.isNotEmpty()) {
                                        if (!isNotesComplete(notes)) {
                                            return element.id
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                elementId = db.tableFlowElement.next(elementId) ?: return elementId
                progress = db.tableFlowElement.progress(elementId)?.first ?: return elementId
            }
            return elementId
        }

    // endregion flow element support
}

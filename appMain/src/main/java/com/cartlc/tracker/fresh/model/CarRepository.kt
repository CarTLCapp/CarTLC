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
        if (db.tableProjects.query().isEmpty()) {
            when {
                prefHelper.firstTechCode.isNullOrBlank() -> curFlowValue = LoginFlow()
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
                val hasTruckDamageValue = prefHelper.truckHasDamage != null
                val hasEquipment = prefHelper.projectId?.let { projectId ->
                    db.tableCollectionEquipmentProject.hasEquipment(projectId)
                } ?: false
                if (!hasTruckNumberValue || !hasTruckNumberPicture) {
                    curFlowValue = TruckNumberPictureFlow()
                } else if (!hasTruckDamageValue || (hasTruckNumberValue && !hasTruckDamagePicture)) {
                    curFlowValue = TruckDamagePictureFlow()
                } else if (hasEquipment) {
                    curFlowValue = EquipmentFlow()
                } else {
                    curFlowValue = TruckNumberPictureFlow()
                    // TODO: This it the eventual, yet not with a 0 for flowElementId, but having that
                    // computed based on what is not yet filled up.
//                    curFlowValue = CustomFlow(0)
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
            if (!note.value.isNullOrBlank()) {
                if (note.numDigits > 0 && note.value!!.length != note.numDigits.toInt()) {
                    return false
                }
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

    val currentFlowElementProgress: Pair<Int, Int>?
        get() {
            val stage = curFlowValue.stage
            if (stage is Stage.CUSTOM_FLOW) {
                return db.tableFlowElement.progress(stage.flowElementId)
            }
            return null
        }

    private val firstFlowElementId: Long?
        get() {
            prefHelper.currentProjectGroup?.let { combo ->
                combo.project?.let { project ->
                    db.tableFlow.queryBySubProjectId(project.id.toInt())?.let { flow ->
                        return db.tableFlowElement.first(flow.id)
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

    // endregion flow element support
}

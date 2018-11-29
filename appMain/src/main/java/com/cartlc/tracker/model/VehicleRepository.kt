package com.cartlc.tracker.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.R
import com.cartlc.tracker.model.flow.VehicleStage
import com.cartlc.tracker.model.data.DataVehicle
import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.model.event.ActionEvent
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.app.TBApplication

class VehicleRepository(
        private val context: Context,
        private val dm: DatabaseTable,
        private val prefHelper: PrefHelper
) {

    val app: TBApplication
        get() = context.applicationContext as TBApplication
    val stage: MutableLiveData<VehicleStage> by lazy {
        MutableLiveData<VehicleStage>()
    }
    var stageValue: VehicleStage?
        get() = stage.value
        set(value) {
            stage.value = value
        }

    val inspectingList: List<String>
        get() {
            return dm.tableVehicleName.vehicleNames
        }

    val typeOfInspection = context.resources.getStringArray(R.array.type_of_inspection)
    val headLights = context.resources.getStringArray(R.array.head_lights)
    val tailLights = context.resources.getStringArray(R.array.tail_lights)
    val fluidChecks = context.resources.getStringArray(R.array.fluid_checks)
    val tireInspection = context.resources.getStringArray(R.array.tire_inspection)

    /** ActionEvent **/

    private val handleAction: MutableLiveData<ActionEvent> by lazy {
        MutableLiveData<ActionEvent>()
    }

    fun handleActionEvent(): LiveData<ActionEvent> = handleAction

    fun dispatchActionEvent(action: Action) {
        handleAction.value = ActionEvent(action)
    }
    
    init {
        stageValue = VehicleStage.STAGE_1
    }

    inner class Entered {
        var vehicle = DataVehicle(dm.tableString)

        val mileageValue: String
            get() = if (vehicle.mileage == 0) {
                ""
            } else {
                vehicle.mileage.toString()
            }

        internal val isValidMileage: Boolean
            get() = vehicle.mileage > 0

        internal val isValidExteriorLights: Boolean
            get() = vehicle.exteriorLightIssues.isNotBlank()

        internal val isValidFluids: Boolean
            get() = vehicle.fluidProblemsDetected.isNotBlank()

        internal val isValidDamage: Boolean
            get() = vehicle.exteriorDamage.isNotBlank()

        internal val isValidOther: Boolean
            get() = vehicle.other.isNotBlank()

        internal var hasIssuesExteriorLights: Boolean? = null
        internal var hasIssuesFluids: Boolean? = null
        internal var hasIssuesDamage: Boolean? = null
        internal var hasIssuesOther: Boolean? = null

        internal val noIssuesExteriorLights: Boolean
            get() = hasIssuesExteriorLights == false
        internal val noIssuesFluids: Boolean
            get() = hasIssuesFluids == false
        internal val noIssuesDamage: Boolean
            get() = hasIssuesDamage == false
        internal val noIssuesOther: Boolean
            get() = hasIssuesOther == false

        fun clear() {
            vehicle = DataVehicle(dm.tableString)
            hasIssuesExteriorLights = null
            hasIssuesDamage = null
            hasIssuesFluids = null
            hasIssuesOther = null
        }
    }

    val entered = Entered()

    private val isValidInspecting: Boolean
        get() = inspectingList.contains(entered.vehicle.inspectingValue)
    private val isValidTypeOfInspection: Boolean
        get() = typeOfInspection.toList().contains(entered.vehicle.typeOfInspectionValue)

    val isValidStage1: Boolean
        get() = isValidInspecting
    val isValidStage2: Boolean
        get() = entered.isValidMileage && isValidTypeOfInspection
    val isValidStage3: Boolean
        get() = entered.noIssuesExteriorLights || entered.isValidExteriorLights
    val isValidStage4: Boolean
        get() = entered.noIssuesFluids || entered.isValidFluids
    val isValidStage5: Boolean
        get() = entered.noIssuesDamage || entered.isValidDamage
    val isValidStage6: Boolean
        get() = entered.noIssuesOther || entered.isValidOther

    fun submit() {
        prefHelper.registrationHasChanged = true
        dm.tableVehicle.save(entered.vehicle)
        entered.clear()
        app.ping()
        stageValue = VehicleStage.STAGE_1
    }
}
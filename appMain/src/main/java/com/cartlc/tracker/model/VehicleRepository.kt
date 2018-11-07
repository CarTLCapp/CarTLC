package com.cartlc.tracker.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.R
import com.cartlc.tracker.model.flow.VehicleStage
import android.text.TextUtils
import com.cartlc.tracker.model.data.DataVehicle
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

    val typeOfInspection = context.resources.getStringArray(R.array.type_of_inspection)
    val headLights = context.resources.getStringArray(R.array.head_lights)
    val tailLights = context.resources.getStringArray(R.array.tail_lights)
    val fluidChecks = context.resources.getStringArray(R.array.fluid_checks)
    val tireInspection = context.resources.getStringArray(R.array.tire_inspection)

    init {
        stageValue = VehicleStage.STAGE_1
    }

    val inspectingList = listOf(
            "E350 (CarTLC) #1",
            "Promaster City #2",
            "NV3500 #3",
            "F150 #4",
            "Promaster City #5",
            "City Express #6",
            "City Express #7",
            "Quest #8",
            "Silverado #9",
            "Promaster #10")

    inner class Entered {
        var email: String = prefHelper.email ?: ""
        var vehicle = DataVehicle(dm.tableString)

        val mileageValue: String
            get() = if (vehicle.mileage == 0) {
                ""
            } else {
                vehicle.mileage.toString()
            }

        val isValidEmail: Boolean
            get() {
                return if (TextUtils.isEmpty(email)) {
                    false
                } else {
                    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                }
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

        fun clear() {
            vehicle = DataVehicle(dm.tableString)
        }
    }

    val entered = Entered()

    private val isValidInspecting: Boolean
        get() = inspectingList.contains(entered.vehicle.inspectingValue)
    private val isValidTypeOfInspection: Boolean
        get() = typeOfInspection.toList().contains(entered.vehicle.typeOfInspectionValue)

    val isValidStage1: Boolean
        get() = isValidInspecting && entered.isValidEmail
    val isValidStage2: Boolean
        get() = entered.isValidMileage && isValidTypeOfInspection
    val isValidStage3: Boolean
        get() = entered.isValidExteriorLights
    val isValidStage4: Boolean
        get() = entered.isValidFluids
    val isValidStage5: Boolean
        get() = entered.isValidDamage
    val isValidStage6: Boolean
        get() = entered.isValidOther

    fun submit() {
        prefHelper.email = entered.email
        prefHelper.registrationHasChanged = true
        dm.tableVehicle.save(entered.vehicle)
        entered.clear()
        app.ping()
    }
}
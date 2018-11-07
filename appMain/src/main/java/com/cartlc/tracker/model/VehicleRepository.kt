package com.cartlc.tracker.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.R
import com.cartlc.tracker.model.flow.VehicleStage
import com.cartlc.tracker.model.misc.HashList

class VehicleRepository(context: Context) {

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

    class Entered {
        var email: String = ""
        var mileage: Int? = null
        var exteriorLights: String = ""
        var fluidsOrLeaks: String = ""
        var damage: String = ""
        var other: String = ""
        var inspectionType: String = ""
        var inspecting: String = ""
        var headLights = HashList()
        var tailLights = HashList()
        var fluid = HashList()
        var tireInspection = HashList()

        val mileageValue: String
            get() = mileage?.toString() ?: ""
    }

    val entered = Entered()

}
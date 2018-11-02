package com.cartlc.tracker.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.R
import com.cartlc.tracker.model.flow.VehicleStage

class VehicleRepository(context: Context) {

    val stage: MutableLiveData<VehicleStage> by lazy {
        MutableLiveData<VehicleStage>()
    }

    val typeOfInspection = context.resources.getStringArray(R.array.type_of_inspection)
    val headLights = context.resources.getStringArray(R.array.head_lights)
    val tailLights = context.resources.getStringArray(R.array.tail_lights)
    val fluidChecks = context.resources.getStringArray(R.array.fluid_checks)
    val tireInspection = context.resources.getStringArray(R.array.tire_inspection)

    init {
        stage.value = VehicleStage.STAGE_1
    }

    val inspecting = listOf(
            "E350 (CarTLC) #1",
            "Promaster City #2",
            "NV3500 #3",
            "F150 #4",
            "Promaster City #5",
            "City Express #6",
            "City Express #7",
            "Quest #8",
            "Silverado #9",
            "Promaster #10"
    )


}
package com.cartlc.tracker.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cartlc.tracker.model.VehicleRepository
import com.cartlc.tracker.model.flow.Action
import com.cartlc.tracker.model.flow.VehicleStage
import java.lang.NumberFormatException

class VehicleViewModel(
        val repo: VehicleRepository
) : BaseViewModel() {

    var showFrame1 = ObservableBoolean(true)
    var showFrame2 = ObservableBoolean(false)
    var showFrame3 = ObservableBoolean(false)
    var stage3ListTitle = ObservableField<String>()
    var stage3List2Title = ObservableField<String>()

    var showFrame1Value: Boolean
        get() = showFrame1.get()
        set(value) = showFrame1.set(value)
    var showFrame2Value: Boolean
        get() = showFrame2.get()
        set(value) = showFrame2.set(value)
    var showFrame3Value: Boolean
        get() = showFrame3.get()
        set(value) = showFrame3.set(value)
    var stage3ListTitleValue: String?
        get() = stage3ListTitle.get()
        set(value) = stage3ListTitle.set(value)
    var stage3List2TitleValue: String?
        get() = stage3List2Title.get()
        set(value) = stage3List2Title.set(value)

    var emailTextValue: () -> String = { "" }
    var mileageTextValue: () -> String = { "" }
    var entryTextValue: () -> String = { "" }
    var btnNextVisible: (flag: Boolean) -> Unit = {}

    fun doSimpleEntryEmailReturn(value: String) {
        store(value)
    }

    fun doSimpleEntryMileageReturn(value: String) {
        store(value)
    }

    fun doSimpleEntryReturn(value: String) {
        store(value)
    }

    fun onBtnNext() {
        save()
        if (repo.stageValue == VehicleStage.STAGE_6) {
            repo.submit()
            dispatchActionEvent(Action.SUBMIT)
        } else {
            repo.stageValue = repo.stageValue?.advance()
        }
    }

    fun onBtnPrev() {
        save()
        repo.stageValue = repo.stageValue?.previous()
    }

    private fun save() {
        when (repo.stageValue) {
            VehicleStage.STAGE_1 -> store(emailTextValue())
            VehicleStage.STAGE_2 -> store(mileageTextValue())
            else -> store(entryTextValue())
        }
    }

    private fun store(value: String) {
        try {
            when (repo.stageValue) {
                VehicleStage.STAGE_1 -> repo.entered.email = value
                VehicleStage.STAGE_2 -> repo.entered.vehicle.mileage = value.toInt()
                VehicleStage.STAGE_3 -> repo.entered.vehicle.exteriorLightIssues = value
                VehicleStage.STAGE_4 -> repo.entered.vehicle.fluidProblemsDetected = value
                VehicleStage.STAGE_5 -> repo.entered.vehicle.exteriorDamage = value
                VehicleStage.STAGE_6 -> repo.entered.vehicle.other = value
            }
        } catch (ex: NumberFormatException) {
        }
    }

    fun onRadioSelect(text: String) {
        when (repo.stageValue) {
            VehicleStage.STAGE_1 -> {
                repo.entered.vehicle.inspectingValue = text
                btnNextVisible(repo.isValidStage1)
            }
            VehicleStage.STAGE_2 -> {
                repo.entered.vehicle.typeOfInspectionValue = text
                btnNextVisible(repo.isValidStage2)
            }
        }
    }

    fun onListSelect(text: String, selected: Boolean) {
        when (repo.stageValue) {
            VehicleStage.STAGE_3 -> repo.entered.vehicle.headLights.set(text, selected)
            VehicleStage.STAGE_4 -> repo.entered.vehicle.fluidChecks.set(text, selected)
            VehicleStage.STAGE_5 -> repo.entered.vehicle.tireInspection.set(text, selected)
        }
    }

    fun onList2Select(text: String, selected: Boolean) {
        when (repo.stageValue) {
            VehicleStage.STAGE_3 -> repo.entered.vehicle.tailLights.set(text, selected)
        }
    }

    fun onEmailChanged(text: String) {
        store(text)
        onStageChanged()
    }

    fun onMileageChanged(text: String) {
        store(text)
        onStageChanged()
    }

    fun onEntryChanged(text: String) {
        store(text)
        onStageChanged()
    }

    private fun onStageChanged() {
        repo.stageValue?.let { onStageChanged(it) }
    }

    fun onStageChanged(stage: VehicleStage) {
        when (stage) {
            VehicleStage.STAGE_1 -> {
                btnNextVisible(repo.isValidStage1)
            }
            VehicleStage.STAGE_2 -> {
                btnNextVisible(repo.isValidStage2)
            }
            VehicleStage.STAGE_3 -> {
                btnNextVisible(repo.isValidStage3)
            }
            VehicleStage.STAGE_4 -> {
                btnNextVisible(repo.isValidStage4)
            }
            VehicleStage.STAGE_5 -> {
                btnNextVisible(repo.isValidStage5)
            }
            VehicleStage.STAGE_6 -> {
                btnNextVisible(repo.isValidStage6)
            }
        }
    }
}
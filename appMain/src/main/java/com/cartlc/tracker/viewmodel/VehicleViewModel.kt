package com.cartlc.tracker.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cartlc.tracker.model.VehicleRepository
import com.cartlc.tracker.model.flow.Action
import com.cartlc.tracker.model.flow.ButtonDialog
import com.cartlc.tracker.model.flow.VehicleStage
import java.lang.NumberFormatException

class VehicleViewModel(
        val repo: VehicleRepository
) : BaseViewModel() {

    var showFrame12 = ObservableBoolean(false)
    var showFrame345 = ObservableBoolean(false)
    var stage3ListTitle = ObservableField<String>()
    var stage3List2Title = ObservableField<String>()

    var showFrame12Value: Boolean
        get() = showFrame12.get()
        set(value) = showFrame12.set(value)
    var showFrame345Value: Boolean
        get() = showFrame345.get()
        set(value) = showFrame345.set(value)
    var stage3ListTitleValue: String?
        get() = stage3ListTitle.get()
        set(value) = stage3ListTitle.set(value)
    var stage3List2TitleValue: String?
        get() = stage3List2Title.get()
        set(value) = stage3List2Title.set(value)

    var mileageTextValue: () -> String = { "" }
    var entryTextValue: () -> String = { "" }
    var entryHasCheckedValue: () -> Boolean = { false }
    var btnNextVisible: (flag: Boolean) -> Unit = {}

    val show345EditText: Boolean
        get() {
            val hasIssues = when (repo.stageValue) {
                VehicleStage.STAGE_3 -> repo.entered.hasIssuesExteriorLights
                VehicleStage.STAGE_4 -> repo.entered.hasIssuesFluids
                VehicleStage.STAGE_5 -> repo.entered.hasIssuesDamage
                VehicleStage.STAGE_6 -> repo.entered.hasIssuesOther
                else -> null
            }
            return hasIssues == true
        }

    fun dispatchActionEvent(action: Action) = repo.dispatchActionEvent(action)

    fun handleActionEvent() = repo.handleActionEvent()

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
            VehicleStage.STAGE_2 -> store(mileageTextValue())
            else -> store(entryTextValue())
        }
    }

    private fun store(value: String) {
        try {
            when (repo.stageValue) {
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

    fun onEntryChanged(text: String) {
        store(text)
        onStageChanged()
    }

    fun onEntryPressAction(button: ButtonDialog) {
        val hasIssues = (button == ButtonDialog.YES)
        when (repo.stageValue) {
            VehicleStage.STAGE_3 -> repo.entered.hasIssuesExteriorLights = hasIssues
            VehicleStage.STAGE_4 -> repo.entered.hasIssuesFluids = hasIssues
            VehicleStage.STAGE_5 -> repo.entered.hasIssuesDamage = hasIssues
            VehicleStage.STAGE_6 -> repo.entered.hasIssuesOther = hasIssues
        }
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
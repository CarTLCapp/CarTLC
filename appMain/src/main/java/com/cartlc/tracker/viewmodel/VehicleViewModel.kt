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
                VehicleStage.STAGE_2 -> repo.entered.mileage = value.toInt()
                VehicleStage.STAGE_3 -> repo.entered.exteriorLights = value
                VehicleStage.STAGE_4 -> repo.entered.fluidsOrLeaks = value
                VehicleStage.STAGE_5 -> repo.entered.damage = value
                VehicleStage.STAGE_6 -> repo.entered.other = value
            }
        } catch (ex: NumberFormatException) {
        }
    }

    fun onRadioSelect(text: String) {
        when (repo.stageValue) {
            VehicleStage.STAGE_1 -> repo.entered.inspecting = text
            VehicleStage.STAGE_2 -> repo.entered.inspectionType = text
        }
    }

    fun onListSelect(text: String, selected: Boolean) {
        when (repo.stageValue) {
            VehicleStage.STAGE_3 -> repo.entered.headLights.set(text, selected)
            VehicleStage.STAGE_4 -> repo.entered.fluid.set(text, selected)
            VehicleStage.STAGE_5 -> repo.entered.tireInspection.set(text, selected)
        }
    }

    fun onList2Select(text: String, selected: Boolean) {
        when (repo.stageValue) {
            VehicleStage.STAGE_3 -> repo.entered.tailLights.set(text, selected)
        }
    }
}
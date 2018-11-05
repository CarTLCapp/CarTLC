package com.cartlc.tracker.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cartlc.tracker.model.VehicleRepository
import com.cartlc.tracker.model.flow.Action
import com.cartlc.tracker.model.flow.VehicleStage

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

    fun doSimpleEntryEmailReturn(value: String) {
    }

    fun doSimpleEntryMileageReturn(value: String) {
    }

    fun doSimpleEntryReturn(value: String) {
    }

    fun onBtnNext() {
        if (repo.stage.value == VehicleStage.STAGE_6) {
            dispatchActionEvent(Action.SUBMIT)
        } else {
            repo.stage.value = repo.stage.value?.advance()
        }
    }

    fun onBtnPrev() {
        repo.stage.value = repo.stage.value?.previous()
    }
}
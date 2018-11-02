package com.cartlc.tracker.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cartlc.tracker.model.VehicleRepository

class VehicleViewModel(
        val repo: VehicleRepository
) : BaseViewModel() {

    var showList = ObservableBoolean(true)
    var showList2 = ObservableBoolean(true)
    var title = ObservableField<String>()
    var title2 = ObservableField<String>()

    var showListValue: Boolean
        get() = showList.get()
        set(value) = showList.set(value)
    var showList2Value: Boolean
        get() = showList2.get()
        set(value) = showList2.set(value)
    var titleValue: String?
        get() = title.get()
        set(value) = title.set(value)
    var titleValue2: String?
        get() = title2.get()
        set(value) = title2.set(value)

    fun doSimpleEntryAboveReturn(value: String) {
    }

    fun doSimpleEntryBelowReturn(value: String) {
    }

    fun onBtnNext() {
        repo.stage.value = repo.stage.value?.advance()
    }

    fun onBtnPrev() {
        repo.stage.value = repo.stage.value?.previous()
    }
}
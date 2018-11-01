package com.cartlc.tracker.viewmodel

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.databinding.FragConfirmationBinding
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.LoginFlow
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.app.TBApplication
import javax.inject.Inject

class ConfirmationViewModel(
        private val act: Activity,
        private val binding: FragConfirmationBinding
) : BaseViewModel() {

    @Inject
    lateinit var repo: CarRepository

    val db: DatabaseTable
        get() = repo.db

    val prefHelper: PrefHelper
        get() = repo.prefHelper

    val curFlow: MutableLiveData<Flow>
        get() = repo.curFlow

    private var curFlowValue: Flow
        get() = curFlow.value ?: LoginFlow()
        set(value) {
            curFlow.value = value
        }

    private val app: TBApplication
        get() = act.applicationContext as TBApplication

    init {
        app.carRepoComponent.inject(this)
    }

    var showing: Boolean = false
        set(value) {
            field = value
            binding.invalidateAll()
        }

}
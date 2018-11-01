package com.cartlc.tracker.viewmodel

import android.app.Activity
import com.cartlc.tracker.databinding.FragButtonsBinding
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.ui.app.TBApplication
import javax.inject.Inject

class ButtonsViewModel(
        private val act: Activity,
        private val binding: FragButtonsBinding
) : BaseViewModel() {

    @Inject
    lateinit var repo: CarRepository

    private val prefHelper: PrefHelper
        get() = repo.prefHelper
    private val app: TBApplication
        get() = act.applicationContext as TBApplication

    init {
        app.carRepoComponent.inject(this)
    }

    var showing: Boolean = true
        set(value) {
            field = value
            binding.invalidateAll()
        }
    var prevText: String = ""
        set(value) {
            field = value
            binding.invalidateAll()
        }
    var nextText: String = ""
        set(value) {
            field = value
            binding.invalidateAll()
        }
    var centerText: String = ""
        set(value) {
            field = value
            binding.invalidateAll()
        }
    var showPrevButton: Boolean = false
        set(value) {
            field = value
            binding.invalidateAll()
        }
    var showNextButton: Boolean = false
        set(value) {
            field = value
            binding.invalidateAll()
        }
    var showCenterButton: Boolean = false
        set(value) {
            field = value
            binding.invalidateAll()
        }
    var showChangeButton: Boolean = false
        set(value) {
            field = value
            binding.invalidateAll()
        }

}
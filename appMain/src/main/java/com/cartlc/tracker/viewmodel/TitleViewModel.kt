package com.cartlc.tracker.viewmodel

import android.app.Activity
import com.cartlc.tracker.databinding.FragTitleBinding
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.ui.app.TBApplication
import javax.inject.Inject

class TitleViewModel(
        private val act: Activity,
        private val binding: FragTitleBinding
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

    var title: String = ""
        set(value) {
            field = value
            binding.invalidateAll()
        }
    var subTitle: String? = null
        set(value) {
            field = value
            binding.invalidateAll()
        }
    var showSeparator: Boolean = false
        set(value) {
            field = value
            binding.invalidateAll()
        }
}
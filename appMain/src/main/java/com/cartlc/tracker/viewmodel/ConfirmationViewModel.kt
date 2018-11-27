package com.cartlc.tracker.viewmodel

import android.app.Activity
import androidx.databinding.ObservableBoolean
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.app.TBApplication
import javax.inject.Inject

class ConfirmationViewModel(private val act: Activity) : BaseViewModel() {

    @Inject
    lateinit var repo: CarRepository

    val db: DatabaseTable
        get() = repo.db

    val prefHelper: PrefHelper
        get() = repo.prefHelper

    private val app: TBApplication
        get() = act.applicationContext as TBApplication

    init {
        app.carRepoComponent.inject(this)
    }

    var showing = ObservableBoolean(false)

    var showingValue: Boolean
        get() = showing.get()
        set(value) {
            showing.set(value)
        }

}
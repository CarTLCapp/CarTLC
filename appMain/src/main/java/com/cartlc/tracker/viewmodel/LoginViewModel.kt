package com.cartlc.tracker.viewmodel

import android.app.Activity
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.databinding.FragLoginBinding
import com.cartlc.tracker.model.misc.ErrorMessage
import javax.inject.Inject

class LoginViewModel(
        private val act: Activity,
        private val binding: FragLoginBinding
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

    var firstName: String = prefHelper.firstName ?: ""
    var lastName: String = prefHelper.lastName ?: ""
    var secondaryFirstName: String = prefHelper.secondaryFirstName ?: ""
    var secondaryLastName: String = prefHelper.secondaryLastName ?: ""
    var showing: Boolean = false
        set(value) {
            field = value
            binding.invalidateAll()
        }
    var isSecondaryPromptsEnabled: Boolean = prefHelper.isSecondaryEnabled
        set(value) {
            field = value
            binding.invalidateAll()
        }

    fun afterFirstNameChanged(s: CharSequence) {
        firstName = s.toString()
    }

    fun afterLastNameChanged(s: CharSequence) {
        lastName = s.toString()
    }

    fun onSecondaryLoginChecked(isChecked: Boolean) {
        isSecondaryPromptsEnabled = isChecked
    }

    fun afterSecondaryFirstNameChanged(s: CharSequence) {
        secondaryFirstName = s.toString()
    }

    fun afterSecondaryLastNameChanged(s: CharSequence) {
        secondaryLastName = s.toString()
    }

    fun detectLoginError(): Boolean {
        if (firstName.isBlank() || lastName.isBlank()) {
            error.value = ErrorMessage.ENTER_YOUR_NAME
            return true
        }
        prefHelper.firstName = firstName
        prefHelper.lastName = lastName
        prefHelper.isSecondaryEnabled = isSecondaryPromptsEnabled

        if (prefHelper.isSecondaryEnabled) {
            prefHelper.secondaryFirstName = secondaryFirstName
            prefHelper.secondaryLastName = secondaryLastName
        }
        prefHelper.setRegistrationChanged(true)
        app.ping()
        return false
    }
}
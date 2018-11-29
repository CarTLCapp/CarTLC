/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.viewmodel.frag

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.misc.ErrorMessage
import com.cartlc.tracker.model.misc.StringMessage
import com.cartlc.tracker.viewmodel.BaseViewModel

class LoginViewModel(private val repo: CarRepository) : BaseViewModel() {

    private val prefHelper: PrefHelper
        get() = repo.prefHelper

    private var firstNameEdit = prefHelper.firstName ?: ""
    private var lastNameEdit = prefHelper.lastName ?: ""
    private var secondaryFirstNameEdit = prefHelper.secondaryFirstName ?: ""
    private var secondaryLastNameEdit = prefHelper.secondaryLastName ?: ""

    var showing = ObservableBoolean(false)
    var firstName = ObservableField<String>(firstNameEdit)
    var lastName = ObservableField<String>(lastNameEdit)
    var secondaryFirstName = ObservableField<String>(secondaryFirstNameEdit)
    var secondaryLastName = ObservableField<String>(secondaryLastNameEdit)
    var isSecondaryPromptsEnabled = ObservableBoolean(prefHelper.isSecondaryEnabled)

    lateinit var error: MutableLiveData<ErrorMessage>
    lateinit var buttonsViewModel: ButtonsViewModel

    var getString: (msg: StringMessage) -> String = { "" }
    var dispatchActionEvent: (action: Action) -> Unit = {}

    private val loginValid: Boolean
        get() {
            if (isSecondaryPromptsEnabled.get()) {
                if (secondaryFirstNameEdit.isBlank() || secondaryLastNameEdit.isBlank()) {
                    return false
                }
            }
            return firstNameEdit.isNotBlank() && lastNameEdit.isNotBlank()
        }

    var showingValue: Boolean
        get() = showing.get()
        set(value) {
            showing.set(value)
        }

    fun afterFirstNameChanged(s: CharSequence) {
        firstNameEdit = s.toString()
        detectShowNext()
    }

    fun afterLastNameChanged(s: CharSequence) {
        lastNameEdit = s.toString()
        detectShowNext()
    }

    fun onSecondaryLoginChecked(isChecked: Boolean) {
        isSecondaryPromptsEnabled.set(isChecked)
        detectShowNext()
    }

    fun afterSecondaryFirstNameChanged(s: CharSequence) {
        secondaryFirstNameEdit = s.toString()
        detectShowNext()
    }

    fun afterSecondaryLastNameChanged(s: CharSequence) {
        secondaryLastNameEdit = s.toString()
        detectShowNext()
    }

    private fun detectShowNext() {
        buttonsViewModel.showCenterButtonValue = loginValid
    }

    fun save() {
        prefHelper.firstName = firstNameEdit
        prefHelper.lastName = lastNameEdit
        prefHelper.isSecondaryEnabled = isSecondaryPromptsEnabled.get()

        if (prefHelper.isSecondaryEnabled) {
            prefHelper.secondaryFirstName = secondaryFirstNameEdit
            prefHelper.secondaryLastName = secondaryLastNameEdit
        }
        prefHelper.registrationHasChanged = true
        dispatchActionEvent(Action.PING)
    }

    fun onStageChanged(flow: Flow) {
        when (flow.stage) {
            Stage.LOGIN -> {
                showingValue = true
                buttonsViewModel.showCenterButtonValue = true
                buttonsViewModel.centerTextValue = getString(StringMessage.title_login)
            }
            else -> {
                showingValue = false
            }
        }
    }
}
package com.cartlc.tracker.viewmodel.frag

import android.app.Activity
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.model.misc.ErrorMessage
import com.cartlc.tracker.model.misc.StringMessage
import com.cartlc.tracker.viewmodel.BaseViewModel
import javax.inject.Inject

class LoginViewModel(private val act: Activity) : BaseViewModel() {

    @Inject
    lateinit var repo: CarRepository

    private val prefHelper: PrefHelper
        get() = repo.prefHelper
    private val app: TBApplication
        get() = act.applicationContext as TBApplication

    init {
        app.carRepoComponent.inject(this)
    }

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

    var showingValue: Boolean
        get() = showing.get()
        set(value) {
            showing.set(value)
        }

    fun afterFirstNameChanged(s: CharSequence) {
        firstNameEdit = s.toString()
    }

    fun afterLastNameChanged(s: CharSequence) {
        lastNameEdit = s.toString()
    }

    fun onSecondaryLoginChecked(isChecked: Boolean) {
        isSecondaryPromptsEnabled.set(isChecked)
    }

    fun afterSecondaryFirstNameChanged(s: CharSequence) {
        secondaryFirstNameEdit = s.toString()
    }

    fun afterSecondaryLastNameChanged(s: CharSequence) {
        secondaryLastNameEdit = s.toString()
    }

    fun detectLoginError(): Boolean {
        if (firstNameEdit.isBlank() || lastNameEdit.isBlank()) {
            error.value = ErrorMessage.ENTER_YOUR_NAME
            return true
        }
        prefHelper.firstName = firstNameEdit
        prefHelper.lastName = lastNameEdit
        prefHelper.isSecondaryEnabled = isSecondaryPromptsEnabled.get()

        if (prefHelper.isSecondaryEnabled) {
            prefHelper.secondaryFirstName = secondaryFirstNameEdit
            prefHelper.secondaryLastName = secondaryLastNameEdit
        }
        prefHelper.registrationHasChanged = true
        app.ping()
        return false
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
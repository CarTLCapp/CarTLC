package com.cartlc.tracker.ui.stage.login

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.model.flow.ActionUseCase
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.FlowUseCase
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.msg.MessageHandler
import com.cartlc.tracker.model.msg.StringMessage
import com.cartlc.tracker.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.ui.stage.StageHook

class LoginController(
        boundFrag: BoundFrag,
        private val viewMvc: LoginViewMvc,
        stageHook: StageHook
) : LifecycleObserver, LoginViewMvc.Listener, FlowUseCase.Listener, ActionUseCase.Listener {

    private val repo = boundFrag.repo
    private val prefHelper = repo.prefHelper
    private val messageHandler: MessageHandler = boundFrag.componentRoot.messageHandler
    private val buttonsViewModel = stageHook.buttonsViewModel

    private var firstNameEdit: String = prefHelper.firstName ?: ""
    private var lastNameEdit: String = prefHelper.lastName ?: ""
    private var secondaryFirstNameEdit: String = prefHelper.secondaryFirstName ?: ""
    private var secondaryLastNameEdit: String = prefHelper.secondaryLastName ?: ""
    private var isSecondaryPromptsEnabled: Boolean = prefHelper.isSecondaryEnabled

    init {
        boundFrag.bindObserver(this)
        repo.flowUseCase.registerListener(this)
    }

    private val loginValid: Boolean
        get() {
            if (isSecondaryPromptsEnabled) {
                if (secondaryFirstNameEdit.isBlank() || secondaryLastNameEdit.isBlank()) {
                    return false
                }
            }
            return firstNameEdit.isNotBlank() && lastNameEdit.isNotBlank()
        }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        viewMvc.registerListener(this)
        viewMvc.firstName = firstNameEdit
        viewMvc.lastName = lastNameEdit
        viewMvc.secondaryFirstName = secondaryFirstNameEdit
        viewMvc.secondaryLastName = secondaryLastNameEdit
        viewMvc.secondaryFirstNameEnabled = isSecondaryPromptsEnabled
        viewMvc.secondaryLastNameEnabled = isSecondaryPromptsEnabled
        repo.actionUseCase.registerListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        viewMvc.unregisterListener(this)
        repo.flowUseCase.unregisterListener(this)
        repo.actionUseCase.unregisterListener(this)
    }

    override fun onActionChanged(action: Action) {
        when (action) {
            Action.SAVE_LOGIN_INFO -> save()
            else -> {
            }
        }
    }

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
    }

    override fun onStageChanged(flow: Flow) {
        when (flow.stage) {
            Stage.LOGIN -> {
                buttonsViewModel.showCenterButtonValue = true
                buttonsViewModel.centerTextValue = messageHandler.getString(StringMessage.title_login)
            }
            else -> {
            }
        }
    }

    /// endregion FlowUseCase.Listener

    fun save() {
        prefHelper.firstName = firstNameEdit
        prefHelper.lastName = lastNameEdit
        prefHelper.isSecondaryEnabled = isSecondaryPromptsEnabled

        if (prefHelper.isSecondaryEnabled) {
            prefHelper.secondaryFirstName = secondaryFirstNameEdit
            prefHelper.secondaryLastName = secondaryLastNameEdit
        }
        prefHelper.registrationHasChanged = true

        repo.dispatchActionEvent(Action.PING)
    }

    override fun onFirstNameChanged(value: String) {
        firstNameEdit = value
        detectShowNext()
    }

    override fun onLastNameChanged(value: String) {
        lastNameEdit = value
        detectShowNext()
    }

    override fun onSecondaryFirstNameChanged(value: String) {
        secondaryFirstNameEdit = value
        detectShowNext()
    }

    override fun onSecondaryLastNameChanged(value: String) {
        secondaryLastNameEdit = value
        detectShowNext()
    }

    override fun onSecondaryCheckBoxChanged(value: Boolean) {
        isSecondaryPromptsEnabled = value
        detectShowNext()
        viewMvc.secondaryFirstNameEnabled = isSecondaryPromptsEnabled
        viewMvc.secondaryLastNameEnabled = isSecondaryPromptsEnabled
    }

    private fun detectShowNext() {
        buttonsViewModel.showCenterButtonValue = loginValid
    }
}
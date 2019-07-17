package com.cartlc.tracker.ui.stage

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.transition.Fade
import com.cartlc.tracker.R
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.FlowUseCase
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.buttons.ButtonsUseCase
import com.cartlc.tracker.fresh.ui.confirm.ConfirmFragment
import com.cartlc.tracker.fresh.ui.login.LoginFragment
import com.cartlc.tracker.fresh.ui.title.TitleUseCase
import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.model.msg.StringMessage

class StageNavigator(
        boundAct: BoundAct,
        override val buttonsUseCase: ButtonsUseCase,
        private val titleUseCase: TitleUseCase
) : LifecycleObserver, StageHook, FlowUseCase.Listener {

    var dispatchActionEvent: (action: Action) -> Unit = {}
    var storeRotation: () -> Unit = {}

    private val activity = boundAct.act
    private val loginFragment = LoginFragment(this)
    private val confirmFragment = ConfirmFragment()
    private val repo = boundAct.repo
    private val componentRoot = boundAct.componentRoot
    private val messageHandler = componentRoot.messageHandler

    init {
        boundAct.bindObserver(this)
    }

    // region lifecycle

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        repo.flowUseCase.registerListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        repo.flowUseCase.unregisterListener(this)
    }

    // endregion lifecycle

    private fun bind(fragment: Fragment) {
        val previousFragment = activity.supportFragmentManager.findFragmentById(R.id.frame_fragment)
        if (previousFragment != fragment) {
            previousFragment?.exitTransition = Fade()
            fragment.enterTransition = Fade()
            activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_fragment, fragment)
                    .commit()
        }
    }

    private fun clear() {
        activity.supportFragmentManager.findFragmentById(R.id.frame_fragment)?.let {
            it.exitTransition = Fade()
            activity.supportFragmentManager.beginTransaction()
                    .remove(it)
                    .commit()
        }
    }

    override fun onStageChangedAboutTo(flow: Flow) {
        when (flow.stage) {
            Stage.LOGIN -> {
                bind(loginFragment)
            }
            Stage.CONFIRM -> {
                bind(confirmFragment)
            }
            else -> {
                clear()
            }
        }
    }

    // TODO: All this is in the wrong place, slowly moving things over.

    override fun onStageChanged(flow: Flow) {
        when (flow.stage) {
            Stage.CONFIRM -> {
                buttonsUseCase.nextText = messageHandler.getString(StringMessage.btn_confirm)
                titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_confirmation)
                storeRotation()
            }
            else -> {
            }
        }
    }

    override fun onConfirmOkay() {
        confirmFragment.useCase?.onConfirmOkay()
        dispatchActionEvent(Action.PING)
    }

}

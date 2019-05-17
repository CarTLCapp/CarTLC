package com.cartlc.tracker.ui.stage

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.transition.Fade
import com.cartlc.tracker.R
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.FlowUseCase
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.ui.bits.SoftKeyboardDetect
import com.cartlc.tracker.ui.stage.buttons.ButtonsUseCase
import com.cartlc.tracker.ui.stage.buttons.ButtonsView
import com.cartlc.tracker.ui.stage.login.LoginFragment

class StageNavigator(
        boundAct: BoundAct,
        override val buttonsUseCase: ButtonsUseCase
) : LifecycleObserver, StageHook, FlowUseCase.Listener {

    private val activity = boundAct.act
    private val loginFragment = LoginFragment(this)
    private val repo = boundAct.repo

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
    private fun bind(fragment: Fragment, @IdRes layout: Int, showing: Boolean) {
        val previousFragment = activity.supportFragmentManager.findFragmentById(layout)
        if (showing) {
            if (previousFragment != fragment) {
                previousFragment?.exitTransition = Fade()
                fragment.enterTransition = Fade()
                activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_fragment, fragment)
                        .commit()
            }
        } else {
            if (previousFragment == fragment) {
                previousFragment.exitTransition = Fade()
                activity.supportFragmentManager.beginTransaction()
                        .remove(fragment)
                        .commit()
            }
        }
    }

    override fun onStageChangedAboutTo(flow: Flow) {
        when (flow.stage) {
            Stage.LOGIN -> {
                bind(loginFragment, R.id.frame_fragment,true)
            }
            else -> {
                bind(loginFragment, R.id.frame_fragment,false)
            }
        }
    }

    override fun onStageChanged(flow: Flow) {
    }

}

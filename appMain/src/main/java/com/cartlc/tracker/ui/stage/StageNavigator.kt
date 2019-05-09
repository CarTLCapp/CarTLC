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
import com.cartlc.tracker.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.ui.stage.login.LoginFragment
import com.cartlc.tracker.viewmodel.frag.ButtonsViewModel

class StageNavigator(
        boundAct: BoundAct,
        override val buttonsViewModel: ButtonsViewModel
) : LifecycleObserver, StageHook, FlowUseCase.Listener {

    private val activity = boundAct.act
    private val loginFragment = LoginFragment(this)
    private val repo = boundAct.repo

    init {
        boundAct.bindObserver(this)
        repo.flowUseCase.registerListener(this)
    }

    // region lifecycle

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        repo.flowUseCase.unregisterListener(this)
    }

    // endregion lifecycle
    private fun bind(fragment: Fragment, showing: Boolean) {
        val previousFragment = activity.supportFragmentManager.findFragmentById(R.id.frame_fragment)
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
                bind(loginFragment, true)
            }
            else -> {
                bind(loginFragment, false)
            }
        }
    }

    override fun onStageChanged(flow: Flow) {
    }

}

package com.cartlc.tracker.ui.stage.buttons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cartlc.tracker.ui.app.FactoryController
import com.cartlc.tracker.ui.app.FactoryViewMvc
import com.cartlc.tracker.ui.app.dependencyinjection.ComponentRoot
import com.cartlc.tracker.ui.base.BaseFragment
import com.cartlc.tracker.ui.bits.SoftKeyboardDetect
import com.cartlc.tracker.ui.stage.StageHook

class ButtonsFragment(
        private val stageHook: StageHook,
        private val softKeyboardDetect: SoftKeyboardDetect
) : BaseFragment() {

    private val componentRoot: ComponentRoot
        get() = boundFrag.componentRoot
    private val factoryViewMvc: FactoryViewMvc
        get() = componentRoot.factoryViewMvc
    private val factoryController: FactoryController
        get() = componentRoot.factoryController

    private lateinit var controller: ButtonsController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewMvc = factoryViewMvc.allocButtonsViewMvc(null)
        controller = factoryController.allocButtonsController(boundFrag, viewMvc, stageHook)
        controller.install(softKeyboardDetect)
        return viewMvc.rootView
    }

}
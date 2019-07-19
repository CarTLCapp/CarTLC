package com.cartlc.tracker.fresh.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cartlc.tracker.fresh.ui.app.factory.FactoryController
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.ComponentRoot
import com.cartlc.tracker.fresh.ui.buttons.ButtonsUseCase
import com.cartlc.tracker.ui.base.BaseFragment

class LoginFragment(
        private val buttonsUseCase: ButtonsUseCase
) : BaseFragment() {

    private val componentRoot: ComponentRoot
        get() = boundFrag.componentRoot
    private val factoryViewMvc: FactoryViewMvc
        get() = componentRoot.factoryViewMvc
    private val factoryController: FactoryController
        get() = componentRoot.factoryController

    private lateinit var controller: LoginController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewMvc = factoryViewMvc.allocLoginViewMvc(null)
        controller = factoryController.allocLoginController(boundFrag, viewMvc, buttonsUseCase)
        return viewMvc.rootView
    }

}
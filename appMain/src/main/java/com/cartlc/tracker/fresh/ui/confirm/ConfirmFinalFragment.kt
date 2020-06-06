package com.cartlc.tracker.fresh.ui.confirm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cartlc.tracker.fresh.ui.app.factory.FactoryController
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.ComponentRoot
import com.cartlc.tracker.fresh.ui.base.BaseFragment

class ConfirmFinalFragment: BaseFragment() {

    private val componentRoot: ComponentRoot
        get() = boundFrag.componentRoot
    private val factoryViewMvc: FactoryViewMvc
        get() = componentRoot.factoryViewMvc
    private val factoryController: FactoryController
        get() = componentRoot.factoryController

    var useCase: ConfirmFinalUseCase? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewMvc = factoryViewMvc.allocConfirmViewMvc(context!!)
        useCase = factoryController.allocConfirmController(boundFrag, viewMvc)
        return viewMvc.rootView
    }

}
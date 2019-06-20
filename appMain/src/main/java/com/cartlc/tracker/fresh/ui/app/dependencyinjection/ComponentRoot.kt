package com.cartlc.tracker.fresh.ui.app.dependencyinjection

import android.content.Context
import android.view.LayoutInflater
import com.callassistant.common.rx.SchedulerPlanImpl
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.event.EventController
import com.cartlc.tracker.model.flow.FlowUseCase
import com.cartlc.tracker.model.msg.MessageHandlerImpl
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.server.DCPing
import com.cartlc.tracker.model.server.DCServerRx
import com.cartlc.tracker.fresh.ui.app.FactoryController
import com.cartlc.tracker.fresh.ui.app.FactoryViewMvc

class ComponentRoot(
        context: Context,
        val prefHelper: PrefHelper,
        val flowUseCase: FlowUseCase,
        val repo: CarRepository,
        val ping: DCPing,
        val dcRx: DCServerRx
) {

    val messageHandler = MessageHandlerImpl(context)
    val factoryViewMvc = FactoryViewMvc(LayoutInflater.from(context))
    val schedulerPlan = SchedulerPlanImpl()
    val factoryController = FactoryController(prefHelper, dcRx, schedulerPlan)
    val eventController = EventController()

}
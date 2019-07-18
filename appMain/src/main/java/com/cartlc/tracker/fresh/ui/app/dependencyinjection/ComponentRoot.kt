/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.app.dependencyinjection

import android.content.Context
import android.view.LayoutInflater
import com.cartlc.tracker.fresh.model.SchedulerPlan
import com.cartlc.tracker.fresh.model.SchedulerPlanImpl
import com.cartlc.tracker.fresh.service.ServiceUseCaseImpl
import com.cartlc.tracker.fresh.ui.app.FactoryAdapterController
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.event.EventController
import com.cartlc.tracker.model.flow.FlowUseCase
import com.cartlc.tracker.model.msg.MessageHandler
import com.cartlc.tracker.model.msg.MessageHandlerImpl
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.server.DCPing
import com.cartlc.tracker.model.server.DCServerRx
import com.cartlc.tracker.fresh.ui.app.FactoryController
import com.cartlc.tracker.fresh.ui.app.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.common.ContextWrapper

class ComponentRoot(
        context: Context,
        val prefHelper: PrefHelper,
        val flowUseCase: FlowUseCase,
        val repo: CarRepository,
        val ping: DCPing,
        val dcRx: DCServerRx
) {

    val contextWrapper: ContextWrapper = ContextWrapper(context)
    val messageHandler: MessageHandler = MessageHandlerImpl(context)
    val factoryAdapterController = FactoryAdapterController(repo, messageHandler)
    val factoryViewMvc = FactoryViewMvc(LayoutInflater.from(context), factoryAdapterController)
    val schedulerPlan: SchedulerPlan = SchedulerPlanImpl()
    val factoryController = FactoryController(dcRx, schedulerPlan)
    val eventController = EventController()
    val serviceUseCase = ServiceUseCaseImpl(context)

}
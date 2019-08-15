/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.app.dependencyinjection

import android.content.Context
import android.view.LayoutInflater
import com.cartlc.tracker.fresh.model.SchedulerPlan
import com.cartlc.tracker.fresh.model.SchedulerPlanImpl
import com.cartlc.tracker.fresh.service.ServiceUseCaseImpl
import com.cartlc.tracker.fresh.ui.app.factory.FactoryAdapterController
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.event.EventController
import com.cartlc.tracker.fresh.model.flow.FlowUseCase
import com.cartlc.tracker.fresh.model.misc.NotesHelper
import com.cartlc.tracker.fresh.model.msg.MessageHandler
import com.cartlc.tracker.fresh.model.msg.MessageHandlerImpl
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.fresh.service.endpoint.DCPing
import com.cartlc.tracker.fresh.service.endpoint.DCServerRx
import com.cartlc.tracker.fresh.ui.app.factory.FactoryController
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.common.ContextWrapper
import com.cartlc.tracker.fresh.ui.common.PermissionHelper

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
    val notesHelper = NotesHelper(repo)
    val factoryAdapterController = FactoryAdapterController(repo, notesHelper, messageHandler)
    val factoryViewMvc = FactoryViewMvc(LayoutInflater.from(context), factoryAdapterController)
    val schedulerPlan: SchedulerPlan = SchedulerPlanImpl()
    val factoryController = FactoryController(dcRx, schedulerPlan)
    val eventController = EventController()
    val serviceUseCase = ServiceUseCaseImpl(context)
    val permissionHelper = PermissionHelper(context)

}
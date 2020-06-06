/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.app.dependencyinjection

import android.app.Application
import android.view.LayoutInflater
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.SchedulerPlan
import com.cartlc.tracker.fresh.model.SchedulerPlanImpl
import com.cartlc.tracker.fresh.model.event.EventController
import com.cartlc.tracker.fresh.model.flow.FlowUseCase
import com.cartlc.tracker.fresh.model.msg.MessageHandler
import com.cartlc.tracker.fresh.model.msg.MessageHandlerImpl
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.fresh.service.ServiceUseCaseImpl
import com.cartlc.tracker.fresh.service.alarm.AlarmController
import com.cartlc.tracker.fresh.service.endpoint.DCPing
import com.cartlc.tracker.fresh.service.endpoint.DCServerRx
import com.cartlc.tracker.fresh.service.instabug.InstaBugUseCase
import com.cartlc.tracker.fresh.ui.app.factory.FactoryAdapterController
import com.cartlc.tracker.fresh.ui.app.factory.FactoryController
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.common.BitmapHelper
import com.cartlc.tracker.fresh.ui.common.ContextWrapper
import com.cartlc.tracker.fresh.ui.common.DeviceHelper
import com.cartlc.tracker.fresh.ui.common.PermissionHelper

class ComponentRoot(
        app: Application,
        val prefHelper: PrefHelper,
        val flowUseCase: FlowUseCase,
        val repo: CarRepository,
        val ping: DCPing,
        dcRx: DCServerRx
) {

    val contextWrapper: ContextWrapper = ContextWrapper(app)
    val messageHandler: MessageHandler = MessageHandlerImpl(app)
    private val factoryAdapterController = FactoryAdapterController(repo, messageHandler)
    val factoryViewMvc = FactoryViewMvc(LayoutInflater.from(app), factoryAdapterController)
    private val schedulerPlan: SchedulerPlan = SchedulerPlanImpl()
    val factoryController = FactoryController(dcRx, schedulerPlan)
    val eventController = EventController()
    val serviceUseCase = ServiceUseCaseImpl(app)
    val permissionHelper = PermissionHelper(app)
    val deviceHelper = DeviceHelper(app)
    val bitmapHelper = BitmapHelper()
    val alarmController = AlarmController(app, repo)
    val instaBugUseCase = InstaBugUseCase(app)

}

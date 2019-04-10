package com.cartlc.tracker.ui.app.dependencyinjection

import android.content.Context
import android.view.LayoutInflater
import com.cartlc.tracker.model.event.EventController
import com.cartlc.tracker.model.flow.FlowUseCaseImpl
import com.cartlc.tracker.model.msg.MessageHandlerImpl
import com.cartlc.tracker.ui.app.FactoryController
import com.cartlc.tracker.ui.app.FactoryViewMvc

class ComponentRoot(
        context: Context
) {

    val messageHandler = MessageHandlerImpl(context)
    val factoryViewMvc = FactoryViewMvc(LayoutInflater.from(context))
    val factoryController = FactoryController()
    val eventController = EventController()
    val flowUseCase = FlowUseCaseImpl()

}
package com.cartlc.tracker.fresh.model.event

import org.greenrobot.eventbus.EventBus

class EventController {

    fun post(event: EventCommon) {
        EventBus.getDefault().post(event)
    }

    fun register(subscriber: Any) {
        EventBus.getDefault().register(subscriber)
    }

    fun unregister(subscriber: Any) {
        EventBus.getDefault().unregister(subscriber)
    }

}
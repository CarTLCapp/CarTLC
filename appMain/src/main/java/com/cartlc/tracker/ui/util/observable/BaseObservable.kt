package com.callassistant.util.observable

interface BaseObservable<ListenerType> {

    fun registerListener(listener: ListenerType)

    fun unregisterListener(listener: ListenerType)

}
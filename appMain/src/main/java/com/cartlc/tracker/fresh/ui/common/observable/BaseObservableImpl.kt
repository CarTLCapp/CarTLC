/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.common.observable

import java.util.Collections
import java.util.HashSet

abstract class BaseObservableImpl<ListenerType> : BaseObservable<ListenerType> {

    private val _listeners = HashSet<ListenerType>()

    protected val listeners: Set<ListenerType>
        get() = Collections.unmodifiableSet(_listeners)

    override fun registerListener(listener: ListenerType) {
        _listeners.add(listener)
    }

    override fun unregisterListener(listener: ListenerType) {
        _listeners.remove(listener)
    }
}

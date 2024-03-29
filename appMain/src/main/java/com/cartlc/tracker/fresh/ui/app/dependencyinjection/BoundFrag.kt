/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.app.dependencyinjection

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

class BoundFrag(
        private val frag: Fragment
) : BoundAct(frag.requireActivity()) {

    override val lifecycleOwner: LifecycleOwner
        get() = frag

    override fun bindObserver(observer: LifecycleObserver): LifecycleObserver {
        (frag as LifecycleOwner).lifecycle.addObserver(observer)
        return observer
    }

}
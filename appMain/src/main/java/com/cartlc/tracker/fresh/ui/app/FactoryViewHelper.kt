/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.app

import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.common.FragmentHelper

class FactoryViewHelper(
        private val boundAct: BoundAct
) {

    val fragmentHelper: FragmentHelper
        get() = boundAct.fragmentHelper

}
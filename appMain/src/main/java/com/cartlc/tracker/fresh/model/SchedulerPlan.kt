/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model

import io.reactivex.Scheduler

interface SchedulerPlan {

    val subscribeWith: Scheduler // server side
    val observeWith: Scheduler // client side

}
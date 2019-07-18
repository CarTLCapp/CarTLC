/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SchedulerPlanImpl : SchedulerPlan {

    override val subscribeWith: Scheduler
        get() = Schedulers.io()
    override val observeWith: Scheduler
        get() = AndroidSchedulers.mainThread()

}
package com.callassistant.common.rx

import io.reactivex.Scheduler

interface SchedulerPlan {

    val subscribeWith: Scheduler // server side
    val observeWith: Scheduler // client side

}
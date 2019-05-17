package com.callassistant.common.rx

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SchedulerPlanImpl : SchedulerPlan {

    override val subscribeWith: Scheduler
        get() = Schedulers.io()
    override val observeWith: Scheduler
        get() = AndroidSchedulers.mainThread()

}
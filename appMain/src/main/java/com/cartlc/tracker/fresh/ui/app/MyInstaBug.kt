/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.ui.app

import android.app.Application
import com.instabug.library.Instabug
import com.instabug.library.invocation.InstabugInvocationEvent

object MyInstaBug {

    private const val TOKEN = "fc84f2a2145cd800e63ffc1bb1da06b2"

    fun init(app: Application) {
        Instabug.Builder(app, TOKEN)
                .setInvocationEvents(InstabugInvocationEvent.NONE)
                .build()
    }

}
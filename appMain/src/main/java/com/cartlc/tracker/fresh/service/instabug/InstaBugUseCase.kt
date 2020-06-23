/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.service.instabug

import android.app.Application
//import com.instabug.library.Instabug
//import com.instabug.library.invocation.InstabugInvocationEvent

class InstaBugUseCase(
        app: Application
) {

    companion object {
        private const val TOKEN = "fc84f2a2145cd800e63ffc1bb1da06b2"
    }

    init {
//        Instabug.Builder(app, TOKEN)
//                .setInvocationEvents(InstabugInvocationEvent.SCREENSHOT)
//                .build()
    }

    fun show() {
//        Instabug.show()
    }

}
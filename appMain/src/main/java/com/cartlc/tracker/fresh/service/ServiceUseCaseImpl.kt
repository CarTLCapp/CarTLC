/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.service

import android.content.Context
import com.cartlc.tracker.ui.app.TBApplication

class ServiceUseCaseImpl(
        context: Context
) : ServiceUseCase {

    private val app = context.applicationContext as TBApplication

    override fun ping() {
        app.ping()
    }

    override fun reloadFromServer() {
        app.reloadFromServer()
    }
}
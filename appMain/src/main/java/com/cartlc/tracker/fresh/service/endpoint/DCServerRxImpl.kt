/*
 * *
 *   * Copyright 2019, FleetTLC. All rights reserved
 *
 */

package com.cartlc.tracker.fresh.service.endpoint

import com.cartlc.tracker.fresh.service.endpoint.DCServerRx.Result
import io.reactivex.Single

class DCServerRxImpl(
        private val ping: DCPing
) : DCServerRx {

    override fun sendRegistration(firstCode: String, secondCode: String?): Single<Result> {
        return Single.create<Result> { subscriber ->
            subscriber.onSuccess(Result(ping.sendRegistration(firstCode, secondCode)))
        }
    }

}
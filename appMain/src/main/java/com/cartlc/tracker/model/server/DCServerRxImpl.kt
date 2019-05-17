package com.cartlc.tracker.model.server

import com.cartlc.tracker.model.server.DCServerRx.Result
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
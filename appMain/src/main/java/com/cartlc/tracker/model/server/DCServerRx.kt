package com.cartlc.tracker.model.server

import io.reactivex.Single

interface DCServerRx {

    class Result(val errorMessage: String?)

    fun sendRegistration(firstCode: String, secondCode: String?): Single<Result>

}
/*
 * *
 *   * Copyright 2019, FleetTLC. All rights reserved
 *
 */

package com.cartlc.tracker.fresh.service.endpoint

import io.reactivex.Single

interface DCServerRx {

    class Result(val errorMessage: String?)

    fun sendRegistration(firstCode: String, secondCode: String?): Single<Result>

}
/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.service

interface ServiceUseCase {

    fun ping()
    fun reloadFromServer()

}
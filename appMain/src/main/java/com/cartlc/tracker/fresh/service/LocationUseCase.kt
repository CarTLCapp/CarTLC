/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.service

import android.location.Address

interface LocationUseCase {

    fun getLocation(listener: (address: Address) -> Unit)
    fun reduceStreets(address: Address, streets: List<String>): List<String>
    fun matchState(address: Address, states: List<String>): String?
    fun matchCity(address: Address, cities: List<String>): String?

}
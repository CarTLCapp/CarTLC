/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.service

import android.app.Activity
import android.location.Address
import com.cartlc.tracker.ui.util.helper.LocationHelper

class LocationUseCaseImpl(
        private val act: Activity
) : LocationUseCase {

    override fun getLocation(listener: (address: Address) -> Unit) {
        LocationHelper.instance.requestLocation(act, object : LocationHelper.OnLocationCallback {
            override fun onLocationUpdate(address: Address) {
                listener(address)
            }
        })
    }

    override fun reduceStreets(address: Address, streets: List<String>): List<String> {
        return LocationHelper.instance.reduceStreets(address, streets)
    }

    override fun matchState(address: Address, states: List<String>): String? {
        return LocationHelper.instance.matchState(address, states)
    }

    override fun matchCity(address: Address, cities: List<String>): String? {
        return LocationHelper.instance.matchCity(address, cities)
    }
}
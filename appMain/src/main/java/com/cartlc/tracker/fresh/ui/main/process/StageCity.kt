/*
 * Copyright 2019-2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.model.flow.*
import com.cartlc.tracker.fresh.model.msg.ErrorMessage
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import java.util.ArrayList

class StageCity (
        shared: MainController.Shared
) : ProcessBase(shared) {

    /**
     * At this point we should already have a company and a state entered. If not we need to back track.
     * If we do, we can use these to look up the expected city.
     * If there is already a city entry then use that as an option.
     *
     * @param flow Flow
     */
    fun process(flow: Flow) {
        with(shared) {
            var isEditing = flow.stage == Stage.ADD_CITY
            titleUseCase.subTitleText = if (isEditing) editProjectHint else curProjectHint
            titleUseCase.mainTitleVisible = true
            titleUseCase.subTitleVisible = true
            buttonsController.nextVisible = false
            val company = prefHelper.company
            if (company == null) {
                curFlowValue = CompanyFlow() // No company, abort this and go back to there.
                return
            }
            val state = prefHelper.state
            if (state == null) {
                curFlowValue = StateFlow() // No state, abort this and go back to there.
                return
            }
            val city = prefHelper.city
            val cities = mutableListOf<String>()
            if (!city.isNullOrBlank()) {
                cities.add(city)
            }
            val zipcode = prefHelper.zipCode
            db.tableAddress.queryCities(company, zipcode, state).forEach { addcity ->
                if (!cities.contains(addcity)) {
                    cities.add(addcity)
                }
            }
            if (cities.isEmpty()) {
                zipcode?.let { db.tableZipCode.queryCity(zipcode) }?.let { addcity ->
                    cities.add(addcity)
                } ?: run {
                    isEditing = true
                }
            }
            val hint: String?
            if (isEditing) {
                hint = null
            } else {
                autoNarrowCities(cities)
                if (cities.size == 1 && isAutoNarrowOkay) {
                    prefHelper.city = cities[0]
                    buttonsController.skip()
                    return
                } else {
                    hint = prefHelper.address
                }
            }
            if (isEditing) {
                titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_city)
                entrySimpleUseCase.showing = true
                entrySimpleUseCase.simpleTextClear()
                entrySimpleUseCase.hintValue = messageHandler.getString(StringMessage.title_city)
                if (flow.stage == Stage.CITY) {
                    curFlowValue = AddCityFlow()
                }
            } else {
                entrySimpleUseCase.entryTextValue = hint
                mainListUseCase.visible = true
                setList(StringMessage.title_city, PrefHelper.KEY_CITY, cities)
                if (mainListUseCase.keyValue != null) {
                    buttonsController.nextVisible = true
                }
                buttonsController.centerVisible = true
            }
        }
    }

    private fun autoNarrowCities(cities: MutableList<String>) {
        with(shared) {
            if (!isAutoNarrowOkay) {
                return
            }
            if (cities.size == 1) {
                return
            }
            fabAddress?.let {
                val city = locationUseCase.matchCity(it, cities)
                if (city != null) {
                    cities.clear()
                    cities.add(city)
                }
                cities.sort()
            }
        }
    }

    fun saveAdd(isNext: Boolean): Boolean {
        with(shared) {
            val value = entrySimpleUseCase.entryTextValue ?: ""
            if (value.isBlank()) {
                return !isNext
            } else if (detectedCommaError(value)) {
                errorValue = ErrorMessage.CANNOT_HAVE_COMMAS
                return !isNext
            }
            prefHelper.city = value
            return value.isNotBlank()
        }
    }

}

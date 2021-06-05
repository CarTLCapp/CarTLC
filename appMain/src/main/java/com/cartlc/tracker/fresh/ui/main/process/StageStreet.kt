/*
 * Copyright 2020-2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main.process

import android.text.InputType
import com.cartlc.tracker.fresh.model.flow.*
import com.cartlc.tracker.fresh.model.msg.ErrorMessage
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.model.pref.PrefHelper

class StageStreet(
        shared: MainController.Shared
) : ProcessBase(shared) {

    fun process(flow: Flow) {

        with(shared) {
            var isEditing = flow.stage == Stage.ADD_STREET
            buttonsController.nextVisible = false
            titleUseCase.subTitleText = if (isEditing) editProjectHint else curProjectHint
            titleUseCase.mainTitleVisible = true
            titleUseCase.subTitleVisible = true
            val company = prefHelper.company
            if (company == null) {
                curFlowValue = CompanyFlow() // Expected to have a company by this point -- abort and get it
                return
            }
            val state = prefHelper.state
            if (state == null) {
                curFlowValue = StateFlow() // Expected to have a state at this point -- abort and get it
                return
            }
            val city = prefHelper.city
            if (city == null) {
                curFlowValue = CityFlow() // Expected to have a city at this poinjt -- abort and get it.
                return
            }
            val streets = mutableListOf<String>()
            val street = prefHelper.street
            if (!street.isNullOrEmpty()) {
                streets.add(street)
            }
            db.tableAddress.queryStreets(company, city, state, prefHelper.zipCode).forEach { addstreet ->
                if (!streets.contains(addstreet)) {
                    streets.add(addstreet)
                }
            }
            if (streets.isEmpty()) {
                isEditing = true
            }
            val hint: String?
            if (isEditing) {
                hint = null
            } else {
                autoNarrowStreets(streets)
                if (streets.size == 1 && isAutoNarrowOkay) {
                    prefHelper.street = streets[0]
                    buttonsController.skip()
                    return
                } else {
                    hint = prefHelper.address
                }
            }
            if (isEditing) {
                titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_street)
                entrySimpleUseCase.showing = true
                entrySimpleUseCase.simpleTextClear()
                entrySimpleUseCase.hintValue = messageHandler.getString(StringMessage.title_street)
                entrySimpleUseCase.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                if (flow.stage == Stage.STREET) {
                    curFlowValue = AddStreetFlow()
                }
            } else {
                entrySimpleUseCase.helpValue = hint
                buttonsController.centerVisible = true
                mainListUseCase.visible = true
                setList(StringMessage.title_street, PrefHelper.KEY_STREET, streets)
                if (mainListUseCase.keyValue != null) {
                    buttonsController.nextVisible = true
                }
            }
        }
    }

    private fun autoNarrowStreets(streets: MutableList<String>) {
        with(shared) {
            if (!isAutoNarrowOkay) {
                return
            }
            if (streets.size == 1) {
                return
            }
            fabAddress?.let {
                val reduced = locationUseCase.reduceStreets(it, streets)
                if (reduced.isNotEmpty()) {
                    streets.clear()
                    streets.addAll(reduced)
                }
                streets.sort()
            }
        }
    }

    fun saveAdd(isNext: Boolean): Boolean {
        with (shared) {
            val value = entrySimpleUseCase.entryTextValue ?: ""
            if (value.isBlank()) {
                return !isNext
            }
            if (detectedCommaError(value)) {
                errorValue = ErrorMessage.CANNOT_HAVE_COMMAS
                return !isNext
            }
            prefHelper.street = value
            return value.isNotBlank()
        }
    }
}

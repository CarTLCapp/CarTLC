/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.viewmodel.main

import android.location.Address
import android.text.InputType
import com.cartlc.tracker.model.data.DataAddress
import com.cartlc.tracker.model.data.DataStates
import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.misc.StringMessage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.util.LocationHelper
import java.util.*

class NewProjectVMHolder(val vm: MainVMHolder) {

    private val prefHelper: PrefHelper
        get() = vm.repo.prefHelper

    private val db: DatabaseTable
        get() = vm.repo.db

    private val curProjectHint: String
        get() {
            val sbuf = StringBuilder()
            val name = prefHelper.projectName
            if (name != null) {
                sbuf.append(name)
                sbuf.append("\n")
            }
            sbuf.append(prefHelper.address)
            return sbuf.toString()
        }

    private val editProjectHint: String
        get() {
            val sbuf = StringBuilder()
            sbuf.append(vm.getString(StringMessage.entry_hint_edit_project))
            sbuf.append("\n")
            val name = prefHelper.projectName
            if (name != null) {
                sbuf.append(name)
                sbuf.append("\n")
            }
            sbuf.append(prefHelper.address)
            return sbuf.toString()
        }

    private val hasProjectName: Boolean
        get() = prefHelper.projectName != null

    internal var autoNarrowOkay = true

    private val isAutoNarrowOkay: Boolean
        get() = vm.buttonsViewModel.wasNext && autoNarrowOkay

    var fab_address: Address? = null
    var fab_addressConfirmOkay = false

    fun onStageChanged(flow: Flow) {

        with(vm) {
            when (flow.stage) {
                Stage.PROJECT -> {
                    mainListViewModel.showingValue = true
                    titleViewModel.subTitleValue = curProjectHint
                    buttonsViewModel.showNextButtonValue = hasProjectName
                    setList(StringMessage.title_project, PrefHelper.KEY_PROJECT, db.tableProjects.query(true))
                    dispatchActionEvent(Action.GET_LOCATION)
                }
                Stage.COMPANY -> {
                    titleViewModel.subTitleValue = editProjectHint
                    mainListViewModel.showingValue = true
                    buttonsViewModel.showCenterButtonValue = true
                    val companies = db.tableAddress.query()
                    autoNarrowCompanies(companies.toMutableList())
                    val companyNames = getNames(companies)
                    if (companyNames.size == 1 && autoNarrowOkay) {
                        prefHelper.company = companyNames[0]
                        buttonsViewModel.skip()
                    } else {
                        setList(StringMessage.title_company, PrefHelper.KEY_COMPANY, companyNames)
                        buttonsViewModel.checkCenterButtonIsEdit()
                    }
                }
                Stage.ADD_COMPANY -> {
                    titleViewModel.titleValue = vm.getString(StringMessage.title_company)
                    entrySimpleViewModel.showingValue = true
                    entrySimpleViewModel.simpleHintValue = vm.getString(StringMessage.title_company)
                    if (buttonsViewModel.isLocalCompany) {
                        buttonsViewModel.companyEditing = prefHelper.company
                        entrySimpleViewModel.simpleTextValue = buttonsViewModel.companyEditing ?: ""
                    } else {
                        entrySimpleViewModel.simpleTextValue = ""
                    }
                }
                Stage.STATE, Stage.ADD_STATE -> {
                    processStates(flow)
                }
                Stage.CITY, Stage.ADD_CITY -> {
                    processCities(flow)
                }
                Stage.STREET, Stage.ADD_STREET -> {
                    processStreets(flow)
                }
                Stage.CONFIRM_ADDRESS -> {
                    if (fab_addressConfirmOkay) {
                        fab_addressConfirmOkay = false
                        titleViewModel.subTitleValue = curProjectHint
                        checkChangeCompanyButtonVisible()
                    } else {
                        buttonsViewModel.skip()
                    }
                }
                else -> {}
            }
        }

    }

    private fun processStates(flow: Flow) {
        with(vm) {
            var isEditing = flow.stage == Stage.ADD_STATE
            titleViewModel.subTitleValue = if (isEditing) editProjectHint else curProjectHint
            mainListViewModel.showingValue = true
            buttonsViewModel.showNextButtonValue = false
            val company = prefHelper.company
            val zipcode = prefHelper.zipCode
            var states: MutableList<String> = db.tableAddress.queryStates(company!!, zipcode).toMutableList()
            if (states.size == 0) {
                val state = zipcode?.let { db.tableZipCode.queryState(it) }
                if (state != null) {
                    states = ArrayList()
                    states.add(state)
                } else {
                    isEditing = true
                }
            }
//            val hint: String?
            if (isEditing) {
                states = DataStates.getUnusedStates(states).toMutableList()
                prefHelper.state = null
//                hint = null
            } else {
                autoNarrowStates(states)
                if (states.size == 1 && autoNarrowOkay) {
                    prefHelper.state = states[0]
                    buttonsViewModel.skip()
                    return
                } else {
//                    hint = prefHelper.address
                }
            }
            if (isEditing) {
                setList(StringMessage.title_state, PrefHelper.KEY_STATE, states)
            } else {
//                entrySimpleViewModel.helpTextValue = hint
//                entrySimpleViewModel.showingValue = true
                setList(StringMessage.title_state, PrefHelper.KEY_STATE, states)

                if (mainListViewModel.keyValue == null) {
                    buttonsViewModel.showNextButtonValue = true
                }
                buttonsViewModel.showCenterButtonValue = true
                checkChangeCompanyButtonVisible()
            }
        }
    }

    private fun processCities(flow: Flow) {
        with(vm) {
            var isEditing = flow.stage == Stage.ADD_CITY
            titleViewModel.subTitleValue = if (isEditing) editProjectHint else curProjectHint
            buttonsViewModel.showNextButtonValue = false
            val company = prefHelper.company
            val zipcode = prefHelper.zipCode
            val state = prefHelper.state
            var cities: MutableList<String> = db.tableAddress.queryCities(company!!, zipcode, state!!).toMutableList()
            if (cities.isEmpty()) {
                val city = zipcode?.let { db.tableZipCode.queryCity(zipcode) }
                if (city != null) {
                    cities = ArrayList()
                    cities.add(city)
                } else {
                    isEditing = true
                }
            }
            val hint: String?
            if (isEditing) {
                cities = mutableListOf()
                hint = null
            } else {
                autoNarrowCities(cities)
                if (cities.size == 1 && autoNarrowOkay) {
                    prefHelper.city = cities[0]
                    buttonsViewModel.skip()
                    return
                } else {
                    hint = prefHelper.address
                }
            }
            if (isEditing) {
                entrySimpleViewModel.showingValue = true
                titleViewModel.titleValue = getString(StringMessage.title_city)
                entrySimpleViewModel.simpleTextValue = ""
                entrySimpleViewModel.simpleHintValue = getString(StringMessage.title_city)
            } else {
                entrySimpleViewModel.helpTextValue = hint
                mainListViewModel.showingValue = true
                setList(StringMessage.title_city, PrefHelper.KEY_CITY, cities)
                if (mainListViewModel.keyValue == null) {
                    buttonsViewModel.showNextButtonValue = true
                }
                buttonsViewModel.showCenterButtonValue = true
                checkChangeCompanyButtonVisible()
            }
        }
    }

    private fun processStreets(flow: Flow) {
        with(vm) {
            var isEditing = flow.stage == Stage.ADD_STREET
            buttonsViewModel.showNextButtonValue = false
            titleViewModel.subTitleValue = if (isEditing) editProjectHint else curProjectHint
            var streets = db.tableAddress.queryStreets(
                    prefHelper.company!!,
                    prefHelper.city!!,
                    prefHelper.state!!,
                    prefHelper.zipCode)
            if (streets.isEmpty()) {
                isEditing = true
            }
            val hint: String?
            if (isEditing) {
                streets = mutableListOf()
                hint = null
            } else {
                autoNarrowStreets(streets)
                if (streets.size == 1 && autoNarrowOkay) {
                    prefHelper.street = streets[0]
                    fab_addressConfirmOkay = true
                    buttonsViewModel.skip()
                    return
                } else {
                    hint = prefHelper.address
                }
            }
            if (isEditing) {
                titleViewModel.titleValue = getString(StringMessage.title_street)
                entrySimpleViewModel.showingValue = true
                entrySimpleViewModel.simpleTextValue = ""
                entrySimpleViewModel.simpleHintValue = getString(StringMessage.title_street)
                entrySimpleViewModel.inputTypeValue = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            } else {
                entrySimpleViewModel.helpTextValue = hint
                buttonsViewModel.showCenterButtonValue = true
                mainListViewModel.showingValue = true
                setList(StringMessage.title_street, PrefHelper.KEY_STREET, streets)
                if (mainListViewModel.keyValue == null) {
                    buttonsViewModel.showNextButtonValue = true
                }
                checkChangeCompanyButtonVisible()
            }
        }
    }

    private fun autoNarrowCompanies(companies: MutableList<DataAddress>) {
        if (!isAutoNarrowOkay) {
            return
        }
        val companyNames = getNames(companies)
        if (companyNames.size == 1) {
            return
        }
        val address = fab_address
        if (address == null) {
            return
        }
        val reduced = ArrayList<DataAddress>()
        for (company in companies) {
            if (LocationHelper.instance.matchCompany(address, company)) {
                reduced.add(company)
            }
        }
        if (reduced.size == 0) {
            return
        }
        companies.clear()
        companies.addAll(reduced)
    }

    private fun getNames(companies: List<DataAddress>): List<String> {
        val list = ArrayList<String>()
        for (address in companies) {
            if (!list.contains(address.company)) {
                list.add(address.company)
            }
        }
        Collections.sort(list)
        return list
    }

    private fun autoNarrowStates(states: MutableList<String>) {
        if (!isAutoNarrowOkay) {
            return
        }
        if (states.size == 1) {
            return
        }
        if (fab_address == null) {
            return
        }
        val state = LocationHelper.instance.matchState(fab_address!!, states)
        if (state != null) {
            states.clear()
            states.add(state)
        }
    }

    private fun autoNarrowCities(cities: MutableList<String>) {
        if (!isAutoNarrowOkay) {
            return
        }
        if (cities.size == 1) {
            return
        }
        if (fab_address == null) {
            return
        }
        val city = LocationHelper.instance.matchCity(fab_address!!, cities)
        if (city != null) {
            cities.clear()
            cities.add(city)
        }
    }

    private fun autoNarrowStreets(streets: List<String>) {
        if (!isAutoNarrowOkay) {
            return
        }
        if (streets.size == 1) {
            return
        }
        if (fab_address == null) {
            return
        }
        LocationHelper.instance.reduceStreets(fab_address!!, streets.toMutableList())
    }


    private fun checkChangeCompanyButtonVisible() {
        if (vm.buttonsViewModel.didAutoSkip) {
            vm.buttonsViewModel.showCenterButtonValue = true
        }
    }

}
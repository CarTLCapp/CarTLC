/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.stage.newproject

import android.location.Address
import android.text.InputType
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.model.data.DataAddress
import com.cartlc.tracker.model.data.DataStates
import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.model.event.Button
import com.cartlc.tracker.model.flow.*
import com.cartlc.tracker.model.msg.MessageHandler
import com.cartlc.tracker.model.msg.StringMessage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.ui.bits.entrysimple.EntrySimpleController
import com.cartlc.tracker.ui.stage.buttons.ButtonsUseCase
import com.cartlc.tracker.ui.util.helper.LocationHelper
import com.cartlc.tracker.viewmodel.frag.MainListViewModel
import com.cartlc.tracker.viewmodel.frag.TitleViewModel
import java.util.*

class NewProjectVMHolder(
        boundAct: BoundAct,
        private val buttonsUseCase: ButtonsUseCase,
        private val mainListViewModel: MainListViewModel,
        private val titleViewModel: TitleViewModel,
        private val entrySimpleControl: EntrySimpleController
) : LifecycleObserver, FlowUseCase.Listener, ButtonsUseCase.Listener {

    private val repo = boundAct.repo
    private val messageHandler: MessageHandler = boundAct.componentRoot.messageHandler

    private val prefHelper: PrefHelper
        get() = repo.prefHelper

    private val db: DatabaseTable
        get() = repo.db

    private var curFlowValue: Flow
        get() = repo.curFlowValue
        set(value) {
            repo.curFlowValue = value
        }

    private val curProjectHint: String
        get() {
            val sbuf = StringBuilder()
            val name = prefHelper.projectDashName
            sbuf.append(name)
            sbuf.append("\n")
            sbuf.append(prefHelper.address)
            return sbuf.toString()
        }

    private val editProjectHint: String
        get() {
            val sbuf = StringBuilder()
            sbuf.append(messageHandler.getString(StringMessage.entry_hint_edit_project))
            sbuf.append("\n")
            val name = prefHelper.projectDashName
            sbuf.append(name)
            sbuf.append("\n")
            sbuf.append(prefHelper.address)
            return sbuf.toString()
        }

    private val hasProjectRootName: Boolean
        get() = prefHelper.projectRootName != null

    private val hasProjectSubName: Boolean
        get() = prefHelper.projectSubName != null

    private val hasCompanyName: Boolean
        get() = !prefHelper.company.isNullOrBlank()

    internal var autoNarrowOkay = true

    private val isAutoNarrowOkay: Boolean
        get() = buttonsUseCase.wasNext && autoNarrowOkay

    var fabAddress: Address? = null

    init {
        boundAct.bindObserver(this)
    }

    // region lifecycle

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        repo.flowUseCase.registerListener(this)
        buttonsUseCase.registerListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        repo.flowUseCase.unregisterListener(this)
        buttonsUseCase.unregisterListener(this)
    }

    // endregion lifecycle

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
    }

    override fun onStageChanged(flow: Flow) {
        when (flow.stage) {
            Stage.ROOT_PROJECT -> {
                mainListViewModel.showingValue = true
                titleViewModel.subTitleValue = null
                buttonsUseCase.nextVisible = hasProjectRootName
                setList(StringMessage.title_root_project, PrefHelper.KEY_ROOT_PROJECT, db.tableProjects.queryRootProjectNames())
                dispatchActionEvent(Action.GET_LOCATION)
            }
            Stage.SUB_PROJECT -> {
                prefHelper.projectRootName?.let { rootName ->
                    mainListViewModel.showingValue = true
                    titleViewModel.subTitleValue = curProjectHint
                    buttonsUseCase.nextVisible = hasProjectSubName
                    setList(StringMessage.title_sub_project, PrefHelper.KEY_SUB_PROJECT, db.tableProjects.querySubProjectNames(rootName))
                } ?: run {
                    curFlowValue = RootProjectFlow()
                }
            }
            Stage.COMPANY -> {
                titleViewModel.subTitleValue = editProjectHint
                mainListViewModel.showingValue = true
                buttonsUseCase.nextVisible = hasCompanyName
                buttonsUseCase.centerVisible = true
                val companies = db.tableAddress.query()
                autoNarrowCompanies(companies.toMutableList())
                val companyNames = getNames(companies)
                if (companyNames.size == 1 && isAutoNarrowOkay) {
                    prefHelper.company = companyNames[0]
                    buttonsUseCase.skip()
                } else {
                    setList(StringMessage.title_company, PrefHelper.KEY_COMPANY, companyNames)
                    buttonsUseCase.checkCenterButtonIsEdit()
                }
            }
            Stage.ADD_COMPANY -> {
                titleViewModel.titleValue = messageHandler.getString(StringMessage.title_company)
                entrySimpleControl.showing = true
                entrySimpleControl.hintValue = messageHandler.getString(StringMessage.title_company)
                if (prefHelper.isLocalCompany) {
                    repo.companyEditing = prefHelper.company
                    repo.companyEditing?.let {
                        entrySimpleControl.entryTextValue = it
                    } ?: entrySimpleControl.simpleTextClear()
                } else {
                    entrySimpleControl.simpleTextClear()
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
                titleViewModel.subTitleValue = curProjectHint
                titleViewModel.titleValue = messageHandler.getString(StringMessage.title_confirmation)
                buttonsUseCase.centerVisible = false
                buttonsUseCase.nextVisible = true
            }
            else -> {
            }
        }
    }

    private fun setList(msg: StringMessage, key: String, list: List<String>) {
        titleViewModel.titleValue = messageHandler.getString(msg)
        mainListViewModel.curKey = key
        if (list.isEmpty()) {
            mainListViewModel.showingValue = false
            onEmptyList()
        } else {
            mainListViewModel.showingValue = true
            dispatchActionEvent(Action.SET_MAIN_LIST(list))
        }
    }

    private fun onEmptyList() {
        when (curFlowValue.stage) {
            Stage.ROOT_PROJECT -> {
                prefHelper.reloadProjects()
                dispatchActionEvent(Action.PING)
            }
            Stage.SUB_PROJECT -> curFlowValue = RootProjectFlow()
            Stage.COMPANY,
            Stage.STREET,
            Stage.CITY,
            Stage.EQUIPMENT,
            Stage.STATE -> buttonsUseCase.dispatch(Button.BTN_CENTER)
            else -> {
            }
        }
    }

    // endregion FlowUseCase.Listener

    // region ButtonsUseCase.Listener

    override fun onButtonConfirm(action: Button): Boolean {
        return true
    }

    override fun onButtonEvent(action: Button) {
        when (action) {
            Button.BTN_NEXT -> repo.companyEditing = null
            else -> {}
        }
    }

    // endregion ButtonsUseCase.Listener

    fun dispatchActionEvent(action: Action) = repo.dispatchActionEvent(action)

    private fun processStates(flow: Flow) {
        var isEditing = flow.stage == Stage.ADD_STATE
        titleViewModel.subTitleValue = if (isEditing) editProjectHint else curProjectHint
        mainListViewModel.showingValue = true
        buttonsUseCase.nextVisible = false
        val company = prefHelper.company
        val zipcode = prefHelper.zipCode
        if (company == null) {
            curFlowValue = CompanyFlow()
            return
        }
        var states: MutableList<String> = db.tableAddress.queryStates(company, zipcode).toMutableList()
        if (states.size == 0) {
            val state = zipcode?.let { db.tableZipCode.queryState(it) }
            if (state != null) {
                states = ArrayList()
                states.add(state)
            } else {
                isEditing = true
            }
        }
        if (isEditing) {
            states = DataStates.getUnusedStates(states).toMutableList()
            prefHelper.state = null
        } else {
            autoNarrowStates(states)
            if (states.size == 1 && isAutoNarrowOkay) {
                prefHelper.state = states[0]
                buttonsUseCase.skip()
                return
            }
        }
        if (isEditing) {
            setList(StringMessage.title_state, PrefHelper.KEY_STATE, states)
        } else {
            setList(StringMessage.title_state, PrefHelper.KEY_STATE, states)

            if (mainListViewModel.keyValue == null) {
                buttonsUseCase.nextVisible = true
            }
            buttonsUseCase.centerVisible = true
        }
    }

    private fun processCities(flow: Flow) {
        var isEditing = flow.stage == Stage.ADD_CITY
        titleViewModel.subTitleValue = if (isEditing) editProjectHint else curProjectHint
        buttonsUseCase.nextVisible = false
        val company = prefHelper.company
        val zipcode = prefHelper.zipCode
        val state = prefHelper.state
        if (company == null) {
            curFlowValue = CompanyFlow()
            return
        }
        if (state == null) {
            curFlowValue = StateFlow()
            return
        }
        var cities: MutableList<String> = db.tableAddress.queryCities(company, zipcode, state).toMutableList()
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
            if (cities.size == 1 && isAutoNarrowOkay) {
                prefHelper.city = cities[0]
                buttonsUseCase.skip()
                return
            } else {
                hint = prefHelper.address
            }
        }
        if (isEditing) {
            titleViewModel.titleValue = messageHandler.getString(StringMessage.title_city)
            entrySimpleControl.showing = true
            entrySimpleControl.simpleTextClear()
            entrySimpleControl.hintValue = messageHandler.getString(StringMessage.title_city)
            if (flow.stage == Stage.CITY) {
                curFlowValue = AddCityFlow()
            }
        } else {
            entrySimpleControl.entryTextValue = hint
            mainListViewModel.showingValue = true
            setList(StringMessage.title_city, PrefHelper.KEY_CITY, cities)
            if (mainListViewModel.keyValue == null) {
                buttonsUseCase.nextVisible = true
            }
            buttonsUseCase.centerVisible = true
        }
    }

    private fun processStreets(flow: Flow) {
        var isEditing = flow.stage == Stage.ADD_STREET
        buttonsUseCase.nextVisible = false
        titleViewModel.subTitleValue = if (isEditing) editProjectHint else curProjectHint
        val company = prefHelper.company
        val city = prefHelper.city
        val state = prefHelper.state
        if (company == null) {
            curFlowValue = CompanyFlow()
            return
        }
        if (state == null) {
            curFlowValue = StateFlow()
            return
        }
        if (city == null) {
            curFlowValue = CityFlow()
            return
        }
        var streets = db.tableAddress.queryStreets(
                company,
                city,
                state,
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
            if (streets.size == 1 && isAutoNarrowOkay) {
                prefHelper.street = streets[0]
                buttonsUseCase.skip()
                return
            } else {
                hint = prefHelper.address
            }
        }
        if (isEditing) {
            titleViewModel.titleValue = messageHandler.getString(StringMessage.title_street)
            entrySimpleControl.showing = true
            entrySimpleControl.simpleTextClear()
            entrySimpleControl.hintValue = messageHandler.getString(StringMessage.title_street)
            entrySimpleControl.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            if (flow.stage == Stage.STREET) {
                curFlowValue = AddStreetFlow()
            }
        } else {
            entrySimpleControl.helpValue = hint
            buttonsUseCase.centerVisible = true
            mainListViewModel.showingValue = true
            setList(StringMessage.title_street, PrefHelper.KEY_STREET, streets)
            if (mainListViewModel.keyValue == null) {
                buttonsUseCase.nextVisible = true
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
        val address = fabAddress ?: return
        val reduced = ArrayList<DataAddress>()
        for (company in companies) {
            if (LocationHelper.instance.matchCompany(address, company)) {
                reduced.add(company)
            }
        }
        if (reduced.isEmpty()) {
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
        list.sort()
        return list
    }

    private fun autoNarrowStates(states: MutableList<String>) {
        if (!isAutoNarrowOkay) {
            return
        }
        if (states.size == 1) {
            return
        }
        if (fabAddress == null) {
            return
        }
        val state = LocationHelper.instance.matchState(fabAddress!!, states)
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
        if (fabAddress == null) {
            return
        }
        val city = LocationHelper.instance.matchCity(fabAddress!!, cities)
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
        if (fabAddress == null) {
            return
        }
        LocationHelper.instance.reduceStreets(fabAddress!!, streets.toMutableList())
    }

}
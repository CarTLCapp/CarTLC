package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.model.core.data.DataAddress
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.msg.StringMessage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.ui.util.helper.LocationHelper
import java.util.ArrayList

class StageCompany(
        shared: MainController.Shared
) : ProcessBase(shared) {

    fun process(flow: Flow) {
        with(shared) {
            when(flow.stage) {
                Stage.COMPANY -> {
                    titleUseCase.subTitleText = editProjectHint
                    mainListUseCase.visible = true
                    titleUseCase.subTitleText = null
                    titleUseCase.mainTitleVisible = true
                    buttonsUseCase.nextVisible = hasCompanyName
                    val companies = db.tableAddress.query()
                    autoNarrowCompanies(companies.toMutableList())
                    val companyNames = getNames(companies)
                    if (companyNames.size == 1 && isAutoNarrowOkay) {
                        prefHelper.company = companyNames[0]
                        buttonsUseCase.skip()
                    } else {
                        setList(StringMessage.title_company, PrefHelper.KEY_COMPANY, companyNames)
                        checkCenterButtonIsEdit()
                    }
                }
                Stage.ADD_COMPANY -> {
                    titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_company)
                    titleUseCase.subTitleText = null
                    titleUseCase.mainTitleVisible = true
                    titleUseCase.subTitleVisible = true
                    entrySimpleUseCase.showing = true
                    entrySimpleUseCase.hintValue = messageHandler.getString(StringMessage.title_company)
                    if (prefHelper.isLocalCompany) {
                        repo.companyEditing = prefHelper.company
                        repo.companyEditing?.let {
                            entrySimpleUseCase.entryTextValue = it
                        } ?: entrySimpleUseCase.simpleTextClear()
                    } else {
                        entrySimpleUseCase.simpleTextClear()
                    }
                }
            }
        }
    }

    private fun autoNarrowCompanies(companies: MutableList<DataAddress>) {
        with(shared) {
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

}
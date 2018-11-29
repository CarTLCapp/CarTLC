/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.viewmodel.main

import android.app.Activity

import com.cartlc.tracker.BuildConfig
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.data.DataNote
import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.model.event.Button
import com.cartlc.tracker.model.flow.*
import com.cartlc.tracker.model.misc.ErrorMessage
import com.cartlc.tracker.model.misc.StringMessage
import com.cartlc.tracker.model.misc.TruckStatus
import com.cartlc.tracker.viewmodel.frag.ButtonsViewModel

class MainButtonsViewModel(repo: CarRepository) : ButtonsViewModel(repo) {

    companion object {
        private val ALLOW_EMPTY_TRUCK = BuildConfig.DEBUG // true=Debugging only
    }

    val isLocalCompany: Boolean
        get() = db.tableAddress.isLocalCompanyOnly(prefHelper.company)

    val isCenterButtonEdit: Boolean
        get() = curFlowValue.stage == Stage.COMPANY && isLocalCompany

    var entryTextValue: () -> String = { "" }
    var notes: () -> List<DataNote> = { emptyList() }

    internal var companyEditing: String? = null
    internal var wasNext: Boolean = false
    internal var didAutoSkip: Boolean = false

    private var errorValue: ErrorMessage
        get() = repo.errorValue
        set(value) {
            repo.errorValue = value
        }

    fun dispatchActionEvent(action: Action) {
        repo.dispatchActionEvent(action)
    }

    fun reset(flow: Flow) {
        showChangeButtonValue = false
        showCenterButtonValue = false
        centerTextValue = getString(StringMessage.btn_add)
        showNextButtonValue = flow.hasNext
        nextTextValue = getString(StringMessage.btn_next)
        showPrevButtonValue = flow.hasPrev
        prevTextValue = getString(StringMessage.btn_prev)
    }

    fun checkCenterButtonIsEdit() {
        if (isCenterButtonEdit) {
            centerTextValue = getString(StringMessage.btn_edit)
        } else {
            centerTextValue = getString(StringMessage.btn_add)
        }
    }

    private fun btnPrev() {
        if (confirmPrev()) {
            wasNext = false
            companyEditing = null
            curFlowValue.prev()
        }
    }

    private fun confirmPrev(): Boolean {
        return save(false)
    }

    private fun btnNext(wasAutoSkip: Boolean = false) {
        if (confirmNext()) {
            didAutoSkip = wasAutoSkip
            companyEditing = null
            advance()
        }
    }

    private fun confirmNext(): Boolean {
        return save(true)
    }

    private fun btnCenter() {
        if (confirmCenter()) {
            wasNext = false
            curFlowValue.center()
        }
    }

    private fun confirmCenter(): Boolean {
        when (curFlowValue.stage) {
            Stage.LOGIN -> dispatchActionEvent(Action.SAVE_LOGIN_INFO)
        }
        return true
    }

    fun btnProfile() {
        save(false)
        curFlowValue = LoginFlow()
    }

    internal fun skip() {
        if (wasNext) {
            btnNext(true)
        } else {
            btnPrev()
        }
    }

    private fun advance() {
        wasNext = true
        curFlowValue.next()
    }

    private fun save(isNext: Boolean): Boolean {
        val entryText = entryTextValue()
        when (curFlowValue.stage) {
            Stage.TRUCK ->
                if (entryText.isEmpty()) {
                    if (isNext) {
                        errorValue = ErrorMessage.NEED_A_TRUCK
                    }
                    if (!ALLOW_EMPTY_TRUCK) {
                        return false
                    }
                    // For debugging purposes only.
                    prefHelper.truckNumber = null
                    prefHelper.licensePlate = null
                    prefHelper.doErrorCheck = true
                } else {
                    prefHelper.parseTruckValue(entryText)
                }
            Stage.ADD_CITY -> prefHelper.city = entryText
            Stage.ADD_STREET -> prefHelper.street = entryText
            Stage.ADD_EQUIPMENT -> {
                val name = entryText
                if (!name.isEmpty()) {
                    val group = prefHelper.currentProjectGroup
                    if (group != null) {
                        db.tableCollectionEquipmentProject.addLocal(name, group.projectNameId)
                    }
                }
            }
            Stage.EQUIPMENT ->
                if (isNext) {
                    if (db.tableEquipment.countChecked() == 0) {
                        errorValue = ErrorMessage.NEED_EQUIPMENT
                        return false
                    }
                }
            Stage.ADD_COMPANY -> {
                val newCompanyName = entryText.trim { it <= ' ' }
                if (isNext) {
                    if (newCompanyName.isEmpty()) {
                        errorValue = ErrorMessage.NEED_NEW_COMPANY
                        return false
                    }
                    prefHelper.company = newCompanyName
                    companyEditing?.let {
                        val companies = db.tableAddress.queryByCompanyName(it)
                        for (address in companies) {
                            address.company = newCompanyName
                            db.tableAddress.update(address)
                        }
                    }
                }
            }
            Stage.COMPANY ->
                if (isNext) {
                    if (prefHelper.company.isNullOrBlank()) {
                        errorValue = ErrorMessage.NEED_COMPANY
                        return false
                    }
                }
            Stage.NOTES ->
                if (isNext) {
                    if (!repo.isNotesComplete(notes())) {
                        dispatchActionEvent(Action.SHOW_NOTE_ERROR)
                        return false
                    }
                }
            Stage.STATUS ->
                if (isNext) {
                    if (prefHelper.status === TruckStatus.UNKNOWN) {
                        errorValue = ErrorMessage.NEED_STATUS
                        return false
                    }
                }
            Stage.CONFIRM ->
                if (isNext) {
                    dispatchActionEvent(Action.CONFIRM_DIALOG)
                    return false
                }
            else -> {
            }
        }
        return true
    }

    fun showNoteErrorOk() {
        if (BuildConfig.DEBUG) {
            advance()
        } else {
            btnNext()
        }
    }

    fun onButtonDispatch(button: Button) {
        when (button) {
            Button.BTN_PREV -> btnPrev()
            Button.BTN_NEXT -> btnNext()
            Button.BTN_CENTER -> btnCenter()
            else -> {
            }
        }
    }

}
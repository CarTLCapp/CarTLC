package com.cartlc.tracker.fresh.ui.main.process

import android.text.InputType
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.model.pref.PrefHelper

class StageTruck(
        shared: MainController.Shared
) : ProcessBase(shared) {

    fun process() {
        with(shared) {
            prefHelper.saveProjectAndAddressCombo(modifyCurrent = false, needsValidServerId = true)

            entrySimpleUseCase.showing = true
            entrySimpleUseCase.hintValue = messageHandler.getString(StringMessage.title_truck)
            entrySimpleUseCase.helpValue = messageHandler.getString(StringMessage.entry_hint_truck)
            entrySimpleUseCase.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            val hint: String?
            if (prefHelper.currentProjectGroup != null) {
                hint = prefHelper.currentProjectGroup?.hintLine
            } else {
                hint = null
            }
            val trucks = db.tableTruck.queryStrings(prefHelper.currentProjectGroup)
            entrySimpleUseCase.entryTextValue = prefHelper.truckValue
            setList(StringMessage.title_truck, PrefHelper.KEY_TRUCK, trucks)
            titleUseCase.subTitleText = hint
            titleUseCase.subTitleVisible = true
        }
    }
}
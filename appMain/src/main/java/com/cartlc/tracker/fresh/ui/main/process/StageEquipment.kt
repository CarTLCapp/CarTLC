/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.flow.Flow
import com.cartlc.tracker.fresh.model.flow.Stage
import com.cartlc.tracker.fresh.model.msg.ErrorMessage
import com.cartlc.tracker.fresh.model.msg.StringMessage

class StageEquipment(
        shared: MainController.Shared
) : ProcessBase(shared) {

    fun process(flow: Flow) {
        with(shared) {
            when (flow.stage) {
                Stage.EQUIPMENT -> {
                    titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_equipment_installed)
                    mainListUseCase.visible = true
                    buttonsController.centerVisible = true
                }
                Stage.ADD_EQUIPMENT -> {
                    titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_equipment)
                    entrySimpleUseCase.showing = true
                    entrySimpleUseCase.hintValue = messageHandler.getString(StringMessage.title_equipment)
                    entrySimpleUseCase.simpleTextClear()
                }
            }
        }
    }

    fun saveAdd(): Boolean {
        with(shared) {
            val entryText = entrySimpleUseCase.entryTextValue ?: ""
            if (entryText.isNotEmpty()) {
                val group = prefHelper.currentProjectGroup
                if (group != null) {
                    db.tableCollectionEquipmentProject.addLocal(entryText, group.projectNameId)
                }
                return true
            }
            return false
        }
    }

    fun save(isNext: Boolean): Boolean {
        with (shared) {
            if (isNext) {
                if (db.tableEquipment.countChecked() == 0) {
                    errorValue = ErrorMessage.NEED_EQUIPMENT
                    return false
                }
            }
        }
        return true
    }
}
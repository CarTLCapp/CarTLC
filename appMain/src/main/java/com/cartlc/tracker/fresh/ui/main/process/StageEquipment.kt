package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.flow.Flow
import com.cartlc.tracker.fresh.model.flow.Stage
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
                    buttonsUseCase.centerVisible = true
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
}
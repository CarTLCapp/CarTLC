package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.model.event.Button
import com.cartlc.tracker.model.flow.RootProjectFlow
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.msg.StringMessage

open class ProcessBase(
        protected val shared: MainController.Shared
) {

    protected fun setList(msg: StringMessage, key: String, list: List<String>) {

        with(shared) {
            titleUseCase.mainTitleText = messageHandler.getString(msg)
            mainListUseCase.key = key
            if (list.isEmpty()) {
                onEmptyList()
            } else {
                mainListUseCase.visible = true
                mainListUseCase.simpleItems = list
            }
        }
    }

    private fun onEmptyList() {
        with(shared) {
            when (curFlowValue.stage) {
                Stage.ROOT_PROJECT -> {
                    prefHelper.reloadProjects()
                    serviceUseCase.ping()
                }
                Stage.SUB_PROJECT -> curFlowValue = RootProjectFlow()
                Stage.STREET,
                Stage.CITY,
                Stage.EQUIPMENT,
                Stage.STATE -> buttonsUseCase.dispatch(Button.BTN_CENTER)
                else -> {
                }
            }
        }
    }
}
package com.cartlc.tracker.ui.app

import com.callassistant.common.rx.SchedulerPlan
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.server.DCServerRx
import com.cartlc.tracker.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.ui.bits.entrysimple.EntrySimpleController
import com.cartlc.tracker.ui.bits.entrysimple.EntrySimpleViewMvc
import com.cartlc.tracker.ui.stage.StageHook
import com.cartlc.tracker.ui.stage.buttons.ButtonsController
import com.cartlc.tracker.ui.stage.buttons.ButtonsViewMvc
import com.cartlc.tracker.ui.stage.login.LoginController
import com.cartlc.tracker.ui.stage.login.LoginViewMvc

class FactoryController(
        private val prefHelper: PrefHelper,
        private val dcRx: DCServerRx,
        private val schedulerPlan: SchedulerPlan
) {

    fun allocEntrySimpleController(boundAct: BoundAct, view: EntrySimpleViewMvc): EntrySimpleController {
        return EntrySimpleController(boundAct, view)
    }

    fun allocLoginController(
            boundFrag: BoundFrag,
            view: LoginViewMvc,
            stageListener: StageHook
    ): LoginController {
        return LoginController(
                boundFrag, view,
                stageListener, dcRx, schedulerPlan)
    }

    fun allocButtonsController(boundAct: BoundAct,
                               viewMvc: ButtonsViewMvc
    ): ButtonsController {
        return ButtonsController(boundAct, viewMvc, prefHelper)
    }

}
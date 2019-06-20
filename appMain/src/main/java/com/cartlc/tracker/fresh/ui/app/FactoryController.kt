package com.cartlc.tracker.fresh.ui.app

import com.callassistant.common.rx.SchedulerPlan
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.server.DCServerRx
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleController
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleViewMvc
import com.cartlc.tracker.ui.stage.StageHook
import com.cartlc.tracker.fresh.ui.buttons.ButtonsController
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvc
import com.cartlc.tracker.fresh.ui.login.LoginController
import com.cartlc.tracker.fresh.ui.login.LoginViewMvc
import com.cartlc.tracker.fresh.ui.title.TitleController
import com.cartlc.tracker.fresh.ui.title.TitleViewMvc

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
        return ButtonsController(boundAct, viewMvc)
    }

    fun allocTitleController(boundAct: BoundAct,
                             viewMvc: TitleViewMvc): TitleController {
        return TitleController(boundAct, viewMvc)
    }

}
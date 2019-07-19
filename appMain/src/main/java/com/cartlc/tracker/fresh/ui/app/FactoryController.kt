/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.app

import com.cartlc.tracker.fresh.model.SchedulerPlan
import com.cartlc.tracker.fresh.service.endpoint.DCServerRx
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleController
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleViewMvc
import com.cartlc.tracker.fresh.ui.buttons.ButtonsController
import com.cartlc.tracker.fresh.ui.buttons.ButtonsUseCase
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvc
import com.cartlc.tracker.fresh.ui.confirm.ConfirmController
import com.cartlc.tracker.fresh.ui.confirm.ConfirmViewMvc
import com.cartlc.tracker.fresh.ui.login.LoginController
import com.cartlc.tracker.fresh.ui.login.LoginViewMvc
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.ui.main.MainViewMvc
import com.cartlc.tracker.fresh.ui.mainlist.MainListController
import com.cartlc.tracker.fresh.ui.mainlist.MainListViewMvc
import com.cartlc.tracker.fresh.ui.picture.PictureListController
import com.cartlc.tracker.fresh.ui.picture.PictureListViewMvc
import com.cartlc.tracker.fresh.ui.title.TitleController
import com.cartlc.tracker.fresh.ui.title.TitleViewMvc

class FactoryController(
        private val dcRx: DCServerRx,
        private val schedulerPlan: SchedulerPlan
) {

    fun allocEntrySimpleController(boundAct: BoundAct, view: EntrySimpleViewMvc): EntrySimpleController {
        return EntrySimpleController(boundAct, view)
    }

    fun allocLoginController(
            boundFrag: BoundFrag,
            view: LoginViewMvc,
            butttonsUseCase: ButtonsUseCase
    ): LoginController {
        return LoginController(
                boundFrag, view,
                butttonsUseCase, dcRx, schedulerPlan)
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

    fun allocConfirmController(boundFrag: BoundFrag,
                               viewMvc: ConfirmViewMvc): ConfirmController {
        return ConfirmController(boundFrag, viewMvc)
    }

    fun allocMainListController(boundAct: BoundAct,
                                viewMvc: MainListViewMvc): MainListController {
        return MainListController(boundAct, viewMvc)
    }

    fun allocPictureListController(boundAct: BoundAct,
                               viewMvc: PictureListViewMvc): PictureListController {
        return PictureListController(boundAct, viewMvc)
    }

    fun allocMainController(boundAct: BoundAct, viewMvc: MainViewMvc): MainController {
        return MainController(boundAct, viewMvc)
    }

}
/*
 * Copyright 2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.app.factory

import com.cartlc.tracker.fresh.model.SchedulerPlan
import com.cartlc.tracker.fresh.service.endpoint.DCServerRx
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleController
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleViewMvc
import com.cartlc.tracker.fresh.ui.buttons.ButtonsControllerImpl
import com.cartlc.tracker.fresh.ui.buttons.ButtonsController
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvc
import com.cartlc.tracker.fresh.ui.confirm.ConfirmFinalController
import com.cartlc.tracker.fresh.ui.confirm.ConfirmFinalViewMvc
import com.cartlc.tracker.fresh.ui.daar.DaarController
import com.cartlc.tracker.fresh.ui.daar.DaarViewMvc
import com.cartlc.tracker.fresh.ui.listentries.ListEntriesController
import com.cartlc.tracker.fresh.ui.listentries.ListEntriesViewMvc
import com.cartlc.tracker.fresh.ui.login.LoginController
import com.cartlc.tracker.fresh.ui.login.LoginViewMvc
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.ui.main.MainViewMvc
import com.cartlc.tracker.fresh.ui.mainlist.MainListController
import com.cartlc.tracker.fresh.ui.mainlist.MainListViewMvc
import com.cartlc.tracker.fresh.ui.picture.PictureListController
import com.cartlc.tracker.fresh.ui.picture.PictureListViewMvc
import com.cartlc.tracker.fresh.ui.main.title.TitleController
import com.cartlc.tracker.fresh.ui.main.title.TitleControllerImpl
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
            butttonsController: ButtonsController
    ): LoginController {
        return LoginController(
                boundFrag, view,
                butttonsController, dcRx, schedulerPlan)
    }

    fun allocButtonsController(boundAct: BoundAct,
                               viewMvc: ButtonsViewMvc
    ): ButtonsController {
        return ButtonsControllerImpl(boundAct, viewMvc)
    }

    fun allocTitleController(boundAct: BoundAct,
                             viewMvc: TitleViewMvc): TitleController {
        return TitleControllerImpl(boundAct, viewMvc)
    }

    fun allocConfirmController(boundFrag: BoundFrag,
                               viewMvc: ConfirmFinalViewMvc): ConfirmFinalController {
        return ConfirmFinalController(boundFrag, viewMvc)
    }

    fun allocMainListController(boundAct: BoundAct,
                                viewMvc: MainListViewMvc): MainListController {
        return MainListController(boundAct, viewMvc)
    }

    fun allocPictureListController(boundAct: BoundAct,
                               viewMvc: PictureListViewMvc): PictureListController {
        return PictureListController(boundAct, viewMvc)
    }

    fun allocMainController(boundAct: BoundAct, viewMvc: MainViewMvc,
                            titleController: TitleController,
                            buttonsController: ButtonsController
    ): MainController {
        return MainController(boundAct, viewMvc, titleController, buttonsController)
    }

    fun allocListEntriesController(boundAct: BoundAct, viewMvc: ListEntriesViewMvc): ListEntriesController {
        return ListEntriesController(boundAct, viewMvc)
    }

    fun allocDaarController(boundAct: BoundAct, daarViewMvc: DaarViewMvc, titleViewMvc: TitleViewMvc, buttonsViewMvc: ButtonsViewMvc): DaarController {
        return DaarController(
                boundAct,
                daarViewMvc,
                titleViewMvc,
                buttonsViewMvc,
                boundAct.repo.db)
    }
}

package com.cartlc.tracker.ui.app

import com.cartlc.tracker.ui.act.MainActivity
import com.cartlc.tracker.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.ui.bits.entrysimple.EntrySimpleController
import com.cartlc.tracker.ui.bits.entrysimple.EntrySimpleViewMvc
import com.cartlc.tracker.ui.stage.StageHook
import com.cartlc.tracker.ui.stage.buttons.ButtonsController
import com.cartlc.tracker.ui.stage.buttons.ButtonsViewMvc
import com.cartlc.tracker.ui.stage.buttons.MainButtonsController
import com.cartlc.tracker.ui.stage.login.LoginController
import com.cartlc.tracker.ui.stage.login.LoginViewMvc

class FactoryController {

    fun allocEntrySimpleController(boundAct: BoundAct, view: EntrySimpleViewMvc): EntrySimpleController {
        return EntrySimpleController(boundAct, view)
    }

    fun allocLoginController(
            boundFrag: BoundFrag,
            view: LoginViewMvc,
            stageListener: StageHook
    ): LoginController {
        return LoginController(boundFrag, view, stageListener)
    }

    fun allocButtonsController(boundFrag: BoundFrag,
                               viewMvc: ButtonsViewMvc,
                               stageHook: StageHook
    ): ButtonsController {
        return if (boundFrag.activity is MainActivity) {
            MainButtonsController(boundFrag, viewMvc)
        } else {
            ButtonsController(boundFrag, viewMvc)
        }
    }

}
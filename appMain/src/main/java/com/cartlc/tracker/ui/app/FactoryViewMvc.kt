package com.cartlc.tracker.ui.app

import android.view.LayoutInflater
import android.view.ViewGroup
import com.cartlc.tracker.ui.bits.entrysimple.EntrySimpleViewMvc
import com.cartlc.tracker.ui.bits.entrysimple.EntrySimpleViewMvcImpl
import com.cartlc.tracker.ui.stage.buttons.ButtonsViewMvc
import com.cartlc.tracker.ui.stage.buttons.ButtonsViewMvcImpl
import com.cartlc.tracker.ui.stage.login.LoginViewMvc
import com.cartlc.tracker.ui.stage.login.LoginViewMvcImpl

class FactoryViewMvc(
        private val inflater: LayoutInflater
) {
    private fun getInflater(container: ViewGroup?): LayoutInflater {
        container?.let {
            return LayoutInflater.from(it.context)
        } ?: return inflater
    }

    fun allocEntrySimpleViewMvc(container: ViewGroup?): EntrySimpleViewMvc {
        return EntrySimpleViewMvcImpl(getInflater(container), container)
    }

    fun allocLoginViewMvc(container: ViewGroup?): LoginViewMvc {
        return LoginViewMvcImpl(getInflater(container), container);
    }

    fun allocButtonsViewMvc(container: ViewGroup?): ButtonsViewMvc {
        return ButtonsViewMvcImpl(getInflater(container), container)
    }

}
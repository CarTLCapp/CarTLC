package com.cartlc.tracker.fresh.ui.app

import android.view.LayoutInflater
import android.view.ViewGroup
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleViewMvc
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleViewMvcImpl
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvc
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvcImpl
import com.cartlc.tracker.fresh.ui.confirm.ConfirmViewMvc
import com.cartlc.tracker.fresh.ui.confirm.ConfirmViewMvcImpl
import com.cartlc.tracker.fresh.ui.login.LoginViewMvc
import com.cartlc.tracker.fresh.ui.login.LoginViewMvcImpl
import com.cartlc.tracker.fresh.ui.title.TitleViewMvc
import com.cartlc.tracker.fresh.ui.title.TitleViewMvcImpl

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

    fun allocTitleViewMvc(container: ViewGroup?): TitleViewMvc {
        return TitleViewMvcImpl(getInflater(container), container)
    }

    fun allocConfirmViewMvc(container: ViewGroup?): ConfirmViewMvc {
        return ConfirmViewMvcImpl(getInflater(container), container)
    }

}
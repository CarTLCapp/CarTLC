package com.cartlc.tracker.fresh.ui.app

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleViewMvc
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleViewMvcImpl
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvc
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvcImpl
import com.cartlc.tracker.fresh.ui.confirm.ConfirmViewMvc
import com.cartlc.tracker.fresh.ui.confirm.ConfirmViewMvcImpl
import com.cartlc.tracker.fresh.ui.login.LoginViewMvc
import com.cartlc.tracker.fresh.ui.login.LoginViewMvcImpl
import com.cartlc.tracker.fresh.ui.mainlist.MainListViewMvc
import com.cartlc.tracker.fresh.ui.mainlist.MainListViewMvcImpl
import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.*
import com.cartlc.tracker.fresh.ui.picture.PictureListViewMvc
import com.cartlc.tracker.fresh.ui.picture.PictureListViewMvcImpl
import com.cartlc.tracker.fresh.ui.picture.item.PictureListItemViewMvc
import com.cartlc.tracker.fresh.ui.picture.item.PictureListItemViewMvcImpl
import com.cartlc.tracker.fresh.ui.picture.item.PictureListThumbnailItemViewMvc
import com.cartlc.tracker.fresh.ui.picture.item.PictureListThumbnailItemViewMvcImpl
import com.cartlc.tracker.fresh.ui.title.TitleViewMvc
import com.cartlc.tracker.fresh.ui.title.TitleViewMvcImpl
import com.cartlc.tracker.model.msg.MessageHandler

class FactoryViewMvc(
        private val inflater: LayoutInflater,
        private val factoryAdapterController: FactoryAdapterController
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

    fun allocConfirmViewMvc(context: Context): ConfirmViewMvc {
        return ConfirmViewMvcImpl(LayoutInflater.from(context), null, this)
    }

    fun allocMainListViewMvc(container: ViewGroup?): MainListViewMvc {
        return MainListViewMvcImpl(getInflater(container), container, this, factoryAdapterController)
    }

    fun allocSimpleListItemViewMvc(container: ViewGroup?, @LayoutRes entryItemLayoutId: Int): SimpleItemViewMvc {
        return SimpleItemViewMvcImpl(getInflater(container), container, entryItemLayoutId)
    }

    fun allocProjectGroupItemViewMvc(container: ViewGroup?): ProjectGroupItemViewMvc {
        return ProjectGroupItemViewMvcImpl(getInflater(container), container)
    }

    fun allocEquipmentSelectItemViewMvc(container: ViewGroup?): EquipmentSelectItemViewMvc {
        return EquipmentSelectItemViewMvcImpl(getInflater(container), container)
    }

    fun allocRadioListItemViewMvc(container: ViewGroup?): RadioListItemViewMvc {
        return RadioListItemViewMvcImpl(getInflater(container), container)
    }

    fun allocNoteListEntryItemViewMvc(container: ViewGroup?): NoteListEntryItemViewMvc {
        return NoteListEntryItemViewMvcImpl(getInflater(container), container)
    }

    fun allocPictureListViewMvc(container: ViewGroup?): PictureListViewMvc {
        return PictureListViewMvcImpl(getInflater(container), container, this)
    }

    fun allocPictureListItemViewMvc(container: ViewGroup?): PictureListItemViewMvc {
        return PictureListItemViewMvcImpl(getInflater(container), container)
    }

    fun allocPictureListThumbnailItemViewMvc(container: ViewGroup?): PictureListThumbnailItemViewMvc {
        return PictureListThumbnailItemViewMvcImpl(getInflater(container), container)
    }

}
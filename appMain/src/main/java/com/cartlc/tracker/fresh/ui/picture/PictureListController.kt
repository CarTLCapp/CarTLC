/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import android.os.Handler
import android.os.Message
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.model.core.table.TableNote
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.common.observable.BaseObservableImpl
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.ui.picture.item.*
import java.lang.ref.WeakReference

class PictureListController(
        boundAct: BoundAct,
        private val viewMvc: PictureListViewMvc
) : BaseObservableImpl<PictureListUseCase.Listener>(),
        PictureListViewMvc.Listener,
        PictureListUseCase,
        PictureNoteItemViewMvc.Listener {

    companion object {
        private const val MSG_REMOVE_ITEM = 1
        private const val DELAY_REMOVE_ITEM = 100
    }

    private class MyHandler(other: PictureListController) : Handler() {

        private val obj = WeakReference(other)

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REMOVE_ITEM -> if (msg.obj is DataPicture) {
                    val item = msg.obj as DataPicture
                    obj.get()?.itemRemove(item)
                }
            }
        }
    }

    private val messageHandler = boundAct.componentRoot.messageHandler
    private val bitmapHelper = boundAct.componentRoot.bitmapHelper
    private var handler = MyHandler(this)
    private var pictures: MutableList<DataPicture> = mutableListOf()
    private val repo = boundAct.repo
    private val prefHelper = repo.prefHelper
    private val showButtonMinImageSize = boundAct.act.resources.getDimension(R.dimen.image_show_button_min_size).toInt()

    init {
        viewMvc.listener = this
    }

    // region PictureListUseCase

    private var notes = listOf<DataNote>()

    override var pictureItems: List<DataPicture>
        get() = pictures
        set(value) {
            pictures = value.toMutableList()
            viewMvc.onPictureRefreshNeeded()
        }

    override var pictureNotes: List<DataNote>
        get() = notes
        set(value) {
            notes = value
            viewMvc.onPictureRefreshNeeded()
        }

    override fun onPictureRefreshNeeded() {
        viewMvc.onPictureRefreshNeeded()
    }

    override val notesReady: Boolean
        get() {
            for (note in notes) {
                if (note.value.isNullOrBlank()) {
                    return false
                }
            }
            return true
        }

    override fun clearCache() {
        bitmapHelper.clearCache()
    }

//    override fun clearUnmarkedCache() {
//        bitmapHelper.clearUnmarkedCache()
//    }
//
//    override fun clearMarks() {
//        bitmapHelper.clearMarks()
//    }

    // endregion PictureListUseCase

    // region PictureListAdapter.Listener

    override val pictureCount: Int
        get() = pictures.size

    override val noteCount: Int
        get() = notes.size

    override fun onBindViewHolder(itemViewMvc: ViewMvc, position: Int) {
        if (itemViewMvc is PictureListItemViewMvc) {
            onBindPictureViewHolder(itemViewMvc, position)
        } else if (itemViewMvc is PictureNoteItemViewMvc) {
            itemViewMvc.bind(notes[position], this)
        }
    }

    private fun onBindPictureViewHolder(itemViewMvc: PictureListItemViewMvc, position: Int) {
        val item = pictures[position]
        val pictureFile = item.file
        itemViewMvc.buttonsVisible = false
        itemViewMvc.bindPicture(pictureFile)

        if (pictureFile == null || !pictureFile.exists()) {
            val msg = Message()
            msg.what = MSG_REMOVE_ITEM
            msg.obj = item
            handler.sendMessageDelayed(msg, DELAY_REMOVE_ITEM.toLong())
            itemViewMvc.loading = messageHandler.getString(StringMessage.error_picture_removed)
        } else {
            itemViewMvc.loading = null
            itemViewMvc.bindListener(object : PictureListItemViewMvc.Listener {
                override fun onRemoveClicked() {
                    itemRemove(item)
                }

                override fun onCwClicked() {
                    incAutoRotate(item.rotateCW())
                    viewMvc.onPictureRefreshNeeded()
                }

                override fun onCcwClicked() {
                    incAutoRotate(item.rotateCCW())
                    viewMvc.onPictureRefreshNeeded()
                }

                override fun onImageLoaded(imageHeight: Int) {
                    itemViewMvc.buttonsVisible = imageHeight > showButtonMinImageSize
                }
            })
        }
    }

    // endregion PictureListAdapter.Listener

    // region PictureListItemNoteViewMvc.Listener

    override fun onNoteValueChanged(note: DataNote) {
        repo.db.tableNote.updateValue(note)
        for (listener in listeners) {
            listener.onPictureNoteChanged(note)
        }
    }

    override fun getHint(note: DataNote): String? {
        return when (note.name) {
            TableNote.NOTE_TRUCK_DAMAGE_NAME -> messageHandler.getString(StringMessage.truck_damage_hint)
            TableNote.NOTE_TRUCK_NUMBER_NAME -> messageHandler.getString(StringMessage.truck_number_hint)
            else -> null
        }
    }

    // endregion PictureListItemNoteViewMvc.Listener

    private fun itemRemove(item: DataPicture) {
        item.remove()
        pictures.remove(item)
        viewMvc.onPictureRefreshNeeded()
        repo.db.tablePicture.removeFileDoesNotExist(listOf(item))
        for (listener in listeners) {
            listener.onPictureRemoveDone(pictures.size)
        }
    }

    private fun incAutoRotate(degrees: Int) {
        prefHelper.autoRotatePicture = prefHelper.autoRotatePicture + degrees
    }
}
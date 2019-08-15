/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import android.os.Handler
import android.os.Message
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.model.core.sql.SqlTableNote
import com.cartlc.tracker.fresh.model.core.table.TableNote
import com.cartlc.tracker.fresh.model.flow.Stage
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.common.observable.BaseObservableImpl
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc
import com.cartlc.tracker.fresh.ui.picture.item.PictureListItemViewMvc
import com.cartlc.tracker.fresh.ui.picture.item.PictureListThumbnailItemViewMvc
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.ui.picture.item.PictureNoteItemViewMvc
import com.cartlc.tracker.fresh.ui.picture.item.PictureNoteThumbnailItemViewMvc
import java.io.File
import java.lang.ref.WeakReference
import java.util.HashMap

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
    private var handler = MyHandler(this)
    private var rotationMap: HashMap<String, Int> = HashMap()
    private var pictures: MutableList<DataPicture> = mutableListOf()
    private val repo = boundAct.repo

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

    override var isThumbnail: Boolean = false

    override val commonRotation: Int
        get() {
            var commonRotation = 0
            for (picture in pictures) {
                val path = picture.unscaledFile.absolutePath
                if (!rotationMap.containsKey(path)) {
                    return 0
                }
                val rotation = rotationMap[path]
                if (commonRotation == 0 && rotation != null) {
                    commonRotation = rotation
                } else if (commonRotation != rotation) {
                    return 0
                }
            }
            return commonRotation
        }

    override val hadSomeRotations: Boolean
        get() {
            for (key in rotationMap.keys) {
                if (rotationMap[key] != 0) {
                    return true
                }
            }
            return false
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

    // endregion PicturListUseCase

    // region PictureListAdapter.Listener

    override val pictureCount: Int
        get() = pictures.size

    override val noteCount: Int
        get() = notes.size

    override fun onBindViewHolder(itemViewMvc: ViewMvc, position: Int) {
        if (itemViewMvc is PictureListThumbnailItemViewMvc) {
            onBindPictureViewHolder(itemViewMvc, position)
        } else if (itemViewMvc is PictureNoteThumbnailItemViewMvc) {
            val note = notes[position]
            itemViewMvc.noteLabel = note.name
            itemViewMvc.noteValue = note.value
        } else if (itemViewMvc is PictureNoteItemViewMvc) {
            itemViewMvc.bind(notes[position], this)
        }
    }

    private fun onBindPictureViewHolder(itemViewMvc: PictureListThumbnailItemViewMvc, position: Int) {
        val itemViewMvcRegular = itemViewMvc as? PictureListItemViewMvc
        val item = pictures[position]
        val pictureFile: File?
        when {
            item.existsUnscaled -> pictureFile = item.unscaledFile
            item.existsScaled -> pictureFile = item.scaledFile
            else -> pictureFile = null
        }
        itemViewMvc.bindPicture(pictureFile)

        if (pictureFile == null || !pictureFile.exists()) {
            val msg = Message()
            msg.what = MSG_REMOVE_ITEM
            msg.obj = item
            handler.sendMessageDelayed(msg, DELAY_REMOVE_ITEM.toLong())
            itemViewMvc.loading = messageHandler.getString(StringMessage.error_picture_removed)
        } else {
            itemViewMvc.loading = null
            itemViewMvcRegular?.bindListener(object : PictureListItemViewMvc.Listener {
                override fun onRemoveClicked() {
                    itemRemove(item)
                }

                override fun onCwClicked() {
                    incRotation(item, item.rotateCW())
                    viewMvc.onPictureRefreshNeeded()
                }

                override fun onCcwClicked() {
                    incRotation(item, item.rotateCCW())
                    viewMvc.onPictureRefreshNeeded()
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

    private fun incRotation(item: DataPicture, degrees: Int) {
        val file = item.unscaledFile
        val path = file.absolutePath
        if (rotationMap.containsKey(path)) {
            rotationMap[path] = (rotationMap[path]!! + degrees) % 360
        } else {
            rotationMap[path] = degrees
        }
    }
}
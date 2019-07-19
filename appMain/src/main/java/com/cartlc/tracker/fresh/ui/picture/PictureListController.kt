/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import android.os.Handler
import android.os.Message
import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.common.observable.BaseObservableImpl
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc
import com.cartlc.tracker.fresh.ui.picture.item.PictureListItemViewMvc
import com.cartlc.tracker.fresh.ui.picture.item.PictureListThumbnailItemViewMvc
import com.cartlc.tracker.fresh.model.msg.StringMessage
import java.io.File
import java.lang.ref.WeakReference
import java.util.HashMap

class PictureListController(
        boundAct: BoundAct,
        private val viewMvc: PictureListViewMvc
) : BaseObservableImpl<PictureListUseCase.Listener>(),
        PictureListViewMvc.Listener,
        PictureListUseCase {

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
    private val dialogNavigator = boundAct.dialogNavigator
    private var handler = MyHandler(this)
    private var rotationMap: HashMap<String, Int> = HashMap()
    private var items: MutableList<DataPicture> = mutableListOf()

    init {
        viewMvc.listener = this
    }

    // region PictureListUseCase

    override var pictureItems: List<DataPicture>
        get() {
            return items
        }
        set(value) {
            items = value.toMutableList()
            viewMvc.onPictureRefreshNeeded()
        }

    override var isThumbnail: Boolean = false

    override val commonRotation: Int
        get() {
            var commonRotation = 0
            for (picture in items) {
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

    // endregion PicturListUseCase

    // region PictureListAdapter.Listener

    override val itemCount: Int
        get() = items.size

    override fun onBindViewHolder(itemViewMvc: ViewMvc, position: Int) {
        val itemViewMvcRegular = itemViewMvc as? PictureListItemViewMvc
        val itemViewMvcThumbnail = itemViewMvc as PictureListThumbnailItemViewMvc
        val item = items[position]
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
            itemViewMvcRegular?.bind(object : PictureListItemViewMvc.Listener {
                override fun onRemoveClicked() {
                    item.remove()
                    items.remove(item)
                    viewMvc.onPictureRefreshNeeded()
                    for (listener in listeners) {
                        listener.onPictureRemoveDone(items.size)
                    }
                }

                override fun onCwClicked() {
                    incRotation(item, item.rotateCW())
                    viewMvc.onPictureRefreshNeeded()
                }

                override fun onCcwClicked() {
                    incRotation(item, item.rotateCCW())
                    viewMvc.onPictureRefreshNeeded()
                }

                override fun onNoteDialogClicked() {
                    dialogNavigator.showPictureNoteDialog(item) {
                        viewMvc.onPictureRefreshNeeded()
                        for (listener in listeners) {
                            listener.onPictureNoteAdded(item)
                        }
                    }
                }
            })
            itemViewMvc.note = item.note
        }
    }

    // endregion PictureListAdapter.Listener

    private fun itemRemove(item: DataPicture) {
        item.remove()
        items.remove(item)
        viewMvc.onPictureRefreshNeeded()
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
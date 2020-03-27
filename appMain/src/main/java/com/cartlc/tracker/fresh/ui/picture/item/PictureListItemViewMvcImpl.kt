/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl
import com.cartlc.tracker.ui.util.helper.BitmapHelper
import timber.log.Timber
import java.io.File

class PictureListItemViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ViewMvcImpl(), PictureListItemViewMvc {

    override val rootView: View = inflater.inflate(R.layout.picture_list_item, container, false)

    private val pictureView = findViewById<ImageView>(R.id.picture)
    private val loadingView = findViewById<TextView>(R.id.loading)
    private val removeButton = findViewById<ImageView>(R.id.remove)
    private val rotateCwButton = findViewById<ImageView>(R.id.rotate_cw)
    private val rotateCcwButton = findViewById<ImageView>(R.id.rotate_ccw)

    private var listener: PictureListItemViewMvc.Listener? = null
    private var dstPictureFile: File? = null
    private val loadedHeight = context.resources.getDimension(R.dimen.image_full_max_height).toInt()

    private val app = context.applicationContext as TBApplication
    private val bitmapHelper = app.componentRoot.bitmapHelper

    override var btnRemoveVisible: Boolean
        get() = removeButton.visibility == View.VISIBLE
        set(value) { removeButton.visibility = if (value) View.VISIBLE else View.GONE }

    override var btnCcwVisible: Boolean
        get() = rotateCcwButton.visibility == View.VISIBLE
        set(value) { rotateCcwButton.visibility = if (value) View.VISIBLE else View.GONE }

    override var btnCwVisible: Boolean
        get() = rotateCwButton.visibility == View.VISIBLE
        set(value) { rotateCwButton.visibility = if (value) View.VISIBLE else View.GONE }


    init {
        pictureView.addOnLayoutChangeListener { v: View, left: Int, top: Int, right: Int, bottom: Int,
                                                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
            onPictureViewSizeChanged(right - left, bottom - top)
        }
    }

    private fun onPictureViewSizeChanged(width: Int, height: Int) {
        listener?.onImageLoaded(pictureView.height)
    }

    // region PictureListItemViewMvc

    override var loading: String?
        get() = loadingView.text.toString()
        set(value) {
            loadingView.text = value
            loadingView.visibility = if (value == null) View.GONE else View.VISIBLE
        }

    override fun bindListener(listener: PictureListItemViewMvc.Listener) {
        removeButton.setOnClickListener { listener.onRemoveClicked() }
        rotateCwButton.setOnClickListener { listener.onCwClicked() }
        rotateCcwButton.setOnClickListener { listener.onCcwClicked() }
        this.listener = listener
    }

    override fun bindPicture(pictureFile: File?) {
        pictureView.setImageResource(android.R.color.transparent)
        dstPictureFile = pictureFile
        dstPictureFile?.let {
            if (it.exists()) {
                bitmapHelper.loadBitmap(it.absolutePath, loadedHeight, pictureView, false) {
                    listener?.onImageLoaded(pictureView.height)
                }
            }
        }
    }

    // endregion PictureListItemViewMvc

}
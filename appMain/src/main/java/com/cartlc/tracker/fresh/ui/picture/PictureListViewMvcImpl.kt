/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.callassistant.util.viewmvc.ObservableViewMvcImpl
import com.callassistant.util.viewmvc.ViewMvc
import com.callassistant.util.viewmvc.ViewMvcImpl
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.app.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.picture.item.PictureListItemViewMvc
import com.cartlc.tracker.ui.bits.AutoLinearLayoutManager

class PictureListViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?,
        factoryViewMvc: FactoryViewMvc
) : ViewMvcImpl(),
        PictureListViewMvc,
        PictureListAdapter.Listener
{

    override val rootView: View = inflater.inflate(R.layout.frame_picture_list, container, false) as ViewGroup

    private val pictureList = findViewById<RecyclerView>(R.id.list_pictures)
    private val pictureListAdapter = PictureListAdapter(factoryViewMvc, this)

    override var listener: PictureListViewMvc.Listener? = null

    init {
        val linearLayoutManager = AutoLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        pictureList.layoutManager = linearLayoutManager
        pictureList.adapter = pictureListAdapter
    }

    override fun onPictureRefreshNeeded() {
        pictureListAdapter.notifyDataSetChanged()
    }

    // region PictureListAdapter.Listener

    override val isThumbnail: Boolean
        get() = listener?.isThumbnail ?: false

    override val itemCount: Int
        get() = listener?.itemCount ?: 0

    override fun onBindViewHolder(itemViewMvc: ViewMvc, position: Int) {
        listener?.onBindViewHolder(itemViewMvc, position)
    }

    // endregion PictureListAdapter.Listener
}
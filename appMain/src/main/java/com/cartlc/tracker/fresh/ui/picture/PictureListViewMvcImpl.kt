/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl

class PictureListViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?,
        factoryViewMvc: FactoryViewMvc
) : ViewMvcImpl(),
        PictureListViewMvc,
        PictureListAdapter.Listener,
        PictureNoteAdapter.Listener {

    override val rootView: View = inflater.inflate(R.layout.frame_picture_list, container, false) as ViewGroup

    private val pictureList = findViewById<RecyclerView>(R.id.list_pictures)
    private val pictureListAdapter = PictureListAdapter(factoryViewMvc, this)
    private val noteList = findViewById<RecyclerView>(R.id.list_picture_notes)
    private val noteListAdapter = PictureNoteAdapter(factoryViewMvc, this)

    override var listener: PictureListViewMvc.Listener? = null

    init {
        noteList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        noteList.adapter = noteListAdapter
        pictureList.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        pictureList.adapter = pictureListAdapter
    }

    override fun onPictureRefreshNeeded() {
        pictureListAdapter.notifyDataSetChanged()
        noteListAdapter.notifyDataSetChanged()
    }

    // region PictureListAdapter.Listener & PictureListItemNoteAdapter.Listener

    override fun onBindViewHolder(itemViewMvc: ViewMvc, position: Int) {
        listener?.onBindViewHolder(itemViewMvc, position)
    }

    // endregion PictureListAdapter.Listener & PictureListItemNoteAdapter.Listener

    // region PictureListAdapter.Listener

    override val pictureCount: Int
        get() = listener?.pictureCount ?: 0

    // endregion PictureListAdapter.Listener

    // region PictureListItemNoteAdapter.Listener

    override val noteCount: Int
        get() = listener?.noteCount ?: 0

    // endregion PictureListItemNoteAdapter.Listener

}
/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvc
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl
import com.cartlc.tracker.fresh.ui.bits.AutoLinearLayoutManager

class PictureListViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?,
        factoryViewMvc: FactoryViewMvc
) : ViewMvcImpl(),
        PictureListViewMvc,
        PictureListAdapter.Listener,
        PictureNoteAdapter.Listener
{

    override val rootView: View = inflater.inflate(R.layout.frame_picture_list, container, false) as ViewGroup

    private val pictureList = findViewById<RecyclerView>(R.id.list_pictures)
    private val pictureListAdapter = PictureListAdapter(factoryViewMvc, this)
    private val noteList = findViewById<RecyclerView>(R.id.list_picture_notes)
    private val noteListAdapter = PictureNoteAdapter(factoryViewMvc, this)

    override var listener: PictureListViewMvc.Listener? = null

    init {
        val linearLayoutManager = AutoLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        noteList.layoutManager = linearLayoutManager
        noteList.adapter = noteListAdapter
        layout()
    }

    override fun onPictureRefreshNeeded() {
        pictureListAdapter.notifyDataSetChanged()
        noteListAdapter.notifyDataSetChanged()
        layout()
    }

    private fun layout() {
        if (listener?.isThumbnail == true) {
            pictureList.layoutManager = AutoLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            pictureList.adapter = pictureListAdapter
            val params = pictureList.layoutParams as ConstraintLayout.LayoutParams
            params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
            rootView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            pictureList.layoutManager = AutoLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            pictureList.adapter = pictureListAdapter
            val params = pictureList.layoutParams as ConstraintLayout.LayoutParams
            params.height = 0
            rootView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    // region PictureListAdapter.Listener & PictureListItemNoteAdapter.Listener

    override val isThumbnail: Boolean
        get() = listener?.isThumbnail ?: false

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
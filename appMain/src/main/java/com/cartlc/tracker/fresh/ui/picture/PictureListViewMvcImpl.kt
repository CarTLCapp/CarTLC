/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    companion object {
        // TODO: Would like a more universal way of handling this:
        const val NUM_COLUMNS = 4
    }
    override val rootView: View = inflater.inflate(R.layout.frame_picture_list, container, false) as ViewGroup

    private val pictureList = findViewById<RecyclerView>(R.id.list_pictures)
    private val pictureListAdapter = PictureListAdapter(factoryViewMvc, this)
    private val noteList = findViewById<RecyclerView>(R.id.list_picture_notes)
    private val noteListAdapter = PictureNoteAdapter(factoryViewMvc, this)

    override var listener: PictureListViewMvc.Listener? = null

    private var wasThumbnail: Boolean? = null

    init {
        val linearLayoutManager = AutoLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        noteList.layoutManager = linearLayoutManager
        noteList.adapter = noteListAdapter
        pictureList.layoutManager = AutoLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        pictureList.adapter = pictureListAdapter
    }

    override fun onPictureRefreshNeeded() {
        pictureListAdapter.notifyDataSetChanged()
        noteListAdapter.notifyDataSetChanged()
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

    private fun layout() {
        val isThumbnail = listener?.isThumbnail ?: false
        if (wasThumbnail == null || wasThumbnail != isThumbnail) {
            if (isThumbnail) {
                pictureList.layoutManager = GridLayoutManager(context, NUM_COLUMNS)
            } else {
                pictureList.layoutManager = AutoLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            wasThumbnail = isThumbnail
        }
    }
}
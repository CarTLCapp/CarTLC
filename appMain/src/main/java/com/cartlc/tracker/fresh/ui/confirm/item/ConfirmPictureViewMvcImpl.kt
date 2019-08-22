/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.confirm.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.bits.AutoLinearLayoutManager
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl
import com.cartlc.tracker.fresh.ui.confirm.data.ConfirmDataPicture
import com.cartlc.tracker.fresh.ui.confirm.data.NoteLabelValue
import java.io.File

class ConfirmPictureViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?,
        factoryViewMvc: FactoryViewMvc
) : ViewMvcImpl(),
        ConfirmPictureViewMvc {
    override val rootView: View = inflater.inflate(R.layout.confirm_picture, container, false)

    private val confirmPicturesLabel = findViewById<TextView>(R.id.title)
    private val pictureList = findViewById<RecyclerView>(R.id.list_pictures)
    private val pictureListAdapter = ConfirmPictureAdapter(factoryViewMvc)
    private val noteList = findViewById<RecyclerView>(R.id.list_picture_notes)
    private val noteListAdapter = ConfirmPictureNoteAdapter(factoryViewMvc)

    init {
        noteList.layoutManager = AutoLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        noteList.adapter = noteListAdapter
        pictureList.layoutManager = AutoLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        pictureList.adapter = pictureListAdapter
    }

    // region ConfirmPictureViewMvc

    override var data: ConfirmDataPicture
        get() { return ConfirmDataPicture(
                pictureLabel,
                pictureItems,
                pictureNotes
        )}
        set(value) {
            pictureLabel = value.pictureLabel
            pictureItems = value.pictureItems
            pictureNotes = value.pictureNotes
        }

    private var pictureLabel: String
        get() = confirmPicturesLabel.text.toString()
        set(value) {
            confirmPicturesLabel.text = value
        }

    private var pictureItems: List<File>
        get() = pictureListAdapter.items
        set(value) {
            pictureListAdapter.items = value
        }

    private var pictureNotes: List<NoteLabelValue>
        get() = noteListAdapter.items
        set(value) {
            noteListAdapter.items = value
        }

    // endregion ConfirmPictureViewMvc

}
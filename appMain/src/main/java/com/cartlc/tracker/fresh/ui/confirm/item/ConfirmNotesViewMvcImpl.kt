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
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl
import com.cartlc.tracker.fresh.ui.confirm.data.ConfirmDataNotes
import com.cartlc.tracker.ui.list.NoteListAdapter

class ConfirmNotesViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ViewMvcImpl(), ConfirmNotesViewMvc {

    override val rootView: View = inflater.inflate(R.layout.confirm_notes, container, false)

    private val ctx = rootView.context

    private val notesList = findViewById<RecyclerView>(R.id.notes_list)
    private val notesLabel = findViewById<TextView>(R.id.confirm_notes_label)
    private val noteAdapter: NoteListAdapter

    init {
        noteAdapter = NoteListAdapter(ctx)
        notesList.adapter = noteAdapter
        notesList.layoutManager = LinearLayoutManager(ctx)
    }

    // region ConfirmNotesViewMvc

    override var data: ConfirmDataNotes
        get() { return ConfirmDataNotes(notes) }
        set(value) {
            notes = value.notes
        }

    private var notes: List<DataNote>
        get() = TODO("not implemented")
        set(value) {
            noteAdapter.setItems(value)
            notesLabel.visibility = if (value.isEmpty()) View.GONE else View.VISIBLE
        }

    // endregion ConfirmNotesViewMvc
}
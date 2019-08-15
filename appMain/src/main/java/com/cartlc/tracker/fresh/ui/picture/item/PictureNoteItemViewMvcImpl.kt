/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture.item

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl

class PictureNoteItemViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ViewMvcImpl(), PictureNoteItemViewMvc {

    override val rootView: View = inflater.inflate(R.layout.picture_list_item_note, container, false)

    private val noteLabelView = findViewById<TextView>(R.id.label)
    private val noteValueView = findViewById<EditText>(R.id.value)
    private var listener: PictureNoteItemViewMvc.Listener? = null
    private var note: DataNote? = null

    private inner class ItemTextChangedWatcher : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            note?.let { note ->
                val newValue = s.toString().trim { it <= ' ' }
                if (newValue != note.value) {
                    note.value = newValue
                    listener?.onNoteValueChanged(note)
                }
            }
        }
    }

    private var textWatcher = ItemTextChangedWatcher()

    override fun bind(note: DataNote, listener: PictureNoteItemViewMvc.Listener) {
        noteLabelView.text = note.name
        noteValueView.removeTextChangedListener(textWatcher)
        noteValueView.setText(note.value)
        noteValueView.addTextChangedListener(textWatcher)
        this.listener = listener
        this.note = note
        listener.getHint(note)?.let { noteValueView.hint = it }
    }

    override fun clear() {
        noteValueView.removeTextChangedListener(textWatcher)
    }
}
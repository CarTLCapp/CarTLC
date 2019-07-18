package com.cartlc.tracker.fresh.ui.mainlist.adapter.item

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl
import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.NoteListEntryItemViewMvc.Listener

class NoteListEntryItemViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ViewMvcImpl(), NoteListEntryItemViewMvc {

    override val rootView = inflater.inflate(R.layout.entry_item_entry_note, container, false)

    private val labelView = findViewById<TextView>(R.id.label)
    private val entryView = findViewById<EditText>(R.id.entry)

    override var label: String?
        get() = labelView.text.toString()
        set(value) {
            labelView.text = value
        }

    override var entryText: String?
        get() = entryView.text.toString()
        set(value) {
            entryView.setText(value)
        }

    override var isSelected: Boolean
        get() = labelView.isSelected
        set(value) { labelView.isSelected = value }

    override var inputType: Int
        get() = entryView.inputType
        set(value) { entryView.inputType = value }

    override var maxLines: Int
        get() = entryView.maxLines
        set(value) { entryView.maxLines= value }

    override var numLines: Int = 1
        set(value) {
            field = value
            entryView.setLines(value)
        }

// region TextWatcher

    inner class ItemTextChangedWatcher(var listener: Listener) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            listener.afterTextChanged(s.toString().trim { it <= ' ' })
        }
    }

    private var textWatcherForEntry: ItemTextChangedWatcher? = null

    // endregion TextWatcher

    // region NoteListEntryItemViewMvc

    override fun bind(listener: Listener) {
        textWatcherForEntry?.let { entryView.removeTextChangedListener(it) }
        textWatcherForEntry = ItemTextChangedWatcher(listener)
        entryView.addTextChangedListener(textWatcherForEntry)

        entryView.onFocusChangeListener = android.view.View.OnFocusChangeListener { _, hasFocus ->
            listener.onEntryFocused(hasFocus)
        }
    }

    // endregion NoteListEntryItemViewMvc

}
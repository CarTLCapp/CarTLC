/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.list

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.viewmodel.frag.MainListViewModel
import kotlinx.android.synthetic.main.entry_item_entry_note.view.*

import java.util.ArrayList

/**
 * Created by dug on 5/12/17.
 */

class NoteListEntryAdapter(
        ctx: Context,
        private val vm: MainListViewModel,
        private val mListener: EntryListener
) : RecyclerView.Adapter<NoteListEntryAdapter.CustomViewHolder>() {

    private val mLayoutInflater = LayoutInflater.from(ctx)
    private var mItems: MutableList<DataNote> = mutableListOf()

    val notes: List<DataNote>
        get() = mItems

    inner class ItemTextChangedWatcher(var mLabel: TextView, var mItem: DataNote) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            if (mLabel.isSelected) {
                mItem.value = s.toString().trim { it <= ' ' }
                vm.updateNoteValue(mItem)
                mListener.textEntered(mItem)
            }
        }
    }

    inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private var mTextWatcherForEntry: ItemTextChangedWatcher? = null

        fun bind(item: DataNote) {
            with(view) {
                label!!.text = item.name
                entry!!.setText(item.value)
                setTextWatcher(item)
                entry!!.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        mListener.textFocused(item)
                        label!!.isSelected = true
                    } else {
                        label!!.isSelected = false
                    }
                }
                if (item.type === DataNote.Type.ALPHANUMERIC) {
                    entry!!.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                    entry!!.maxLines = 1
                } else if (item.type === DataNote.Type.NUMERIC_WITH_SPACES) {
                    entry!!.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
                    entry!!.maxLines = 1
                } else if (item.type === DataNote.Type.NUMERIC) {
                    entry!!.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
                    entry!!.maxLines = 1
                } else if (item.type === DataNote.Type.MULTILINE) {
                    entry!!.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    entry!!.maxLines = 3
                    entry!!.setLines(3)
                } else {
                    entry!!.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    entry!!.maxLines = 1
                }
            }
        }

        private fun setTextWatcher(item: DataNote) {
            if (mTextWatcherForEntry != null) {
                view.entry!!.removeTextChangedListener(mTextWatcherForEntry)
            }
            mTextWatcherForEntry = ItemTextChangedWatcher(view.label, item)
            view.entry!!.addTextChangedListener(mTextWatcherForEntry)
        }
    }

    interface EntryListener {
        fun textEntered(note: DataNote)
        fun textFocused(note: DataNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = mLayoutInflater.inflate(R.layout.entry_item_entry_note, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(mItems[position])
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun onDataChanged() {
        val currentEditEntry = vm.currentEditEntry
        mItems = if (currentEditEntry != null) {
            currentEditEntry.notesAllWithValuesOverlaid.toMutableList()
        } else {
            vm.queryNotes().toMutableList()
        }
        pushToBottom("Other")
        notifyDataSetChanged()
    }

    private fun pushToBottom(name: String) {
        val others = ArrayList<DataNote>()
        for (item in mItems) {
            if (item.name.startsWith(name)) {
                others.add(item)
                break
            }
        }
        for (item in others) {
            mItems.remove(item)
            mItems.add(item)
        }
    }
}

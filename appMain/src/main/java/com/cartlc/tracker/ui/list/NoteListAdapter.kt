/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.cartlc.tracker.R
import com.cartlc.tracker.model.data.DataNote
import kotlinx.android.synthetic.main.entry_item_note.view.*

/**
 * Created by dug on 5/12/17.
 */

class NoteListAdapter(mContext: Context) : RecyclerView.Adapter<NoteListAdapter.CustomViewHolder>() {
    protected val mLayoutInflater: LayoutInflater
    protected var mItems: List<DataNote> = mutableListOf()

    inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: DataNote) {
            with (view) {
                label!!.text = item.name
                value!!.text = item.value
            }
        }
    }

    init {
        mLayoutInflater = LayoutInflater.from(mContext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = mLayoutInflater.inflate(R.layout.entry_item_note, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(mItems[position])
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun setItems(items: List<DataNote>) {
        mItems = items
        notifyDataSetChanged()
    }

}

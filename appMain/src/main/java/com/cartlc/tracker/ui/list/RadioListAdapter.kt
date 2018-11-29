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
import kotlinx.android.synthetic.main.entry_item_radio.view.*

/**
 * Created by dug on 11/1/18.
 */

class RadioListAdapter(ctx: Context) : RecyclerView.Adapter<RadioListAdapter.CustomViewHolder>() {

    private val layoutInflater: LayoutInflater

    init {
        layoutInflater = LayoutInflater.from(ctx)
    }

    private var lastSelectedPos = -1
    private var lastSelectedText: String? = null

    var selectedPos: Int
        get() = lastSelectedPos
        set(value) {
            lastSelectedPos = value
            notifyDataSetChanged()
        }

    var selectedText: String?
        get() = lastSelectedText
        set(value) {
            selectedPos = list.indexOf(value)
        }

    var list: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var listener: (seletectedPos: Int, selectedText: String) -> Unit = { _, _ -> }

    inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = layoutInflater.inflate(R.layout.entry_item_radio, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val text = list[position]
        holder.view.item.text = text
        holder.view.item.setOnClickListener {onItemSelected(holder.adapterPosition, text) }
        holder.view.item.isChecked = lastSelectedPos == position
    }

    private fun onItemSelected(pos: Int, text: String) {
        lastSelectedPos = pos
        lastSelectedText = text
        listener.invoke(lastSelectedPos, text)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}

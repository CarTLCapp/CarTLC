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
import kotlinx.android.synthetic.main.entry_item_simple.view.*

typealias SimpleListListener = (position: Int, text: String) -> Unit

/**
 * Created by dug on 10/10/18.
 */
class SimpleListAdapter : RecyclerView.Adapter<SimpleListAdapter.CustomViewHolder> {

    private val ctx: Context
    private val layoutInflater: LayoutInflater
    private val entryItemLayoutId: Int
    private var listener: SimpleListListener = { _, _ -> }
    private var selectedOkay = false

    var items: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var selectedPos: Int = -1
        set(value) {
            field = value
            selectedOkay = true
            notifyDataSetChanged()
        }

    inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    constructor(context: Context, listener: SimpleListListener) {
        ctx = context
        this.listener = listener
        entryItemLayoutId = R.layout.entry_item_simple
        layoutInflater = LayoutInflater.from(ctx)
    }

    constructor(context: Context, entryItemLayoutId: Int) {
        ctx = context
        this.entryItemLayoutId = entryItemLayoutId
        layoutInflater = LayoutInflater.from(ctx)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = layoutInflater.inflate(entryItemLayoutId, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val text = items[position]
        holder.view.item.text = text

        if (selectedOkay) {
            if (position == selectedPos) {
                holder.itemView.setBackgroundResource(R.color.list_item_selected)
            } else {
                holder.itemView.setBackgroundResource(android.R.color.transparent)
            }
        }
        holder.view.item.setOnClickListener {
            val pos = holder.adapterPosition
            selectedPos = pos
            listener.invoke(pos, text)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setSelected(value: String): Int {
        selectedPos = items.indexOf(value)
        return selectedPos
    }

    fun setNoneSelected() {
        selectedPos = -1
    }

}

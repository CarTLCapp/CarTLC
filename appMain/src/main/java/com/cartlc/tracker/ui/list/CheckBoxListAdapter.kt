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
import kotlinx.android.synthetic.main.entry_item_check_box.view.*

typealias CheckBoxListListener = (position: Int, text: String, isSelected: Boolean) -> Unit

/**
 * Created by dug on 10/10/18.
 */
class CheckBoxListAdapter : RecyclerView.Adapter<CheckBoxListAdapter.CustomViewHolder> {

    private val ctx: Context
    private val layoutInflater: LayoutInflater
    private var listener: CheckBoxListListener = { _, _, _ -> }

    var items: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var selectedItems: HashSet<Int> = HashSet()

    inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    constructor(context: Context, listener: CheckBoxListListener) {
        ctx = context
        this.listener = listener
        layoutInflater = LayoutInflater.from(ctx)
    }

    constructor(context: Context) {
        ctx = context
        layoutInflater = LayoutInflater.from(ctx)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = layoutInflater.inflate(R.layout.entry_item_check_box, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val text = items[position]
        holder.view.item.text = text

        holder.view.item.setOnClickListener {
            val pos = holder.adapterPosition
            toggle(pos)
            listener.invoke(pos, text, isSelected(pos))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun toggle(value: String): Int {
        return toggle(items.indexOf(value))
    }

    fun toggle(value: Int): Int {
        if (value >= 0 && value < items.size) {
            if (selectedItems.contains(value)) {
                selectedItems.remove(value)
            } else {
                selectedItems.add(value)
            }
            notifyDataSetChanged()
        }
        return value
    }

    fun isSelected(value: Int): Boolean {
        return selectedItems.contains(value)
    }

    fun setNoneSelected() {
        selectedItems.clear()
        notifyDataSetChanged()
    }
}

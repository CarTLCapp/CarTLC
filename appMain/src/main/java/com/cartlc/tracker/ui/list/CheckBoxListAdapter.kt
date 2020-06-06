/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView

import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.misc.HashStringList
import kotlinx.android.synthetic.main.entry_item_check_box.view.*

typealias CheckBoxListListener = (position: Int, text: String, isSelected: Boolean) -> Unit

/**
 * Created by dug on 10/10/18.
 */
class CheckBoxListAdapter(
        ctx: Context,
        private val listener: CheckBoxListListener
) : RecyclerView.Adapter<CheckBoxListAdapter.CustomViewHolder>() {

    private val layoutInflater = LayoutInflater.from(ctx)

    var items: List<String> = emptyList()
        set(value) {
            field = value
            selectedItems.clear()
            notifyDataSetChanged()
        }

    var selectedItems = HashStringList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val box: CheckBox
            get() = view as CheckBox
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = layoutInflater.inflate(R.layout.entry_item_check_box, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val text = items[position]
        holder.view.item.text = text
        holder.box.isChecked = isSelected(text)

        holder.view.item.setOnClickListener {
            toggle(text)
            listener.invoke(position, text, isSelected(text))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun toggle(value: String) {
        if (selectedItems.contains(value)) {
            selectedItems.remove(value)
        } else {
            selectedItems.add(value)
        }
        notifyDataSetChanged()

    }

    private fun isSelected(value: String): Boolean {
        return selectedItems.contains(value)
    }
}

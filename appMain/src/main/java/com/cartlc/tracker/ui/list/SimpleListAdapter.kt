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

/**
 * Created by dug on 5/10/17.
 */

class SimpleListAdapter : RecyclerView.Adapter<SimpleListAdapter.CustomViewHolder> {

    internal val mContext: Context
    internal val mLayoutInflater: LayoutInflater
    internal val mEntryItemLayoutId: Int
    internal var mListener: OnItemSelectedListener? = null
    internal var mItems: List<String> = emptyList()
    internal var mSelectedPos = -1
    internal var mSelectedOkay = false

    interface OnItemSelectedListener {
        fun onSelectedItem(position: Int, text: String)
    }

    inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    constructor(context: Context, listener: OnItemSelectedListener) {
        mContext = context
        mListener = listener
        mEntryItemLayoutId = R.layout.entry_item_simple
        mLayoutInflater = LayoutInflater.from(mContext)
    }

    constructor(context: Context, entryItemLayoutId: Int) {
        mContext = context
        mEntryItemLayoutId = entryItemLayoutId
        mLayoutInflater = LayoutInflater.from(mContext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = mLayoutInflater.inflate(mEntryItemLayoutId, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val text = mItems[position]
        holder.view.item.text = text

        if (mSelectedOkay) {
            if (position == mSelectedPos) {
                holder.itemView.setBackgroundResource(R.color.list_item_selected)
            } else {
                holder.itemView.setBackgroundResource(android.R.color.transparent)
            }
        }
        holder.view.item.setOnClickListener {
            val pos = holder.adapterPosition
            setSelected(pos)
            mListener?.onSelectedItem(pos, text)
        }
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun setList(list: List<String>) {
        mItems = list
        notifyDataSetChanged()
    }

    fun setSelected(position: Int) {
        mSelectedPos = position
        mSelectedOkay = true
        notifyDataSetChanged()
    }

    fun setSelected(value: String): Int {
        val position = mItems.indexOf(value)
        setSelected(position)
        return position
    }

    fun setNoneSelected() {
        setSelected(-1)
    }
}

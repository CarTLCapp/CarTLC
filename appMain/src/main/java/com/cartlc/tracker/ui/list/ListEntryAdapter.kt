/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.list

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.ui.app.TBApplication
import kotlinx.android.synthetic.main.entry_item_full.view.*

/**
 * Created by dug on 5/12/17.
 */

class ListEntryAdapter(
        private val mContext: Context,
        private val mListener: OnItemSelectedListener
) : RecyclerView.Adapter<ListEntryAdapter.CustomViewHolder>() {
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private var mItems: List<DataEntry> = emptyList()

    private val app: TBApplication
        get() = mContext.applicationContext as TBApplication
    val repo: CarRepository
        get() = app.repo
    private val prefHelper: PrefHelper
        get() = repo.prefHelper

    inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

            fun bind(item: DataEntry) {
                with(view) {
                    truck_value!!.text = item.getTruckLine(mContext)
                    status!!.text = item.getStatus(mContext)
                    notes!!.text = item.notesLine
                    equipments!!.text = item.getEquipmentLine(mContext)
                    edit!!.setOnClickListener { mListener.onEdit(item) }
                    if (TextUtils.isEmpty(notes!!.text.toString().trim { it <= ' ' })) {
                        notes!!.visibility = View.GONE
                    } else {
                        notes!!.visibility = View.VISIBLE
                    }
                }
            }
    }

    interface OnItemSelectedListener {
        fun onEdit(entry: DataEntry)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = mLayoutInflater.inflate(R.layout.entry_item_full, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(mItems[position])
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun onDataChanged() {
        val combo = prefHelper.currentProjectGroup
        if (combo == null) {
            mItems = emptyList()
        } else {
            mItems = combo.entries
        }
    }
}

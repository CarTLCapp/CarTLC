/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.ui.listentries

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.bits.AutoLinearLayoutManager
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvcImpl
import com.cartlc.tracker.fresh.ui.listentries.item.ListEntriesItemViewMvc

class ListEntriesViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?,
        factoryViewMvc: FactoryViewMvc
) : ObservableViewMvcImpl<ListEntriesViewMvc.Listener>(),
        ListEntriesViewMvc,
        ListEntriesAdapter.Listener {

    override val rootView: View = inflater.inflate(R.layout.frame_list_entries, container, false) as ViewGroup

    private val editAddress = findViewById<Button>(R.id.edit_address)
    private val deleteView = findViewById<Button>(R.id.delete)
    private val listEntriesView = findViewById<RecyclerView>(R.id.list_entries)
    private val entryListAdapter = ListEntriesAdapter(factoryViewMvc, this)
    private val projectNameView = findViewById<TextView>(R.id.project_name)
    private val projectAddressView = findViewById<TextView>(R.id.project_address)

    init {
        editAddress.setOnClickListener {
            for (listener in listeners) {
                listener.onEditAddress()
            }
        }
        deleteView.setOnClickListener {
            for (listener in listeners) {
                listener.onDelete()
            }
        }
        val linearLayoutManager = AutoLinearLayoutManager(rootView.context)
        listEntriesView.layoutManager = linearLayoutManager
        listEntriesView.adapter = entryListAdapter
    }

    // region ListEntriesViewMvc

    override fun notifyDataSetChanged() {
        entryListAdapter.notifyDataSetChanged()
    }

    override var projectName: String?
        get() = projectNameView.text.toString()
        set(value) { projectNameView.text = value }

    override var projectAddress: String?
        get() = projectAddressView.text.toString()
        set(value) { projectAddressView.text = value }

    override var deleteVisible: Boolean
        get() = deleteView.visibility == View.VISIBLE
        set(value) { deleteView.visibility = if (value) View.VISIBLE else View.GONE }

    // endregion ListEntriesViewMvc

    // region ListEntriesAdapter.Listener

    override val itemCount: Int
        get() {
            return if (listeners.isNotEmpty()) {
                listeners.first().itemCount
            } else 0
        }

    override fun onBindViewHolder(itemViewMvc: ListEntriesItemViewMvc, position: Int) {
        for (listener in listeners) {
            listener.onBindViewHolder(itemViewMvc, position)
        }
    }

    // endregion ListEntriesAdapter.Listener
}
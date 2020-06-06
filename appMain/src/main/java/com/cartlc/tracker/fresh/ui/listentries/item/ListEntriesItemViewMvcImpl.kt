/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.ui.listentries.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl

class ListEntriesItemViewMvcImpl(inflater: LayoutInflater,
                                 container: ViewGroup?
) : ViewMvcImpl(), ListEntriesItemViewMvc {

    override val rootView: View = inflater.inflate(R.layout.entry_item_full, container, false)

    private val truckView = findViewById<TextView>(R.id.truck_value)
    private val statusView = findViewById<TextView>(R.id.status)
    private val notesView = findViewById<TextView>(R.id.notes)
    private val equipmentsView = findViewById<TextView>(R.id.equipments)
    private val editView = findViewById<Button>(R.id.edit)

    // region ListEntriesItemViewMvc

    override var truckValue: String?
        get() = truckView.text.toString()
        set(value) { truckView.text = value }
    override var status: String?
        get() = statusView.text.toString()
        set(value) { statusView.text = value }
    override var notesLine: String?
        get() = notesView.text.toString()
        set(value) {
            notesView.text = value
            notesView.visibility = if (value.isNullOrBlank()) View.GONE else View.VISIBLE
        }
    override var equipmentLine: String?
        get() = equipmentsView.text.toString()
        set(value) { equipmentsView.text = value }

    override fun bindOnEditListener(listener: () -> Unit) {
        editView.setOnClickListener {
            listener()
        }
    }

    // endregion ListEntriesItemViewMvc

}
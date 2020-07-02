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

    private val subProjectView = findViewById<TextView>(R.id.label_sub_project_value)
    private val truckView = findViewById<TextView>(R.id.truck_value)
    private val statusView = findViewById<TextView>(R.id.status)
    private val notesLabelView = findViewById<TextView>(R.id.label_notes)
    private val notesView = findViewById<TextView>(R.id.notes)
    private val equipmentsView = findViewById<TextView>(R.id.equipments)
    private val editView = findViewById<Button>(R.id.edit)
    private val incompleteView = findViewById<TextView>(R.id.incomplete)

    // region ListEntriesItemViewMvc

    override var subProjectValue: String
        get() = subProjectView.text.toString()
        set(value) {
            subProjectView.text = value
        }
    override var truckValue: String?
        get() = truckView.text.toString()
        set(value) {
            truckView.text = value
        }
    override var status: String?
        get() = statusView.text.toString()
        set(value) {
            statusView.text = value
        }
    override var notesLine: String?
        get() = notesView.text.toString()
        set(value) {
            if (value.isNullOrBlank()) {
                notesView.text = value
                notesView.visibility = View.VISIBLE
                notesLabelView.visibility = View.VISIBLE
            } else {
                notesView.visibility = View.GONE
                notesLabelView.visibility = View.GONE
            }
        }
    override var equipmentLine: String?
        get() = equipmentsView.text.toString()
        set(value) {
            equipmentsView.text = value
        }
    override var incompleteVisible: Boolean
        get() = incompleteView.visibility == View.VISIBLE
        set(value) {
            incompleteView.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var incompleteText: String
        get() = incompleteView.text.toString()
        set(value) {
            incompleteView.text = value
        }

    override fun bindOnEditListener(listener: () -> Unit) {
        editView.setOnClickListener {
            listener()
        }
    }

    // endregion ListEntriesItemViewMvc

}
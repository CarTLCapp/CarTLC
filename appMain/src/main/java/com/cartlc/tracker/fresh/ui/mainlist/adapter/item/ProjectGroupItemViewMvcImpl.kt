/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl

class ProjectGroupItemViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ViewMvcImpl(), ProjectGroupItemViewMvc {

    override val rootView = inflater.inflate(R.layout.mainlist_item_project, container, false)

    private val projectNameView = findViewById<TextView>(R.id.project_name)
    private val projectNotesView = findViewById<TextView>(R.id.project_notes)
    private val projectAddressView = findViewById<TextView>(R.id.project_address)

    override var projectName: String?
        get() = projectNameView.text.toString()
        set(value) {
            projectNameView.text = value
            projectNameView.visibility = if (value == null) View.GONE else View.VISIBLE
        }
    override var projectNotes: String?
        get() = projectNotesView.text.toString()
        set(value) {
            projectNotesView.text = value
            projectNotesView.visibility = if (value == null) View.GONE else View.VISIBLE
        }
    override var projectAddress: String?
        get() = projectAddressView.text.toString()
        set(value) {
            projectAddressView.text = value
            projectAddressView.visibility = if (value == null) View.GONE else View.VISIBLE
        }

    override var highlight: Boolean
        get() = TODO("not implemented")
        set(value) {
            if (value) {
                rootView.setBackgroundResource(R.color.project_highlight)
            } else {
                rootView.setBackgroundResource(R.color.project_normal)
            }
        }

    override fun bind(projectGroup: DataProjectAddressCombo, listener: ProjectGroupItemViewMvc.Listener) {
        rootView.setOnClickListener { listener.onProjectGroupSelected(projectGroup) }
    }

}
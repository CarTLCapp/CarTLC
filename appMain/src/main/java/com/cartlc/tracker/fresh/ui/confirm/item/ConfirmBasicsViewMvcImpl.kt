/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.confirm.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl
import com.cartlc.tracker.fresh.ui.confirm.data.ConfirmDataBasics

class ConfirmBasicsViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ViewMvcImpl(), ConfirmBasicsViewMvc {

    override val rootView: View = inflater.inflate(R.layout.confirm_basics, container, false)

    private val projectNameValue = findViewById<TextView>(R.id.project_name_value)
    private val projectAddressValue = findViewById<TextView>(R.id.project_address_value)
    private val statusValue = findViewById<TextView>(R.id.status_value)
    private val statusReasonLabel = findViewById<TextView>(R.id.status_reason_label)
    private val statusReasonValue = findViewById<TextView>(R.id.status_reason_value)

    // region ConfirmBasicsViewMvc

    // TODO: This control logic should not be in the ViewMvc class -- oh well.
    override var data: ConfirmDataBasics
        get() {
            return ConfirmDataBasics(projectName, projectAddress, status, statusReason)
        }
        set(value) {
            projectName = value.projectName
            projectAddress = value.projectAddress
            status = value.status
            if (value.reason.isNullOrEmpty()) {
                statusReasonVisible = false
            } else {
                statusReasonVisible = true
                statusReason = value.reason
            }
        }

    private var projectName: String
        get() = projectNameValue.text.toString()
        set(value) {
            projectNameValue.text = value
        }

    private var projectAddress: String?
        get() = projectAddressValue.text.toString()
        set(value) {
            projectAddressValue.text = value
            projectAddressValue.visibility = if (value == null) View.GONE else View.VISIBLE
        }

    private var status: String
        get() = statusValue.text.toString()
        set(value) {
            statusValue.text = value
        }

    private var statusReasonVisible: Boolean
        get() = statusReasonLabel.visibility == View.VISIBLE
        set(value) {
            if (value) {
                statusReasonLabel.visibility = View.VISIBLE
                statusReasonValue.visibility = View.VISIBLE
            } else {
                statusReasonLabel.visibility = View.GONE
                statusReasonValue.visibility = View.GONE
            }
        }
    private var statusReason: String
        get() = statusReasonValue.text.toString()
        set(value) {
            statusReasonValue.text = value
        }

    // endregion ConfirmBasicsViewMvc

}
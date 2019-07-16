package com.cartlc.tracker.fresh.ui.mainlist.adapter.item

import com.callassistant.util.viewmvc.ViewMvc
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo

interface ProjectGroupItemViewMvc : ViewMvc {

    interface Listener {
        fun onProjectGroupSelected(projectGroup: DataProjectAddressCombo)
    }

    var projectName: String?
    var projectNotes: String?
    var projectAddress: String?
    var highlight: Boolean

    fun bind(projectGroup: DataProjectAddressCombo, listener: Listener)

}
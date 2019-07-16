package com.cartlc.tracker.fresh.ui.mainlist.adapter.item

import com.callassistant.util.viewmvc.ViewMvc
import com.cartlc.tracker.fresh.model.core.data.DataEquipment

interface EquipmentSelectItemViewMvc : ViewMvc {

    interface Listener {
        fun onCheckedChanged(item: DataEquipment, isChecked: Boolean)
    }

    var text: String?
    var isChecked: Boolean

    fun bind(item: DataEquipment, listener: Listener)

}
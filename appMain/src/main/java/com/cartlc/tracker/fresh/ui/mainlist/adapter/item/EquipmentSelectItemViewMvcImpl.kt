package com.cartlc.tracker.fresh.ui.mainlist.adapter.item

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataEquipment
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl

class EquipmentSelectItemViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ViewMvcImpl(), EquipmentSelectItemViewMvc {

    override val rootView = inflater.inflate(R.layout.entry_item_equipment, container, false)

    private val checkButtonView = findViewById<CheckBox>(R.id.check_button)

    override var text: String?
        get() = checkButtonView.text.toString()
        set(value) { checkButtonView.text = value }

    override var isChecked: Boolean
        get() = checkButtonView.isChecked
        set(value) {
            checkButtonView.setOnCheckedChangeListener(null)
            checkButtonView.isChecked = value
        }

    override fun bind(item: DataEquipment, listener: EquipmentSelectItemViewMvc.Listener) {
        checkButtonView.setOnCheckedChangeListener { _, isChecked ->
            listener.onCheckedChanged(item, isChecked)
        }
    }
}
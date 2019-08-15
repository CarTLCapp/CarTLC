/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter

import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.CheckBoxItemViewMvc

class CheckBoxListController(
        private val listener: Listener
) : CheckBoxListAdapter.Listener,
        CheckBoxItemViewMvc.Listener,
        CheckBoxListUseCase
{

    interface Listener {
        fun onCheckBoxRefreshNeeded()
        fun onCheckBoxItemChanged(position: Int, item: String, isChecked: Boolean)
        fun isChecked(position: Int): Boolean
    }

    // region CheckBoxUseCase

    override var list: List<String> = emptyList()
        set(value) {
            field = value
            listener.onCheckBoxRefreshNeeded()
        }

    // endregion CheckBoxUseCase

    // region CheckBoxAdapter.Listener

    override val itemCount: Int
        get() = list.size

    override fun onBindViewHolder(viewMvc: CheckBoxItemViewMvc, position: Int) {
        val text = list[position]
        viewMvc.text = text
        viewMvc.isChecked = listener.isChecked(position)
        viewMvc.bind(position, text, this)
    }

    // endregion CheckBoxAdapter.Listener

    // region CheckBoxItemViewMvc.Listener

    override fun onCheckedChanged(position: Int, item: String, isChecked: Boolean) {
        listener.onCheckBoxItemChanged(position, item, isChecked)
    }

    // endregion CheckBoxItemViewMvc.Listener
}
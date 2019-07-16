/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter

import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.RadioListItemViewMvc

class RadioListController(
        private val listener: Listener
) : RadioListAdapter.Listener,
        RadioListItemViewMvc.Listener,
        RadioListUseCase
{

    interface Listener {
        fun onRadioRefreshNeeded()
        fun onRadioItemSelected(text: String)
    }

    // region RadioListUseCase

    override var list: List<String> = emptyList()
        set(value) {
            field = value
            listener.onRadioRefreshNeeded()
        }

    // endregion RadioListUseCase

    private var lastSelectedPos = -1
    private var lastSelectedText: String? = null

    private var selectedPos: Int
        get() = lastSelectedPos
        set(value) {
            lastSelectedPos = value
            listener.onRadioRefreshNeeded()
        }

    override var selectedText: String?
        get() = lastSelectedText
        set(value) {
            selectedPos = list.indexOf(value)
        }

    // region RadioListAdapter.Listener

    override val itemCount: Int
        get() = list.size

    override fun onBindViewHolder(viewMvc: RadioListItemViewMvc, position: Int) {
        val text = list[position]
        viewMvc.text = text
        viewMvc.isChecked = lastSelectedPos == position
        viewMvc.bind(position, text, this)
    }

    // endregion RadioListAdapter.Listener

    // region RadioListItemViewMvc.Listener

    override fun onCheckedChanged(position: Int, item: String, isChecked: Boolean) {
        lastSelectedPos = position
        lastSelectedText = item
        listener.onRadioItemSelected(item)
        listener.onRadioRefreshNeeded()
    }

    // endregion RadioListItemViewMvc.Listener
}
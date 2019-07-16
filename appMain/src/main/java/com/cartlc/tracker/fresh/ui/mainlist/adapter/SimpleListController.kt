package com.cartlc.tracker.fresh.ui.mainlist.adapter

class SimpleListController(
        private val listener: Listener
) : SimpleListAdapter.Listener {

    interface Listener {
        var simpleSelectedPostion: Int
        fun onSimpleItemClicked(position: Int, text: String)
    }

    // region SimpleListAdapter.Listener

    override fun onSimpleItemClicked(position: Int, text: String) {
        listener.simpleSelectedPostion = position
        listener.onSimpleItemClicked(position, text)
    }

    // endregion SimpleListAdapter.Listener

}
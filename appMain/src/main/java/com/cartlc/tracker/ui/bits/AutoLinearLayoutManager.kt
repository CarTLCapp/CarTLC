package com.cartlc.tracker.ui.bits

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AutoLinearLayoutManager(
        context: Context,
        orientation: Int = RecyclerView.VERTICAL,
        reverseLayout: Boolean = false
) : LinearLayoutManager(context, orientation, reverseLayout) {

    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }
}

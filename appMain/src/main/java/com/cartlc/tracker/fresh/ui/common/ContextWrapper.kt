package com.cartlc.tracker.fresh.ui.common

import android.content.Context
import androidx.annotation.DimenRes

class ContextWrapper(
        private val context: Context
) {

    fun getDimension(@DimenRes dimenId: Int): Int {
        return context.resources.getDimension(dimenId).toInt()
    }
}
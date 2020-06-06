/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.common

import android.content.Context
import androidx.annotation.DimenRes
import androidx.annotation.IntegerRes

class ContextWrapper(
        private val context: Context
) {

    fun getDimension(@DimenRes dimenId: Int): Int {
        return context.resources.getDimension(dimenId).toInt()
    }

    fun getInteger(@IntegerRes resId: Int): Int {
        return context.resources.getInteger(resId)
    }
}
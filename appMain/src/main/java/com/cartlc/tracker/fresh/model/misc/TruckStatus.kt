/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.misc

import android.content.Context

import com.cartlc.tracker.R

/**
 * Created by dug on 9/1/17.
 */
enum class TruckStatus constructor(internal var displayResId: Int) {
    COMPLETE(R.string.status_complete),
    PARTIAL(R.string.status_partial_install),
    NEEDS_REPAIR(R.string.status_needs_repair),
    UNKNOWN(R.string.status_unknown);

    fun getString(ctx: Context): String {
        return ctx.getString(displayResId)
    }

    fun getStringNull(ctx: Context): String? {
        if (this == UNKNOWN) {
            return null
        }
        return getString(ctx)
    }

    companion object {

        fun from(ord: Int?): TruckStatus {
            if (ord != null) {
                for (status in values()) {
                    if (status.ordinal == ord) {
                        return status
                    }
                }
            }
            return UNKNOWN
        }

        fun from(ctx: Context, text: String): TruckStatus {
            for (status in values()) {
                if (status.getString(ctx) == text) {
                    return status
                }
            }
            return UNKNOWN
        }
    }

}

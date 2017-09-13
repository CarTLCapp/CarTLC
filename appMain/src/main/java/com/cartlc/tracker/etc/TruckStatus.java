package com.cartlc.tracker.etc;

import android.content.Context;

import com.cartlc.tracker.R;

/**
 * Created by dug on 9/1/17.
 */
public enum TruckStatus {
    COMPLETE(R.string.status_complete),
    PARTIAL(R.string.status_partial_install),
    MISSING_TRUCK(R.string.status_missing_truck),
    NEEDS_REPAIR(R.string.status_needs_repair),
    UNKNOWN(R.string.status_unknown);

    int displayResId;

    TruckStatus(int res) {
        displayResId = res;
    }

    public static TruckStatus from(Integer ord) {
        if (ord != null) {
            for (TruckStatus status : values()) {
                if (status.ordinal() == ord) {
                    return status;
                }
            }
        }
        return UNKNOWN;
    }

    public String getString(Context ctx) {
        return ctx.getString(displayResId);
    }

}

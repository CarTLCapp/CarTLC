package com.cartlc.tracker.etc;

import android.content.Context;

import com.cartlc.tracker.R;

/**
 * Created by dug on 9/1/17.
 */
public enum TruckStatus {
    OKAY,
    MISSING_TRUCK,
    NEEDS_REPAIR;

    public static TruckStatus from(Integer ord) {
        if (ord != null) {
            for (TruckStatus status : values()) {
                if (status.ordinal() == ord) {
                    return status;
                }
            }
        }
        return OKAY;
    }

}

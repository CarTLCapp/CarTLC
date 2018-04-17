/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.event;

/**
 * Created by dug on 5/31/17.
 */

public class EventError {

    final String mMessage;

    public EventError(String msg) {
        mMessage = msg;
    }

    public String toString() {
        return mMessage;
    }
}

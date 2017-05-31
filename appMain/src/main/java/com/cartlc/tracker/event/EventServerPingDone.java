package com.cartlc.tracker.event;

/**
 * Created by dug on 5/31/17.
 */

public class EventServerPingDone {
    public int numUploads;

    public EventServerPingDone(int numUploads) {
        this.numUploads = numUploads;
    }
}

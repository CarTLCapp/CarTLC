package com.cartlc.tracker.app;

import android.util.Log;

import com.cartlc.tracker.data.TableCrash;

import timber.log.Timber;

/**
 * Created by dug on 8/16/17.
 */
public class CrashReportingTree extends Timber.Tree {
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }
        String trace;
        if (t != null) {
            trace = t.getMessage();
        } else {
            trace = null;
        }
        TableCrash.getInstance().message(priority, tag, message, trace);
    }
}
package com.cartlc.tracker.app;

import android.util.Log;

import com.cartlc.tracker.BuildConfig;
import com.cartlc.tracker.data.TableCrash;

import timber.log.Timber;

/**
 * Created by dug on 8/16/17.
 */
public class CrashReportingTree extends Timber.Tree {

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.DEBUG) {
            if (BuildConfig.DEBUG) {
                Log.d(tag, message);
            }
        } else if (priority == Log.VERBOSE) {
            if (BuildConfig.DEBUG) {
                Log.v(tag, message);
            }
        } else if (priority == Log.WARN) {
            Log.w(tag, message);
        } else if (priority == Log.INFO) {
            Log.i(tag, message);
        } else {
            if (priority == Log.ERROR) {
                Log.e(tag, message);
            } else if (priority == Log.ASSERT) {
                Log.wtf(tag, message);
            }
            String trace;
            if (t != null) {
                trace = t.getMessage();
            } else {
                trace = null;
            }
            TableCrash.getInstance().message(priority, message, trace);
        }
    }
}
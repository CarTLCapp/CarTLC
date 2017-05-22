package com.cartlc.tracker.app;

import android.app.Application;
import android.content.Intent;

import com.cartlc.tracker.BuildConfig;
import com.cartlc.tracker.data.DatabaseManager;
import com.cartlc.tracker.data.PrefHelper;
import com.cartlc.tracker.data.TestData;
import com.cartlc.tracker.server.DCService;
import com.cartlc.tracker.server.ServerHelper;

import timber.log.Timber;

/**
 * Created by dug on 4/14/17.
 */

public class TBApplication extends Application {

    public static final String OTHER = "Other";

    public TBApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseManager.Init(this);
        PrefHelper.Init(this);
        ServerHelper.Init(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        TestData.Init();
    }

    public void flushEvents() {
        startService(new Intent(this, DCService.class));
    }
}

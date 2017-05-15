package com.cartlc.tracker.app;

import android.app.Application;

import com.cartlc.tracker.BuildConfig;
import com.cartlc.tracker.data.DatabaseManager;
import com.cartlc.tracker.data.PrefHelper;
import com.cartlc.tracker.data.TestData;

import timber.log.Timber;

/**
 * Created by dug on 4/14/17.
 */

public class TBApplication extends Application {

    public TBApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseManager.Init(this);
        PrefHelper.Init(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        TestData.Init();
    }
}

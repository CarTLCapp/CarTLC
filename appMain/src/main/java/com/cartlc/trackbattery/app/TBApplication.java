package com.cartlc.trackbattery.app;

import android.app.Application;
import android.util.Log;

import com.cartlc.trackbattery.BuildConfig;
import com.cartlc.trackbattery.data.DatabaseManager;
import com.cartlc.trackbattery.data.PrefHelper;
import com.cartlc.trackbattery.data.TestData;

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

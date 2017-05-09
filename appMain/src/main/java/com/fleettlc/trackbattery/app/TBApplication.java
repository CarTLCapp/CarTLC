package com.fleettlc.trackbattery.app;

import android.app.Application;
import android.util.Log;

import com.fleettlc.trackbattery.BuildConfig;
import com.fleettlc.trackbattery.data.DatabaseManager;
import com.fleettlc.trackbattery.data.PrefHelper;
import com.fleettlc.trackbattery.data.TableProjects;
import com.fleettlc.trackbattery.data.TestData;

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

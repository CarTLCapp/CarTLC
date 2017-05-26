package com.cartlc.tracker.app;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.cartlc.tracker.BuildConfig;
import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DatabaseManager;
import com.cartlc.tracker.data.PrefHelper;
import com.cartlc.tracker.data.TestData;
import com.cartlc.tracker.server.DCService;
import com.cartlc.tracker.server.ServerHelper;

import timber.log.Timber;

import com.cartlc.tracker.util.PermissionHelper;
import com.cartlc.tracker.util.PermissionHelper.PermissionRequest;
import com.cartlc.tracker.util.PermissionHelper.PermissionListener;

/**
 * Created by dug on 4/14/17.
 */

public class TBApplication extends Application {

    public static final String OTHER = "Other";

    static final PermissionRequest[] PERMISSIONS = {
            new PermissionRequest(android.Manifest.permission.READ_PHONE_STATE, R.string.perm_read_phone_state)};

    public TBApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseManager.Init(this);
        PrefHelper.Init(this);
        ServerHelper.Init(this);
        PermissionHelper.Init();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        TestData.Init();
    }

    public void flushEvents() {
        startService(new Intent(this, DCService.class));
    }

    public void checkPermissions(Activity act, PermissionListener listener) {
        PermissionHelper.getInstance().checkPermissions(act, PERMISSIONS, listener);
    }

}

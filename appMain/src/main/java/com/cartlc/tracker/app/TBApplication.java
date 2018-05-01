/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.app;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.multidex.MultiDex;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.cartlc.tracker.BuildConfig;
import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DatabaseManager;
import com.cartlc.tracker.etc.CheckError;
import com.cartlc.tracker.etc.PrefHelper;
import com.cartlc.tracker.etc.BootstrapData;
import com.cartlc.tracker.data.TableZipCode;
import com.cartlc.tracker.data.DataZipCode;
import com.cartlc.tracker.event.EventError;
import com.cartlc.tracker.server.AmazonHelper;
import com.cartlc.tracker.server.DCService;
import com.cartlc.tracker.server.ServerHelper;
import com.cartlc.tracker.util.LocationHelper;
import com.cartlc.tracker.util.PermissionHelper.PermissionRequest;
import com.cartlc.tracker.util.PermissionHelper.PermissionListener;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

import com.cartlc.tracker.util.PermissionHelper;
import com.squareup.leakcanary.LeakCanary;

import java.io.File;

public class TBApplication extends Application {

    static final Boolean OVERRIDE_IS_DEVELOPMENT_SERVER = null;

    public static boolean IsDevelopmentServer() {
        if (OVERRIDE_IS_DEVELOPMENT_SERVER != null) {
            return OVERRIDE_IS_DEVELOPMENT_SERVER;
        }
        return BuildConfig.DEBUG;
    }

    static final Boolean DEBUG_TREE = false;
    static final Boolean LEAK_CANARY = false;

    public static final Boolean REPORT_LOCATION = true; // BuildConfig.DEBUG;
    public static final boolean LOCATION_ENABLE = true;

    public static final String OTHER = "Other";

    CrashReportingTree mCrashTree = new CrashReportingTree();

    static final PermissionRequest[] PERMISSIONS = new PermissionRequest[]{
            new PermissionRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.perm_read_external_storage),
            new PermissionRequest(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.perm_write_external_storage),
            new PermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, R.string.perm_location),
    };

    public TBApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (LEAK_CANARY) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return;
            }
            LeakCanary.install(this);
        }
        if (IsDevelopmentServer() && DEBUG_TREE) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(mCrashTree);
        }
        DatabaseManager.Init(this);
        PrefHelper.Init(this);
        ServerHelper.Init(this);
        PermissionHelper.Init();
        AmazonHelper.Init(this);
        BootstrapData.Init();
        CheckError.Init();

        PrefHelper.getInstance().detectSpecialUpdateCheck();
        LocationHelper.Init(this);
    }

    public void ping() {
        if (ServerHelper.getInstance().hasConnection()) {
            startService(new Intent(this, DCService.class));
        }
    }

    public void requestZipCode(String zipCode) {
        DataZipCode data = TableZipCode.getInstance().query(zipCode);
        if (data != null) {
            data.check();
            EventBus.getDefault().post(data);
        } else if (ServerHelper.getInstance().hasConnection()) {
            Intent intent = new Intent(this, DCService.class);
            intent.setAction(DCService.ACTION_ZIP_CODE);
            intent.putExtra(DCService.DATA_ZIP_CODE, zipCode);
            startService(intent);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static Uri getUri(Context ctx, File file) {
        return FileProvider.getUriForFile(ctx, "com.cartcl.tracker.fileprovider", file);
    }

    public static void hideKeyboard(Context ctx, View v) {
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void setUncaughtExceptionHandler(final Activity act) {
//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread t, Throwable e) {
//                mCrashTree.log(Log.ERROR, "CarTLCarTLC", e.getMessage(), null);
//                act.finish();
//            }
//        });
    }

    public void checkPermissions(Activity act, PermissionListener listener) {
        PermissionHelper.getInstance().checkPermissions(act, PERMISSIONS, listener);
    }

    public static String ReportError(Exception ex, Class claz, String function, String type) {
        return ReportError(ex.getMessage(), claz, function, type);
    }

    public static String ReportError(String msg, Class claz, String function, String type) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Class:");
        sbuf.append(claz.getSimpleName());
        sbuf.append(".");
        sbuf.append(function);
        sbuf.append(" ");
        sbuf.append(type);
        sbuf.append(": ");
        sbuf.append(msg);
        Timber.e(sbuf.toString());
        return sbuf.toString();
    }

    public String getVersion() throws PackageManager.NameNotFoundException {
        StringBuilder sbuf = new StringBuilder();
        String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        sbuf.append("v");
        sbuf.append(version);
        if (PrefHelper.getInstance().isDevelopment()) {
            sbuf.append("d");
        }
        return sbuf.toString();
    }

    public static void ReportServerError(Exception ex, Class claz, String function, String type) {
        String msg = ReportError(ex, claz, function, type);
        ShowError(msg);
    }

    public static void ShowError(String msg) {
        EventBus.getDefault().post(new EventError(msg));
    }
}

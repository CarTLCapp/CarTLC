/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.server;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.iid.InstanceID;

/**
 * Created by dug on 5/22/17.
 */

public class ServerHelper {

    static ServerHelper sInstance;

    static public ServerHelper getInstance() {
        return sInstance;
    }

    static public void Init(Context ctx) {
        if (sInstance == null) {
            new ServerHelper(ctx);
        }
    }

    final Context             mCtx;
    final ConnectivityManager mCM;

    public ServerHelper(Context ctx) {
        sInstance = this;
        mCtx = ctx;
        mCM = (ConnectivityManager) mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public boolean hasConnection() {
        final NetworkInfo ni = mCM.getActiveNetworkInfo();
        return (ni != null && ni.isConnectedOrConnecting());
    }

    public String getDeviceId() {
        return InstanceID.getInstance(mCtx).getId();
    }
}

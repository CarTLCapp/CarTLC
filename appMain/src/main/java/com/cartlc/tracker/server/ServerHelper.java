package com.cartlc.tracker.server;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

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
    final TelephonyManager    mTM;

    public ServerHelper(Context ctx) {
        sInstance = this;
        mCtx = ctx;
        mCM = (ConnectivityManager) mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        mTM = (TelephonyManager) mCtx.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public boolean hasConnection() {
        final NetworkInfo ni = mCM.getActiveNetworkInfo();
        if (ni != null) {
            if (ni.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else if (ni.getType() == ConnectivityManager.TYPE_MOBILE) {
                return true;
            }
        }
        return false;
    }

    public String getDeviceId() {
        return mTM.getDeviceId();
    }
}

/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import timber.log.Timber;

/**
 * Created by dug on 5/22/17.
 */

public class PermissionHelper {

    static final Boolean LOG          = true;
    static final int     REQUEST_CODE = 111;

    public static class PermissionRequest {
        public String permission;
        public int    explanationResId;

        public PermissionRequest(String permission, int resId) {
            this.permission = permission;
            this.explanationResId = resId;
        }
    }

    public interface PermissionListener {
        void onGranted(String permission);

        void onDenied(String permission);
    }

    @TargetApi(23)
    class RequestToken {
        ArrayList<PermissionListener> mListeners = new ArrayList<>();
        final String mPermission;
        int      mExplainResId;
        Activity mAct;

        RequestToken(Activity act, String perm, int explanationResId) {
            mAct = act;
            mPermission = perm;
            mExplainResId = explanationResId;
            mTokens.put(perm, this);
        }

        boolean equals(RequestToken token) {
            return token.mPermission.equals(mPermission);
        }

        public boolean requestPermission() {
            if (mAct.isFinishing() || mAct.isDestroyed()) {
                if (LOG) {
                    Timber.e("Activity closing down, aborting request.");
                }
                return false;
            }
            if (hasPermission()) {
                informListenersGranted();
            } else {
                if (LOG) {
                    Timber.i("Requesting new permission for: " + mPermission);
                }
                // Should we show an explanation?
                if (mExplainResId != 0) {
                    if (mAct.shouldShowRequestPermissionRationale(mPermission)) {
                        try {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
                            builder.setMessage(mExplainResId);
                            builder.setCancelable(false);
                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkPermission2();
                                }
                            });
                            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    mAlertDialog = null;
                                }
                            });
                            mAlertDialog = builder.create();
                            mAlertDialog.show();
                        } catch (Exception ex) {
                            Timber.e(ex.getMessage());
                            checkPermission2();
                        }
                    } else {
                        checkPermission2();
                    }
                } else {
                    checkPermission2();
                }
            }
            return true;
        }

        void checkPermission2() {
            mLastRequest = this;
            mAct.requestPermissions(new String[]{mPermission}, REQUEST_CODE);
        }

        void handlePermissionResult(int grantResult) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                informListenersGranted();
            } else {
                informListenersDenied();
            }
        }

        boolean hasPermission() {
            return mAct.checkSelfPermission(mPermission) == PackageManager.PERMISSION_GRANTED;
        }

        void addListener(PermissionListener listener) {
            if (listener != null) {
                if (!mListeners.contains(listener)) {
                    mListeners.add(listener);
                }
            }
        }

        void informListenersGranted() {
            for (PermissionListener listener : mListeners) {
                listener.onGranted(mPermission);
            }
            mListeners.clear();
        }

        void informListenersDenied() {
            for (PermissionListener listener : mListeners) {
                listener.onDenied(mPermission);
            }
            mListeners.clear();
        }
    }

    static PermissionHelper sInstance;

    HashMap<String, RequestToken> mTokens = new HashMap<>();
    Queue<RequestToken>           mLive   = new LinkedList<>();

    AlertDialog  mAlertDialog;
    RequestToken mLastRequest;

    public static PermissionHelper getInstance() {
        return sInstance;
    }

    public static void Init() {
        new PermissionHelper();
    }

    PermissionHelper() {
        sInstance = this;
    }

    public void checkPermissions(Activity act, PermissionRequest[] requests, PermissionListener listener) {
        for (PermissionRequest request : requests) {
            checkPermission(act, request, listener);
        }
    }

    public void checkPermission(Activity act, PermissionRequest request, PermissionListener listener) {
        checkPermission(act, request.permission, request.explanationResId, listener);
    }

    public void checkPermission(Activity act, String permission, int explanationResId, PermissionListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (act.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                if (listener != null) {
                    listener.onGranted(permission);
                }
            } else {
                RequestToken token = getToken(permission);
                if (token == null) {
                    token = new RequestToken(act, permission, explanationResId);
                } else {
                    token.mAct = act;
                    token.mExplainResId = explanationResId;
                }
                token.addListener(listener);
                addNewRequest(token);
            }
        } else {
            if (listener != null) {
                listener.onGranted(permission);
            }
        }
    }

    public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (LOG) {
            Timber.d("handlePermissionResult got " + permissions.length);
        }
        mLastRequest = null;
        if (requestCode == REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                RequestToken token = getToken(permission);
                if (token == null) {
                    Timber.e("Could not find request associated with code: " + permission);
                } else {
                    int grantResult = grantResults[i];
                    token.handlePermissionResult(grantResult);
                }
            }
            initiateNextRequest();
        } else {
            Timber.e("Ignoring request code : " + requestCode);
        }
    }

    RequestToken getToken(String permission) {
        return mTokens.get(permission);
    }

    void addNewRequest(RequestToken token) {
        if (LOG) {
            Timber.d("addNewRequest(" + token.mPermission + ")");
        }
        if (!isPosted(token)) {
            mLive.add(token);
        }
        if (mLastRequest != null) {
            if (mLastRequest.hasPermission()) {
                Timber.i("Apparently last permission request went through. So initiating next.");
                initiateNextRequest();
            } else {
                Timber.i("Already have a request pending so no new request issues.\n\tActive: "
                        + mLastRequest.mPermission + "\n\tNew: " + token.mPermission);
            }
        } else {
            initiateNextRequest();
        }
    }

    void initiateNextRequest() {
        if (mLive.size() > 0) {
            if (LOG) {
                Timber.d("initiateNextRequest(): REQUEST");
            }
            RequestToken token = mLive.poll();
            if (!token.requestPermission()) {
                if (LOG) {
                    Timber.d("request failed to initiate: readding to queue");
                }
                mLive.add(token);
            }
        } else {
            mLastRequest = null;
            if (LOG) {
                Timber.d("initiateNextRequest(): No more requests.");
            }
        }
    }

    boolean isPosted(RequestToken token) {
        for (RequestToken t : mLive) {
            if (t.equals(token)) {
                return true;
            }
        }
        return false;
    }
}

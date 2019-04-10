/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.util.helper

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build

import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedList
import java.util.Queue

import timber.log.Timber

/**
 * Created by dug on 5/22/17.
 */

class PermissionHelper {

    internal var mTokens = HashMap<String, RequestToken>()
    internal var mLive: Queue<RequestToken> = LinkedList()

    internal var mAlertDialog: AlertDialog? = null
    internal var mLastRequest: RequestToken? = null

    class PermissionRequest(var permission: String, var explanationResId: Int)

    interface PermissionListener {
        fun onGranted(permission: String)

        fun onDenied(permission: String)
    }

    @TargetApi(23)
    internal inner class RequestToken(var mAct: Activity, val mPermission: String, var mExplainResId: Int) {
        var mListeners = ArrayList<PermissionListener>()

        init {
            mTokens[mPermission] = this
        }

        fun equals(token: RequestToken): Boolean {
            return token.mPermission == mPermission
        }

        fun requestPermission(): Boolean {
            if (mAct.isFinishing || mAct.isDestroyed) {
                if (LOG) {
                    Timber.e("Activity closing down, aborting request.")
                }
                return false
            }
            if (hasPermission()) {
                informListenersGranted()
            } else {
                if (LOG) {
                    Timber.i("Requesting new permission for: $mPermission")
                }
                // Should we show an explanation?
                if (mExplainResId != 0) {
                    if (mAct.shouldShowRequestPermissionRationale(mPermission)) {
                        try {
                            val builder = AlertDialog.Builder(mAct)
                            builder.setMessage(mExplainResId)
                            builder.setCancelable(false)
                            builder.setPositiveButton(android.R.string.ok) { _, _ -> checkPermission2() }
                            builder.setOnCancelListener { mAlertDialog = null }
                            mAlertDialog = builder.create()
                            mAlertDialog!!.show()
                        } catch (ex: Exception) {
                            Timber.e(ex.message)
                            checkPermission2()
                        }

                    } else {
                        checkPermission2()
                    }
                } else {
                    checkPermission2()
                }
            }
            return true
        }

        fun checkPermission2() {
            mLastRequest = this
            mAct.requestPermissions(arrayOf(mPermission), REQUEST_CODE)
        }

        fun handlePermissionResult(grantResult: Int) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                informListenersGranted()
            } else {
                informListenersDenied()
            }
        }

        fun hasPermission(): Boolean {
            return mAct.checkSelfPermission(mPermission) == PackageManager.PERMISSION_GRANTED
        }

        fun addListener(listener: PermissionListener?) {
            if (listener != null) {
                if (!mListeners.contains(listener)) {
                    mListeners.add(listener)
                }
            }
        }

        fun informListenersGranted() {
            for (listener in mListeners) {
                listener.onGranted(mPermission)
            }
            mListeners.clear()
        }

        fun informListenersDenied() {
            for (listener in mListeners) {
                listener.onDenied(mPermission)
            }
            mListeners.clear()
        }
    }

    init {
        instance = this
    }

    fun checkPermissions(act: Activity, requests: Array<PermissionRequest>, listener: PermissionListener) {
        for (request in requests) {
            checkPermission(act, request, listener)
        }
    }

    fun checkPermission(act: Activity, request: PermissionRequest, listener: PermissionListener) {
        checkPermission(act, request.permission, request.explanationResId, listener)
    }

    fun checkPermission(act: Activity, permission: String, explanationResId: Int, listener: PermissionListener?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (act.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                listener?.onGranted(permission)
            } else {
                var token = getToken(permission)
                if (token == null) {
                    token = RequestToken(act, permission, explanationResId)
                } else {
                    token.mAct = act
                    token.mExplainResId = explanationResId
                }
                token.addListener(listener)
                addNewRequest(token)
            }
        } else {
            listener?.onGranted(permission)
        }
    }

    fun handlePermissionResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (LOG) {
            Timber.d("handlePermissionResult got " + permissions.size)
        }
        mLastRequest = null
        if (requestCode == REQUEST_CODE) {
            for (i in permissions.indices) {
                val permission = permissions[i]
                val token = getToken(permission)
                if (token == null) {
                    Timber.e("Could not find request associated with code: $permission")
                } else {
                    val grantResult = grantResults[i]
                    token.handlePermissionResult(grantResult)
                }
            }
            initiateNextRequest()
        } else {
            Timber.e("Ignoring request code : $requestCode")
        }
    }

    internal fun getToken(permission: String): RequestToken? {
        return mTokens[permission]
    }

    internal fun addNewRequest(token: RequestToken) {
        if (LOG) {
            Timber.d("addNewRequest(" + token.mPermission + ")")
        }
        if (!isPosted(token)) {
            mLive.add(token)
        }
        if (mLastRequest != null) {
            if (mLastRequest!!.hasPermission()) {
                Timber.i("Apparently last permission request went through. So initiating btnNext.")
                initiateNextRequest()
            } else {
                Timber.i("Already have a request pending so no new request issues.\n\tActive: "
                        + mLastRequest!!.mPermission + "\n\tNew: " + token.mPermission)
            }
        } else {
            initiateNextRequest()
        }
    }

    internal fun initiateNextRequest() {
        if (mLive.size > 0) {
            if (LOG) {
                Timber.d("initiateNextRequest(): REQUEST")
            }
            val token = mLive.poll()
            if (!token.requestPermission()) {
                if (LOG) {
                    Timber.d("request failed to initiate: readding to queue")
                }
                mLive.add(token)
            }
        } else {
            mLastRequest = null
            if (LOG) {
                Timber.d("initiateNextRequest(): No more requests.")
            }
        }
    }

    internal fun isPosted(token: RequestToken): Boolean {
        for (t in mLive) {
            if (t.equals(token)) {
                return true
            }
        }
        return false
    }

    companion object {

        internal val LOG = true
        internal val REQUEST_CODE = 111

        lateinit var instance: PermissionHelper
            internal set

        fun Init() {
            PermissionHelper()
        }
    }
}

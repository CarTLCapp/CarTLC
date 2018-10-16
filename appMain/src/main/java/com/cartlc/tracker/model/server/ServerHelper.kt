/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.server

import android.content.Context
import android.net.ConnectivityManager

import com.google.android.gms.iid.InstanceID

/**
 * Created by dug on 5/22/17.
 */

class ServerHelper {

    var deviceId: String? = null

    init {
        instance = this
    }

    fun hasConnection(ctx: Context): Boolean {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.isConnectedOrConnecting
    }

    companion object {

        lateinit var instance: ServerHelper
            internal set

        fun Init(ctx: Context) {
            ServerHelper()
            instance.deviceId = InstanceID.getInstance(ctx).id
        }
    }
}


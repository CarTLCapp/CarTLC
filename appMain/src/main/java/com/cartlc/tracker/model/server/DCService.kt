/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.server

import android.app.IntentService
import android.content.Intent

import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.ui.app.TBApplication

import timber.log.Timber

/**
 * Created by dug on 5/22/17.
 */
class DCService : IntentService(SERVER_NAME) {

    private lateinit var mPing: DCPing
    private lateinit var mZip: DCZip

    init {
        Timber.tag(DCService::class.java.simpleName)
    }

    override fun onHandleIntent(intent: Intent?) {
        val app = applicationContext as TBApplication
        mPing = DCPing(this)
        mZip = DCZip(app.db)
        ServerHelper.Init(this)
        if (!ServerHelper.instance.hasConnection(this)) {
            Timber.i("No connection -- service aborted")
            return
        }
        val action = intent!!.action
        if (ACTION_ZIP_CODE == action) {
            val zipCode = intent.getStringExtra(DATA_ZIP_CODE)
            mZip.findZipCode(zipCode)
        } else {
            if (app.prefHelper.techID == 0 || app.prefHelper.hasRegistrationChanged()) {
                if (app.prefHelper.hasName()) {
                    mPing.sendRegistration()
                }
            }
            mPing.ping()
        }
    }

    companion object {

        val ACTION_ZIP_CODE = "zipcode"
        val DATA_ZIP_CODE = "zipcode"

        private val SERVER_NAME = "CarTLC.DataCollectionService"
    }

}

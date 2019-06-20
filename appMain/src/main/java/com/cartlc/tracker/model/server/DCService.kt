/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.server

import android.app.IntentService
import android.content.Intent
import com.cartlc.tracker.model.CarRepository

import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.ui.app.TBApplication

import timber.log.Timber

/**
 * Created by dug on 5/22/17.
 */
class DCService : IntentService(SERVER_NAME) {

    private lateinit var mZip: DCZip
    private lateinit var app: TBApplication
    private lateinit var ping: DCPing

    private val repo: CarRepository
        get() = app.repo
    private val db: DatabaseTable
        get() = repo.db
    private val prefHelper: PrefHelper
        get() = repo.prefHelper

    init {
        Timber.tag(DCService::class.java.simpleName)
    }

    override fun onHandleIntent(intent: Intent?) {
        app = applicationContext as TBApplication
        ping = app.ping
        mZip = DCZip(db)
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
            if (prefHelper.techID == 0 && prefHelper.hasCode) {
                prefHelper.firstTechCode?.let {
                    ping.sendRegistration(it, prefHelper.secondaryTechCode)
                } ?: run {
                    Timber.e("Need to register first!")
                    return
                }
            }
            ping.ping()
        }
    }

    companion object {

        val ACTION_ZIP_CODE = "zipcode"
        val DATA_ZIP_CODE = "zipcode"

        private val SERVER_NAME = "CarTLC.DataCollectionService"
    }

}

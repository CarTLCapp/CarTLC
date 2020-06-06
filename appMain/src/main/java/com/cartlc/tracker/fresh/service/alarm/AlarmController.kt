/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.service.alarm

import android.content.Context
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.flow.LoginFlow
import timber.log.Timber
import java.util.concurrent.TimeUnit


class AlarmController(
        private val context: Context,
        private val repo: CarRepository
) {

    companion object {
        private const val DEBUG = false
        private val CHECK_ACTIVITY_TRIGGER_MINUTES = if (DEBUG) 2 else 60
        private val CHECK_ACTIVITY_TRIGGER = TimeUnit.MINUTES.toMillis(CHECK_ACTIVITY_TRIGGER_MINUTES.toLong())
        private val CHECK_ACTIVITY_TRIGGER_RETRY = CHECK_ACTIVITY_TRIGGER
        private val JUST_LOGGED_IN_TRIGGER = if (DEBUG) TimeUnit.MINUTES.toMillis(2) else TimeUnit.HOURS.toMillis(24)

        fun justLoggedIn(context: Context) {
            AlarmReceiver.scheduleIn(context, JUST_LOGGED_IN_TRIGGER)
        }
    }

    private val prefHelper = repo.prefHelper

    private val hasHadRecentActivity: Boolean
        get() {
            val lastTime = prefHelper.lastActivityTime
            val now = System.currentTimeMillis()
            val diff = now - lastTime
            return diff < CHECK_ACTIVITY_TRIGGER
        }

    fun checkAutoLogout() {
        if (!hasHadRecentActivity) {
            clearLogin()
            repo.curFlowValue = LoginFlow()
        } else {
            AlarmReceiver.scheduleIn(context, CHECK_ACTIVITY_TRIGGER_RETRY)
        }
    }

    private fun clearLogin() {
        Timber.i("LOGIN CLEARED")
        prefHelper.firstTechCode = null
        prefHelper.techFirstName = null
        prefHelper.techLastName = null
        prefHelper.techID = 0
        prefHelper.secondaryTechCode = null
        prefHelper.secondaryTechFirstName = null
        prefHelper.secondaryTechLastName = null
        prefHelper.secondaryTechID = 0
    }
}
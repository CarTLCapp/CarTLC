package com.cartlc.tracker.fresh.ui.daily.hours.data

import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.ui.daily.project.DataProjectsImpl
import java.util.concurrent.TimeUnit

class HoursDataProjectsImpl(
    db: DatabaseTable
) : DataProjectsImpl(db) {

    companion object {
        private const val WINDOW_SINCE_DAYS = 2L
        private const val FALLBACK_SINCE_DAYS = 3L
    }

    override val mostRecentUploadedDate: Long
        get() {
            return db.tableHours.queryMostRecentUploaded()?.let { data ->
                data.date - TimeUnit.DAYS.toMillis(WINDOW_SINCE_DAYS)
            } ?: run {
                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(FALLBACK_SINCE_DAYS)
            }
        }

}
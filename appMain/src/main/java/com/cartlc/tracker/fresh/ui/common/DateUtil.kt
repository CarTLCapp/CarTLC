package com.cartlc.tracker.fresh.ui.common

import android.content.Context
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateUtil(
        private val context: Context
) {

    companion object {

        private const val DATE_FORMAT = "MM/dd/yy EEE"

        fun getDateString(date: Long?): String {
            if (date == null || date <= 0L) {
                return "UNSET"
            }
            val df = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            return df.format(Date(date))
        }


        private const val TIME_FORMAT = "h:mm a"

        fun getTimeString(time: Long?): String {
            if (time == null) {
                return "UNSET"
            }
            val df = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
            return df.format(Date(time))
        }

        fun getDateFormat(format: String, atTime: Long): String {
            return SimpleDateFormat(format, Locale.getDefault()).format(atTime)
        }

    }

    val is24Hour: Boolean
        get() = DateFormat.is24HourFormat(context)

}
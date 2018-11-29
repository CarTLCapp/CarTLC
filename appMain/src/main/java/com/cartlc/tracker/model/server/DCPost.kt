/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.server

import android.util.Log

import com.cartlc.tracker.ui.app.TBApplication

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection

/**
 * Created by dug on 8/24/17.
 */

open class DCPost {

    @Throws(IOException::class)
    protected fun getResult(connection: HttpURLConnection): String? {
        val inputStream: InputStream?
        try {
            inputStream = connection.inputStream
        } catch (ex: Exception) {
            showError(connection)
            return null
        }
        if (inputStream == null) {
            showError(connection)
            return null
        }
        return getStreamString(inputStream)
    }

    @Throws(IOException::class)
    protected fun getStreamString(inputStream: InputStream?): String? {
        if (inputStream == null) {
            return null
        }
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sbuf = StringBuilder()
        var inputLine: String?
        while (true) {
            inputLine = reader.readLine()
            if (inputLine == null) {
                break
            }
            if (sbuf.length > 0) {
                sbuf.append("\n")
            }
            sbuf.append(inputLine)
        }
        reader.close()
        return sbuf.toString()
    }

    @Throws(IOException::class)
    protected fun showError(connection: HttpURLConnection) {
        val errorMsg = getStreamString(connection.errorStream)
        val msg: String
        if (errorMsg == null) {
            msg = "Server might be DOWN. Try again later."
        } else {
            msg = "Server COMPLAINT: $errorMsg"
        }
        Log.e(TAG, msg)
        TBApplication.ShowError(msg)
    }

    companion object {

        internal val TAG = "DCPost"
    }
}

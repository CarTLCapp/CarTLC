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


    companion object {

        private const val TAG = "DCPost"
        private const val MAX_SIZE = 65536

    }


    @Throws(IOException::class)
    protected fun getResult(connection: HttpURLConnection): String? {
        val inputStream: InputStream?
        try {
            inputStream = connection.inputStream
        } catch (ex: Exception) {
            showError(connection, ex.message)
            return null
        }
        if (inputStream == null) {
            showError(connection, null)
            return null
        }
        return getStreamString(inputStream)
    }

    @Throws(IOException::class)
    protected fun getStreamString(inputStream: InputStream?): String? {
        if (inputStream == null) {
            return null
        }
        val buffer = CharArray(1024)
        val reader = BufferedReader(InputStreamReader(inputStream)!!)
        var count: Int
        val sbuf = StringBuffer()
        while (true) {
            count = reader.read(buffer, 0, buffer.size)
            if (count < 0) {
                break
            }
            sbuf.append(buffer.copyOfRange(0, count))
            if (sbuf.length > MAX_SIZE) {
                break
            }
        }
        reader.close()
        return sbuf.toString()
    }

    @Throws(IOException::class)
    protected fun showError(connection: HttpURLConnection, message: String?) {
        val errorMsg = getStreamString(connection.errorStream)
        val msg: String
        if (errorMsg == null) {
            if (message == null) {
                msg = "Server might be DOWN. Try again later."
            } else {
                msg = "Server connection error: $message"
            }
        } else {
            msg = "Server COMPLAINT: $errorMsg"
        }
        Log.e(TAG, msg)
        TBApplication.ShowError(msg)
    }

}

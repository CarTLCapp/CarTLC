/*
 * *
 *   * Copyright 2019, FleetTLC. All rights reserved
 *
 */
package com.cartlc.tracker.fresh.service.endpoint

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
    protected fun getResult(connection: HttpURLConnection): String {
        val inputStream: InputStream?
        try {
            inputStream = connection.inputStream
        } catch (ex: Exception) {
            throw IOException(getError(connection, ex.message))
        }
        inputStream?.let {
            return getStreamString(it)
        }
        throw IOException(getError(connection, null))
    }

    @Throws(IOException::class)
    protected fun getStreamString(inputStream: InputStream): String {
        val buffer = CharArray(1024)
        val reader = BufferedReader(InputStreamReader(inputStream))
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

    private fun getError(connection: HttpURLConnection, message: String?): String {
        val errorMsg = getStreamString(connection.errorStream)
        return if (errorMsg == null) {
            if (message == null) {
                "Server might be DOWN. Try again later."
            } else {
                "Server connection error: $message"
            }
        } else {
            "Server COMPLAINT: $errorMsg"
        }
    }

}

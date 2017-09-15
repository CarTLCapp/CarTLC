package com.cartlc.tracker.server;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * Created by dug on 8/24/17.
 */

public class DCPost {

    static final String TAG = "DCPost";

    protected String getResult(HttpURLConnection connection) throws IOException {
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (Exception ex) {
            Log.e(TAG, "Server response not available. (" + ex.getMessage() + ")");
            return null;
        }
        if (inputStream == null) {
            Log.e(TAG, "NULL response from server");
            return null;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sbuf = new StringBuilder();
        String inputLine;
        while ((inputLine = reader.readLine()) != null) {
            if (sbuf.length() > 0) {
                sbuf.append("\n");
            }
            sbuf.append(inputLine);
        }
        return sbuf.toString();
    }
}

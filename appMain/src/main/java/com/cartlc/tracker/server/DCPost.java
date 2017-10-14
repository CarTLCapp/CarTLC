package com.cartlc.tracker.server;

import android.util.Log;

import com.cartlc.tracker.app.TBApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import de.greenrobot.event.EventBus;

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
            final String msg = "Server is DOWN. (" + ex.getMessage() + ")";
            Log.e(TAG, msg);
            TBApplication.ShowError(msg);
            return null;
        }
        if (inputStream == null) {
            final String msg = "NULL response from server";
            Log.e(TAG, msg);
            TBApplication.ShowError(msg);
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

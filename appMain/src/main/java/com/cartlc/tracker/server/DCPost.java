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
            showError(connection);
            return null;
        }
        if (inputStream == null) {
            showError(connection);
            return null;
        }
        return getStreamString(inputStream);
    }

    protected String getStreamString(InputStream inputStream) throws IOException {
        if (inputStream == null) {
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
        reader.close();
        return sbuf.toString();
    }

    protected void showError(HttpURLConnection connection) throws IOException {
        final String errorMsg = getStreamString(connection.getErrorStream());
        String msg;
        if (errorMsg == null) {
            msg = "Server might be DOWN. Try again later.";
        } else {
            msg = "Server COMPLAINT: " + errorMsg;
        }
        Log.e(TAG, msg);
        TBApplication.ShowError(msg);
    }
}

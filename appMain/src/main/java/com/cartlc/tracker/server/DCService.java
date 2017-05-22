package com.cartlc.tracker.server;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.data.PrefHelper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import timber.log.Timber;

/**
 * Created by dug on 5/22/17.
 */
public class DCService extends IntentService {

    static final String SERVER_NAME = "CarTLC.DataCollectionService";
    static final String SERVER_URL  = "http://cartlc.arqnetworks.com/";
    static final String REGISTER    = SERVER_URL + "/register";

    public DCService() {
        super(SERVER_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ServerHelper.Init(this);

        if (!ServerHelper.getInstance().hasConnection()) {
            Timber.i("No connection -- service aborted");
            return;
        }
        if (PrefHelper.getInstance().getTechID() == 0) {
            if (PrefHelper.getInstance().hasName()) {
                sendRegistration();
            }
        }
    }

    void sendRegistration() {
        try {
            String deviceId = ServerHelper.getInstance().getDeviceId();
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("first_name", PrefHelper.getInstance().getFirstName());
            jsonObject.accumulate("last_name", PrefHelper.getInstance().getLastName());
            jsonObject.accumulate("imei", deviceId);
            post(jsonObject);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    boolean post(JSONObject json) {
        try {
            URL url = new URL(REGISTER);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write(json.toString());

            Timber.d("MYDEBUG: SENT: " + json.toString());

            writer.close();
            InputStream inputStream = connection.getInputStream();
            if (inputStream == null) {
                Timber.e("Unexpected NULL response from server");
                return false;
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
            Timber.i("Response was: " + sbuf.toString());
        } catch (Exception ex) {
            Timber.e(ex);
            return false;
        }
        return true;
    }
}

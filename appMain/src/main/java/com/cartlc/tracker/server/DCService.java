package com.cartlc.tracker.server;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

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
    static final String PING        = SERVER_URL + "/ping";

    public static final String PROJECT = "project";
    public static final String COMPANY = "company";
    public static final String EQUIPMENT = "equipment";
    public static final String NOTE = "note";

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
            post(REGISTER, jsonObject);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }


    void ping() {
        try {
            String deviceId = ServerHelper.getInstance().getDeviceId();
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("imei", deviceId);
            String response = post(REGISTER, jsonObject);

            JSONObject object = parseResult(response);
            if (object == null) {
                Timber.e("Unexpected NULL response from server");
            }
            int project_version = object.getInt(PROJECT);
            int equipment_version = object.getInt(EQUIPMENT);
            int note_version = object.getInt(NOTE);
            int company_version = object.getInt(COMPANY);

        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    String post(String target, JSONObject json) {
        try {
            URL url = new URL(target);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write(json.toString());
            writer.close();
            InputStream inputStream = connection.getInputStream();
            if (inputStream == null) {
                Timber.e("Unexpected NULL response from server");
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
        } catch (Exception ex) {
            Timber.e(ex);
            return null;
        }
    }

    JSONObject parseResult(String result)
    {
        try
        {
            return new JSONObject(result);
        }
        catch (Exception ex)
        {
            Timber.e("Got bad result back from server: " + result + "\n" + ex.getMessage());
        }
        return new JSONObject();
    }

}

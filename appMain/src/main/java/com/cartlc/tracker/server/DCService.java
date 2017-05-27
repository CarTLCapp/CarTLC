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
    static final String PROJECTS    = SERVER_URL + "/projects";

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
        ping();
    }

    void sendRegistration() {
        try {
            String deviceId = ServerHelper.getInstance().getDeviceId();
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("first_name", PrefHelper.getInstance().getFirstName());
            jsonObject.accumulate("last_name", PrefHelper.getInstance().getLastName());
            jsonObject.accumulate("device_id", deviceId);
            post(REGISTER, jsonObject);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    void ping() {
        try {
            String response = post(PING);
            if (response == null) {
                Timber.e("Unexpected NULL response from server");
                return;
            }
            JSONObject object = parseResult(response);

            int version_project = object.getInt(PrefHelper.VERSION_PROJECT);
            int version_equipment = object.getInt(PrefHelper.VERSION_EQUIPMENT);
            int version_note = object.getInt(PrefHelper.VERSION_NOTE);
            int version_company = object.getInt(PrefHelper.VERSION_COMPANY);

            if (PrefHelper.getInstance().getVersionProject() != version_project) {
                Timber.i("New project version " + version_project);
                queryProjects();
                // PrefHelper.getInstance().setVersionProject(version_project);
            }
            if (PrefHelper.getInstance().getVersionCompany() != version_company) {
                Timber.i("New company version " + version_company);
                // PrefHelper.getInstance().setVersionCompany(version_company);
            }
            if (PrefHelper.getInstance().getVersionEquipment() != version_equipment) {
                Timber.i("New equipment version " + version_equipment);
                // PrefHelper.getInstance().setVersionEquipment(version_equipment);
            }
            if (PrefHelper.getInstance().getVersionNote() != version_note) {
                Timber.i("New note version " + version_note);
                // PrefHelper.getInstance().setVersionNote(version_note);
            }
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    void queryProjects() {
        try {
            String response = post(PROJECTS);
            if (response == null) {
                Timber.e("Unexpected NULL response from server");
                return;
            }
            Timber.i("queryProjects():\n" + response);
        } catch (Exception ex) {
            Timber.e(ex);
        }

    }

    String post(String target) {
        try {
            String deviceId = ServerHelper.getInstance().getDeviceId();
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("device_id", deviceId);
            return post(target, jsonObject);
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return null;
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

    JSONObject parseResult(String result) {
        try {
            return new JSONObject(result);
        } catch (Exception ex) {
            Timber.e("Got bad result back from server: " + result + "\n" + ex.getMessage());
        }
        return new JSONObject();
    }

}

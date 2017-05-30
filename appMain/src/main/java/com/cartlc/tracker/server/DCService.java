package com.cartlc.tracker.server;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.cartlc.tracker.data.DataAddress;
import com.cartlc.tracker.data.DataEquipment;
import com.cartlc.tracker.data.DataProject;
import com.cartlc.tracker.data.PrefHelper;
import com.cartlc.tracker.data.TableAddress;
import com.cartlc.tracker.data.TableEquipment;
import com.cartlc.tracker.data.TableProjects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/22/17.
 */
public class DCService extends IntentService {

    static final String SERVER_NAME       = "CarTLC.DataCollectionService";
    static final String SERVER_URL        = "http://cartlc.arqnetworks.com/";
    static final String REGISTER          = SERVER_URL + "/register";
    static final String PING              = SERVER_URL + "/ping";
    static final String PROJECTS          = SERVER_URL + "/projects";
    static final String COMPANIES         = SERVER_URL + "/companies";
    static final String EQUIPMENTS        = SERVER_URL + "/equipments";
    static final String PROJECT_EQUIPMENT = SERVER_URL + "/project_equipment";

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
        if (PrefHelper.getInstance().getTechID() == 0 || PrefHelper.getInstance().hasRegistrationChanged()) {
            if (PrefHelper.getInstance().hasName()) {
                sendRegistration();
            }
        }
        ping();
    }

    void sendRegistration() {
        Timber.i("sendRegistration()");
        try {
            String deviceId = ServerHelper.getInstance().getDeviceId();
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("first_name", PrefHelper.getInstance().getFirstName());
            jsonObject.accumulate("last_name", PrefHelper.getInstance().getLastName());
            jsonObject.accumulate("device_id", deviceId);
            String result = post(REGISTER, jsonObject);
            int tech_id = Integer.parseInt(result);
            PrefHelper.getInstance().setTechID(tech_id);
            PrefHelper.getInstance().setRegistrationChanged(false);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    void ping() {
        Timber.i("ping()");
        try {
            String response = post(PING);
            if (response == null) {
                Timber.e("Unexpected NULL response from server");
                return;
            }
            Timber.d("GOT THIS RESULT BACK: " + response);

            JSONObject object = parseResult(response);

            int version_project = object.getInt(PrefHelper.VERSION_PROJECT);
            int version_equipment = object.getInt(PrefHelper.VERSION_EQUIPMENT);
            int version_note = object.getInt(PrefHelper.VERSION_NOTE);
            int version_company = object.getInt(PrefHelper.VERSION_COMPANY);

            if (PrefHelper.getInstance().getVersionProject() != version_project) {
                Timber.i("New project version " + version_project);
                queryProjects();
                PrefHelper.getInstance().setVersionProject(version_project);
            }
            if (PrefHelper.getInstance().getVersionCompany() != version_company) {
                Timber.i("New company version " + version_company);
                queryCompanies();
                PrefHelper.getInstance().setVersionCompany(version_company);
            }
            if (PrefHelper.getInstance().getVersionEquipment() != version_equipment) {
                Timber.i("New equipment version " + version_equipment);
                queryEquipments();
                PrefHelper.getInstance().setVersionEquipment(version_equipment);
            }
            if (PrefHelper.getInstance().getVersionNote() != version_note) {
                Timber.i("New note version " + version_note);
//                queryNotes();
                // PrefHelper.getInstance().setVersionNote(version_note);
            }
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    void queryProjects() {
        Timber.i("queryProjects()");
        try {
            String response = post(PROJECTS);
            if (response == null) {
                Timber.e("Unexpected NULL response from server");
                return;
            }
            List<String> unprocessed = TableProjects.getInstance().query();

            JSONObject object = parseResult(response);
            JSONArray array = object.getJSONArray("projects");
            for (int i = 0; i < array.length(); i++) {
                JSONObject ele = array.getJSONObject(i);
                int server_id = ele.getInt("id");
                String name = ele.getString("name");
                DataProject project = TableProjects.getInstance().queryByServerId(server_id);
                if (project == null) {
                    if (unprocessed.contains(name)) {
                        // If this name already exists, convert the existing one by simply giving it the server_id.
                        DataProject existing = TableProjects.getInstance().queryByName(name);
                        existing.server_id = server_id;
                        TableProjects.getInstance().update(existing);
                        Timber.i("Commandeer local: " + name);
                    } else {
                        // Otherwise just add the new project.
                        Timber.i("New project: " + name);
                        TableProjects.getInstance().add(name, server_id);
                    }
                } else {
                    // Name change?
                    if (!name.equals(project.name)) {
                        Timber.i("New name: " + name);
                        project.name = name;
                        TableProjects.getInstance().update(project);
                    } else {
                        Timber.i("No change: " + name);
                    }
                }
                unprocessed.remove(name);
            }
            // Remaining unprocessed elements are disabled if they have entries.
            for (String name : unprocessed) {
                DataProject existing = TableProjects.getInstance().queryByName(name);
                if (existing != null) {
                    TableProjects.getInstance().removeOrDisable(existing);
                }
            }
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    void queryCompanies() {
        Timber.i("queryCompanies()");
        try {
            String response = post(COMPANIES);
            if (response == null) {
                Timber.e("Unexpected NULL response from server");
                return;
            }
            List<DataAddress> unprocessed = TableAddress.getInstance().query();

            JSONObject object = parseResult(response);
            JSONArray array = object.getJSONArray("companies");
            for (int i = 0; i < array.length(); i++) {
                JSONObject ele = array.getJSONObject(i);
                int server_id = ele.getInt("id");
                String name = ele.getString("name");
                String street = ele.getString("street");
                String city = ele.getString("city");
                String state = ele.getString("state");
                DataAddress incoming = new DataAddress(server_id, name, street, city, state);
                DataAddress item = TableAddress.getInstance().queryByServerId(server_id);
                if (item == null) {
                    DataAddress match = get(unprocessed, incoming);
                    if (match != null) {
                        // If this name already exists, convert the existing one by simply giving it the server_id.
                        match.server_id = server_id;
                        TableAddress.getInstance().update(match);
                        Timber.i("Commandeer local: " + name);
                        unprocessed.remove(match);
                    } else {
                        // Otherwise just add the new entry.
                        Timber.i("New company: " + name);
                        TableAddress.getInstance().add(incoming);
                    }
                } else {
                    // Change of name, street, city or state?
                    if (!incoming.equals(item)) {
                        Timber.i("Change: " + name);
                        incoming.id = item.id;
                        incoming.server_id = item.server_id;
                        TableAddress.getInstance().update(incoming);
                    } else {
                        Timber.i("No change: " + name);
                    }
                }
            }
            // Remaining unprocessed elements are disabled if they have entries.
            for (DataAddress item : unprocessed) {
                TableAddress.getInstance().removeOrDisable(item);
            }
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    DataAddress get(List<DataAddress> items, DataAddress match) {
        for (DataAddress item : items) {
            if (item.equals(match)) {
                return item;
            }
        }
        return null;
    }

    void queryEquipments() {
        Timber.i("queryEquipments()");
        try {
            String response = post(EQUIPMENTS);
            if (response == null) {
                Timber.e("Unexpected NULL response from server");
                return;
            }
            List<DataEquipment> unprocessed = TableEquipment.getInstance().query();
            JSONObject object = parseResult(response);
            JSONArray array = object.getJSONArray("equipments");
            for (int i = 0; i < array.length(); i++) {
                JSONObject ele = array.getJSONObject(i);
                int server_id = ele.getInt("id");
                String name = ele.getString("name");
                DataEquipment incoming = new DataEquipment(name, server_id);
                DataEquipment item = TableEquipment.getInstance().queryByServerId(server_id);
                if (item == null) {
                    DataEquipment match = get(unprocessed, incoming);
                    if (match != null) {
                        // If this name already exists, convert the existing one by simply giving it the server_id.
                        match.server_id = server_id;
                        TableEquipment.getInstance().update(match);
                        Timber.i("Commandeer local: " + name);
                        unprocessed.remove(match);
                    } else {
                        // Otherwise just add the new entry.
                        Timber.i("New company: " + name);
                        TableEquipment.getInstance().add(incoming);
                    }
                } else {
                    // Change of name
                    if (!incoming.equals(item)) {
                        Timber.i("Change: " + name);
                        incoming.id = item.id;
                        incoming.server_id = item.server_id;
                        incoming.isLocal = false;
                        TableEquipment.getInstance().update(incoming);
                    } else {
                        Timber.i("No change: " + name);
                    }
                }
            }
            response = post(PROJECT_EQUIPMENT);
            if (response == null) {
                Timber.e("Unexpected NULL response from server");
                return;
            }


        } catch (Exception ex) {
            Timber.e(ex);
        }
    }


    DataEquipment get(List<DataEquipment> items, DataEquipment match) {
        for (DataEquipment item : items) {
            if (item.equals(match)) {
                return item;
            }
        }
        return null;
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
            InputStream inputStream;
            try {
                inputStream = connection.getInputStream();
            } catch (Exception ignored) {
                Timber.e("Server not available.");
                return null;
            }
            if (inputStream == null) {
                Timber.e("NULL response from server");
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

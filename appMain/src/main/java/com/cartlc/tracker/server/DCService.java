package com.cartlc.tracker.server;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cartlc.tracker.data.DataAddress;
import com.cartlc.tracker.data.DataCollectionItem;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataEquipment;
import com.cartlc.tracker.data.DataNote;
import com.cartlc.tracker.data.DataPicture;
import com.cartlc.tracker.data.DataProject;
import com.cartlc.tracker.data.DatabaseManager;
import com.cartlc.tracker.data.PrefHelper;
import com.cartlc.tracker.data.TableAddress;
import com.cartlc.tracker.data.TableCollectionEquipmentProject;
import com.cartlc.tracker.data.TableCollectionNoteProject;
import com.cartlc.tracker.data.TableCrash;
import com.cartlc.tracker.data.TableEntry;
import com.cartlc.tracker.data.TableEquipment;
import com.cartlc.tracker.data.TableNote;
import com.cartlc.tracker.data.TablePictureCollection;
import com.cartlc.tracker.data.TableProjects;
import com.cartlc.tracker.event.EventServerPingDone;

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

import de.greenrobot.event.EventBus;
import timber.log.Timber;

/**
 * Created by dug on 5/22/17.
 */
public class DCService extends IntentService {

    static final String TAG = "DCService";

    static final String SERVER_NAME            = "CarTLC.DataCollectionService";
    static final String SERVER_URL_DEVELOPMENT = "http://cartlc.arqnetworks.com/";
    static final String SERVER_URL_RELEASE     = "http://fleettlc.arqnetworks.com/";

    static final String UPLOAD_RESET_TRIGGER = "reset_upload";
    static final String RE_REGISTER_TRIGGER  = "re-register";

    final String SERVER_URL;
    final String REGISTER;
    final String ENTER;
    final String PING;
    final String PROJECTS;
    final String COMPANIES;
    final String EQUIPMENTS;
    final String NOTES;
    final String MESSAGE;

    public DCService() {
        super(SERVER_NAME);

        if (PrefHelper.getInstance().isDevelopment()) {
            SERVER_URL = SERVER_URL_DEVELOPMENT;
        } else {
            SERVER_URL = SERVER_URL_RELEASE;
        }
        REGISTER = SERVER_URL + "register";
        ENTER = SERVER_URL + "enter";
        PING = SERVER_URL + "ping";
        PROJECTS = SERVER_URL + "projects";
        COMPANIES = SERVER_URL + "companies";
        EQUIPMENTS = SERVER_URL + "equipments";
        NOTES = SERVER_URL + "notes";
        MESSAGE = SERVER_URL + "message";
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
            Timber.i("TECH ID=" + tech_id);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    void ping() {
        Timber.i("ping()");
        try {
            if (PrefHelper.getInstance().getTechID() == 0) {
                return;
            }
            String deviceId = ServerHelper.getInstance().getDeviceId();
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("device_id", deviceId);
            jsonObject.accumulate("tech_id", PrefHelper.getInstance().getTechID());
            String response = post(PING, jsonObject);
            if (response == null) {
                Timber.e("ping(): Unexpected NULL response from server");
                return;
            }
            JSONObject object = parseResult(response);
            if (object.has(UPLOAD_RESET_TRIGGER)) {
                if (object.getBoolean(UPLOAD_RESET_TRIGGER)) {
                    Timber.i("UPLOAD RESET!");
                    DatabaseManager.getInstance().clearUploaded();
                }
            }
            if (object.has(RE_REGISTER_TRIGGER)) {
                if (object.getBoolean(RE_REGISTER_TRIGGER)) {
                    Timber.i("RE-REGISTER DETECTED!");
                    sendRegistration();
                }
            }
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
                queryNotes();
                PrefHelper.getInstance().setVersionNote(version_note);
            }
            List<DataEntry> entries = TableEntry.getInstance().queryPendingDataToUploadToMaster();
            int count = 0;
            if (entries.size() > 0) {
                count = sendEntries(entries);
            }
            if (count > 0) {
                EventBus.getDefault().post(new EventServerPingDone());
            }
            entries = TableEntry.getInstance().queryPendingPicturesToUpload();
            if (entries.size() > 0) {
                AmazonHelper.getInstance().sendPictures(entries);
            }
            TablePictureCollection.getInstance().clearUploadedUnscaledPhotos();
            List<TableCrash.CrashLine> lines = TableCrash.getInstance().queryNeedsUploading();
            sendCrashLines(lines);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    void queryProjects() {
        Timber.i("queryProjects()");
        try {
            String response = post(PROJECTS);
            if (response == null) {
                Timber.e("queryProjects(): Unexpected NULL response from server");
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
                        // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                        DataProject existing = TableProjects.getInstance().queryByName(name);
                        existing.server_id = server_id;
                        existing.isBootStrap = false;
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
                Timber.e("queryCompanies(): Unexpected NULL response from server");
                return;
            }
            List<DataAddress> unprocessed = TableAddress.getInstance().query();
            JSONObject object = parseResult(response);
            JSONArray array = object.getJSONArray("companies");
            String name, street, city, state, zipcode;
            for (int i = 0; i < array.length(); i++) {
                JSONObject ele = array.getJSONObject(i);
                int server_id = ele.getInt("id");
                name = ele.getString("name");
                if (ele.has("street")) {
                    street = ele.getString("street");
                } else {
                    street = null;
                }
                if (ele.has("city")) {
                    city = ele.getString("city");
                } else {
                    city = null;
                }
                if (ele.has("state")) {
                    state = ele.getString("state");
                } else {
                    state = null;
                }
                if (ele.has("zipcode")) {
                    zipcode = ele.getString("zipcode");
                } else {
                    zipcode = null;
                }
                DataAddress incoming = new DataAddress(server_id, name, street, city, state, zipcode);
                DataAddress item = TableAddress.getInstance().queryByServerId(server_id);
                if (item == null) {
                    DataAddress match = get(unprocessed, incoming);
                    if (match != null) {
                        // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                        match.server_id = server_id;
                        match.isLocal = false;
                        match.isBootStrap = false;
                        TableAddress.getInstance().update(match);
                        Timber.i("Commandeer local: " + match.toString());
                        unprocessed.remove(match);
                    } else {
                        // Otherwise just add the new entry.
                        Timber.i("New company: " + incoming.toString());
                        TableAddress.getInstance().add(incoming);
                    }
                } else {
                    // Change of name, street, city or state?
                    if (!incoming.equals(item) || (incoming.isLocal != item.isLocal)) {
                        incoming.id = item.id;
                        incoming.server_id = item.server_id;
                        incoming.isLocal = false;
                        Timber.i("Change: " + incoming.toString());
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
                Timber.e("queryEquipments(): Unexpected NULL response from server");
                return;
            }
            JSONObject object = parseResult(response);
            {
                List<DataEquipment> unprocessed = TableEquipment.getInstance().query();
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
                            match.isBootStrap = false;
                            match.isLocal = false;
                            TableEquipment.getInstance().update(match);
                            Timber.i("Commandeer local: " + name);
                            unprocessed.remove(match);
                        } else {
                            // Otherwise just add the new entry.
                            Timber.i("New equipment: " + name);
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
                        unprocessed.remove(item);
                    }
                }
                // Remaining unprocessed elements are disabled if they have entries.
                for (DataEquipment item : unprocessed) {
                    TableEquipment.getInstance().removeOrDisable(item);
                }
            }
            {
                List<DataCollectionItem> unprocessed = TableCollectionEquipmentProject.getInstance().query();
                JSONArray array = object.getJSONArray("project_equipment");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject ele = array.getJSONObject(i);
                    int server_id = ele.getInt("id");
                    int server_project_id = ele.getInt("project_id");
                    int server_equipment_id = ele.getInt("equipment_id");
                    DataCollectionItem incoming = new DataCollectionItem();
                    incoming.server_id = server_id;
                    // Note: project ID is from the perspective of the server, not the APP.
                    DataProject project = TableProjects.getInstance().queryByServerId(server_project_id);
                    if (project == null) {
                        Timber.e("Can't find project with ID " + server_project_id);
                        continue;
                    }
                    incoming.collection_id = project.id;
                    DataEquipment equipment = TableEquipment.getInstance().queryByServerId(server_equipment_id);
                    if (equipment == null) {
                        Timber.e("Can't find equipment with ID " + server_equipment_id);
                        continue;
                    }
                    incoming.value_id = equipment.id;
                    DataCollectionItem item = TableCollectionEquipmentProject.getInstance().queryByServerId(server_id);
                    if (item == null) {
                        DataCollectionItem match = get(unprocessed, incoming);
                        if (match != null) {
                            // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                            match.server_id = server_id;
                            match.isBootStrap = false;
                            TableCollectionEquipmentProject.getInstance().update(match);
                            Timber.i("Commandeer local: PROJECT COLLECTION " + match.collection_id + ", " + match.value_id);
                            unprocessed.remove(match);
                        } else {
                            // Otherwise just add the new entry.
                            Timber.i("New project collection. " + incoming.collection_id + ", " + incoming.value_id);
                            TableCollectionEquipmentProject.getInstance().add(incoming);
                        }
                    } else {
                        // Change of IDs. A little weird, but we will allow it.
                        if (!incoming.equals(item)) {
                            Timber.i("Change? " + item.collection_id + ", " + item.value_id);
                            incoming.id = item.id;
                            incoming.server_id = item.server_id;
                            TableCollectionEquipmentProject.getInstance().update(incoming);
                        }
                    }
                }
                for (DataCollectionItem item : unprocessed) {
                    TableCollectionEquipmentProject.getInstance().removeIfGone(item);
                }
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

    DataCollectionItem get(List<DataCollectionItem> items, DataCollectionItem match) {
        for (DataCollectionItem item : items) {
            if (item.equals(match)) {
                return item;
            }
        }
        return null;
    }

    void queryNotes() {
        Timber.i("queryNotes()");
        try {
            String response = post(NOTES);
            if (response == null) {
                Timber.e("queryNotes(): Unexpected NULL response from server");
                return;
            }
            JSONObject object = parseResult(response);
            {
                List<DataNote> unprocessed = TableNote.getInstance().query();
                JSONArray array = object.getJSONArray("notes");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject ele = array.getJSONObject(i);
                    int server_id = ele.getInt("id");
                    String name = ele.getString("name");
                    String typeStr = ele.getString("type");
                    DataNote.Type type = DataNote.Type.from(typeStr);

                    DataNote incoming = new DataNote(name, type, server_id);
                    DataNote item = TableNote.getInstance().queryByServerId(server_id);
                    if (item == null) {
                        DataNote match = get(unprocessed, incoming);
                        if (match != null) {
                            // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                            match.server_id = server_id;
                            TableNote.getInstance().update(match);
                            Timber.i("Commandeer local: " + name);
                            unprocessed.remove(match);
                        } else {
                            // Otherwise just add the new entry.
                            Timber.i("New note: " + name);
                            TableNote.getInstance().add(incoming);
                        }
                    } else {
                        // Change of name
                        if (!incoming.equals(item)) {
                            Timber.i("Change: " + name);
                            incoming.id = item.id;
                            incoming.server_id = item.server_id;
                            TableNote.getInstance().update(incoming);
                        } else {
                            Timber.i("No change: " + name);
                        }
                        unprocessed.remove(item);
                    }
                }
                // Remove or disable unprocessed elements
                for (DataNote note : unprocessed) {
                    TableNote.getInstance().removeIfUnused(note);
                }
            }
            {
                List<DataCollectionItem> unprocessed = TableCollectionNoteProject.getInstance().query();
                JSONArray array = object.getJSONArray("project_note");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject ele = array.getJSONObject(i);
                    int server_id = ele.getInt("id");
                    int server_project_id = ele.getInt("project_id");
                    int server_note_id = ele.getInt("note_id");
                    DataCollectionItem incoming = new DataCollectionItem();
                    incoming.server_id = server_id;
                    // Note: project ID is from the perspective of the server, not the APP.
                    DataProject project = TableProjects.getInstance().queryByServerId(server_project_id);
                    if (project == null) {
                        Timber.e("Can't find project with ID " + server_project_id);
                        continue;
                    }
                    incoming.collection_id = project.id;
                    DataNote note = TableNote.getInstance().queryByServerId(server_note_id);
                    if (note == null) {
                        Timber.e("Can't find note with ID " + server_note_id);
                        continue;
                    }
                    incoming.value_id = note.id;
                    DataCollectionItem item = TableCollectionNoteProject.getInstance().queryByServerId(server_id);
                    if (item == null) {
                        DataCollectionItem match = get(unprocessed, incoming);
                        if (match != null) {
                            // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                            match.server_id = server_id;
                            TableCollectionNoteProject.getInstance().update(match);
                            Timber.i("Commandeer local: NOTE COLLECTION " + match.collection_id + ", " + match.value_id);
                            unprocessed.remove(match);
                        } else {
                            // Otherwise just add the new entry.
                            Timber.i("New note collection. " + incoming.collection_id + ", " + incoming.value_id);
                            TableCollectionNoteProject.getInstance().add(incoming);
                        }
                    } else {
                        // Change of IDs. A little weird, but we will allow it.
                        if (!incoming.equals(item)) {
                            Timber.i("Change? " + item.collection_id + ", " + item.value_id);
                            incoming.id = item.id;
                            incoming.server_id = item.server_id;
                            TableCollectionNoteProject.getInstance().update(incoming);
                        }
                    }
                }
                for (DataCollectionItem item : unprocessed) {
                    TableCollectionNoteProject.getInstance().removeIfGone(item);
                }
            }
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    int sendEntries(List<DataEntry> list) {
        int count = 0;
        for (DataEntry entry : list) {
            if (sendEntry(entry)) {
                count++;
            }
        }
        return count;
    }

    boolean sendEntry(DataEntry entry) {
        boolean success = false;
        Timber.i("sendEntry(" + entry.id + ")");
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("tech_id", PrefHelper.getInstance().getTechID());
            jsonObject.accumulate("date", entry.date);
            if (entry.truckNumber > 0) {
                jsonObject.accumulate("truck_number", entry.truckNumber);
            }
            if (entry.licensePlateNumber != null) {
                jsonObject.accumulate("license_plate", entry.licensePlateNumber);
            }
            DataProject project = entry.getProject();
            if (project == null) {
                Timber.e("No project name for entry -- abort");
                return false;
            }
            if (project.server_id > 0) {
                jsonObject.accumulate("project_id", project.server_id);
            } else {
                jsonObject.accumulate("project_name", project.name);
            }
            DataAddress address = entry.getAddress();
            if (address == null) {
                Timber.e("No address for entry -- abort");
                return false;
            }
            if (address.server_id > 0) {
                jsonObject.accumulate("address_id", address.server_id);
            } else {
                jsonObject.accumulate("address", address.getLine());
            }
            List<DataEquipment> equipments = entry.getEquipment();
            if (equipments.size() > 0) {
                JSONArray jarray = new JSONArray();
                for (DataEquipment equipment : equipments) {
                    JSONObject jobj = new JSONObject();
                    if (equipment.server_id > 0) {
                        jobj.accumulate("equipment_id", equipment.server_id);
                    } else {
                        jobj.accumulate("equipment_name", equipment.name);
                    }
                    jarray.put(jobj);
                }
                jsonObject.put("equipment", jarray);
            }
            List<DataPicture> pictures = entry.getPictures();
            if (pictures.size() > 0) {
                JSONArray jarray = new JSONArray();
                for (DataPicture picture : pictures) {
                    JSONObject jobj = new JSONObject();
                    jobj.put("filename", picture.getTailname());
                    jarray.put(jobj);
                }
                jsonObject.put("picture", jarray);
            }
            List<DataNote> notes = entry.getNotes();
            if (notes.size() > 0) {
                JSONArray jarray = new JSONArray();
                for (DataNote note : notes) {
                    JSONObject jobj = new JSONObject();
                    if (note.server_id > 0) {
                        jobj.put("id", note.server_id);
                    } else {
                        jobj.put("name", note.name);
                    }
                    jobj.put("value", note.value);
                    jarray.put(jobj);
                }
                jsonObject.put("notes", jarray);
            }
            Timber.i("SENDING " + jsonObject.toString());
            String result = post(ENTER, jsonObject);
            try {
                int code = Integer.parseInt(result);
                if (code == 0) {
                    TableEntry.getInstance().setUploadedMaster(entry, true);
                    success = true;
                    Timber.i("SUCCESS");
                } else {
                    Timber.e("While trying to send entry " + entry.id + ": " + code);
                }
            } catch (Exception ex) {
                Timber.e("While trying to send entry " + entry.id + ": " + result);
            }
        } catch (Exception ex) {
            Timber.e(ex);
            return false;
        }
        return success;
    }

    DataNote get(List<DataNote> items, DataNote match) {
        for (DataNote item : items) {
            if (item.equals(match)) {
                return item;
            }
        }
        return null;
    }

    String post(String target) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("device_id", ServerHelper.getInstance().getDeviceId());
            jsonObject.accumulate("tech_id", PrefHelper.getInstance().getTechID());
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
            } catch (Exception ex) {
                Log.e(TAG, "Server response not available. (" + ex.getMessage() + ")\nJSON=" + json.toString());
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
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
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

    void sendCrashLines(List<TableCrash.CrashLine> lines) {
        for (TableCrash.CrashLine line : lines) {
            sendCrashLine(line);
        }
    }

    void sendCrashLine(TableCrash.CrashLine line) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("tech_id", PrefHelper.getInstance().getTechID());
            jsonObject.accumulate("date", line.date);
            jsonObject.accumulate("code", line.code);
            jsonObject.accumulate("tag", line.tag);
            jsonObject.accumulate("message", line.message);
            jsonObject.accumulate("trace", line.trace);
            String result = post(MESSAGE, jsonObject);
            if (result != null && Integer.parseInt(result) == 0) {
                TableCrash.getInstance().setUploaded(line);
            } else {
                Log.e(TAG, "Unable to send previously trapped message: " + line.message);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }


}

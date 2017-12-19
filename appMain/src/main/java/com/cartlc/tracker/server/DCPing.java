package com.cartlc.tracker.server;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.cartlc.tracker.BuildConfig;
import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.data.DataAddress;
import com.cartlc.tracker.data.DataCollectionItem;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataEquipment;
import com.cartlc.tracker.data.DataNote;
import com.cartlc.tracker.data.DataPicture;
import com.cartlc.tracker.data.DataProject;
import com.cartlc.tracker.data.DataTruck;
import com.cartlc.tracker.data.DatabaseManager;
import com.cartlc.tracker.data.TableCollection;
import com.cartlc.tracker.etc.PrefHelper;
import com.cartlc.tracker.data.TableAddress;
import com.cartlc.tracker.data.TableCollectionEquipmentProject;
import com.cartlc.tracker.data.TableCollectionNoteProject;
import com.cartlc.tracker.data.TableCrash;
import com.cartlc.tracker.data.TableEntry;
import com.cartlc.tracker.data.TableEquipment;
import com.cartlc.tracker.data.TableNote;
import com.cartlc.tracker.data.TablePictureCollection;
import com.cartlc.tracker.data.TableProjects;
import com.cartlc.tracker.data.TableTruck;
import com.cartlc.tracker.etc.TruckStatus;
import com.cartlc.tracker.event.EventError;
import com.cartlc.tracker.event.EventRefreshProjects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

/**
 * Created by dug on 8/24/17.
 */

public class DCPing extends DCPost {

    static final String TAG = "DCPing";

    static final String SERVER_URL_DEVELOPMENT = "http://cartlc.arqnetworks.com/";
    static final String SERVER_URL_RELEASE     = "http://fleettlc.arqnetworks.com/";

    static final String UPLOAD_RESET_TRIGGER = "reset_upload";
    static final String RE_REGISTER_TRIGGER  = "re-register";

    // After this many times indicate to the user if there is a problem that needs to be
    // addressed with the entry.
    static final int FAILED_UPLOADED_TRIGGER = 5;

    final String  SERVER_URL;
    final String  REGISTER;
    final String  ENTER;
    final String  PING;
    final String  PROJECTS;
    final String  COMPANIES;
    final String  EQUIPMENTS;
    final String  NOTES;
    final String  TRUCKS;
    final String  MESSAGE;
    final Context mContext;
    String mVersion;

    public DCPing(Context ctx) {
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
        TRUCKS = SERVER_URL + "trucks";
        mContext = ctx;
    }

    String getVersion() {
        if (mVersion == null) {
            try {
                mVersion = ((TBApplication) mContext.getApplicationContext()).getVersion();
            } catch (Exception ex) {
                TBApplication.ReportError(ex, DCPing.class, "getVersion()", "server");
            }
        }
        return mVersion;
    }

    public void sendRegistration() {
        Timber.i("sendRegistration()");
        try {
            String deviceId = ServerHelper.getInstance().getDeviceId();
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("first_name", PrefHelper.getInstance().getFirstName());
            jsonObject.accumulate("last_name", PrefHelper.getInstance().getLastName());
            jsonObject.accumulate("device_id", deviceId);
            String result = post(REGISTER, jsonObject, true);
            if (result != null) {
                if (TextUtils.isDigitsOnly(result)) {
                    int tech_id = Integer.parseInt(result);
                    PrefHelper.getInstance().setTechID(tech_id);
                    PrefHelper.getInstance().setRegistrationChanged(false);
                    Timber.i("TECH ID=" + tech_id);
                } else {
                    Timber.i("sendRegistration() failed");
                }
            }
        } catch (Exception ex) {
            TBApplication.ReportError(ex, DCPing.class, "sendRegistration()", "server");
        }
    }

    public void ping() {
        Timber.i("ping()");
        try {
            if (PrefHelper.getInstance().getTechID() == 0) {
                return;
            }
            String deviceId = ServerHelper.getInstance().getDeviceId();
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("device_id", deviceId);
            jsonObject.accumulate("tech_id", PrefHelper.getInstance().getTechID());
            jsonObject.accumulate("app_version", getVersion());
            String response = post(PING, jsonObject, true);
            if (response == null) {
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
            int version_truck = object.getInt(PrefHelper.VERSION_TRUCK);
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
            if (PrefHelper.getInstance().getVersionTruck() != version_truck) {
                Timber.i("New truck version " + version_truck);
                queryTrucks();
                PrefHelper.getInstance().setVersionTruck(version_truck);
            }
            List<DataEntry> entries = TableEntry.getInstance().queryPendingDataToUploadToMaster();
            int count = 0;
            if (entries.size() > 0) {
                count = sendEntries(entries);
            }
            if (count > 0) {
                EventBus.getDefault().post(new EventRefreshProjects());
            }
            entries = TableEntry.getInstance().queryPendingPicturesToUpload();
            if (entries.size() > 0) {
                AmazonHelper.getInstance().sendPictures(entries);
            }
            TablePictureCollection.getInstance().clearUploadedUnscaledPhotos();
            List<TableCrash.CrashLine> lines = TableCrash.getInstance().queryNeedsUploading();
            sendCrashLines(lines);
            // If any entries do not yet have server-id's, try to get them.
            entries = TableEntry.getInstance().queryServerIds();
            if (entries.size() > 0) {
                Timber.i("FOUND " + entries.size() + " entries without server IDS");
                sendEntries(entries);
            } else {
                Timber.i("All entries have server ids");
            }
        } catch (Exception ex) {
            TBApplication.ReportServerError(ex, DCPing.class, "ping()", "server");
        }
    }

    void queryProjects() {
        Timber.i("queryProjects()");
        try {
            String response = post(PROJECTS, true);
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
                boolean disabled = ele.getBoolean("disabled");
                DataProject project = TableProjects.getInstance().queryByServerId(server_id);
                if (project == null) {
                    if (unprocessed.contains(name)) {
                        // If this name already exists, convert the existing one by simply giving it the server_id.
                        DataProject existing = TableProjects.getInstance().queryByName(name);
                        existing.serverId = server_id;
                        existing.isBootStrap = false;
                        existing.disabled = disabled;
                        TableProjects.getInstance().update(existing);
                        Timber.i("Commandeer local: " + name);
                    } else {
                        // Otherwise just add the new project.
                        Timber.i("New project: " + name);
                        TableProjects.getInstance().add(name, server_id, disabled);
                    }
                } else {
                    // Name change?
                    if (!name.equals(project.name)) {
                        Timber.i("New name: " + name);
                        project.name = name;
                        project.disabled = disabled;
                        TableProjects.getInstance().update(project);
                    } else if (project.disabled != disabled) {
                        Timber.i("Project " + name + " " + (disabled ? "disabled" : "enabled"));
                        project.disabled = disabled;
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
                    Timber.i("Project disable or delete: " + name);
                    TableProjects.getInstance().removeOrDisable(existing);
                }
            }
        } catch (Exception ex) {
            TBApplication.ReportError(ex, DCPing.class, "queryProjects()", "server");
        }
    }

    void queryCompanies() {
        Timber.i("queryCompanies()");
        try {
            String response = post(COMPANIES, true);
            if (response == null) {
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
                        match.serverId = server_id;
                        match.isLocal = false;
                        match.isBootStrap = false;
                        TableAddress.getInstance().update(match);
                        Timber.i("Commandeer local: " + match.toString());
                        unprocessed.remove(match);
                    } else {
                        // Otherwise just add the new entry.
                        TableAddress.getInstance().add(incoming);
                        Timber.i("New company: " + incoming.toString());
                    }
                } else {
                    // Change of name, street, city or state?
                    if (!incoming.equals(item) || (incoming.isLocal != item.isLocal)) {
                        incoming.id = item.id;
                        incoming.serverId = item.serverId;
                        incoming.isLocal = false;
                        Timber.i("Change: " + incoming.toString());
                        TableAddress.getInstance().update(incoming);
                    } else {
                        Timber.i("No change: " + incoming.toString());
                    }
                    unprocessed.remove(item);
                }
            }
            // Remaining unprocessed elements are disabled if they have entries.
            for (DataAddress item : unprocessed) {
                TableAddress.getInstance().removeOrDisable(item);
            }
        } catch (Exception ex) {
            TBApplication.ReportError(ex, DCPing.class, "queryCompanies()", "server");
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
        final boolean showDebug = BuildConfig.DEBUG;
        Timber.i("queryEquipments()");
        try {
            String response = post(EQUIPMENTS, true);
            if (response == null) {
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
                            match.serverId = server_id;
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
                            incoming.serverId = item.serverId;
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
                    DataEquipment equipment = TableEquipment.getInstance().queryByServerId(server_equipment_id);
                    if (project == null || equipment == null) {
                        if (project == null && equipment == null) {
                            Timber.e("Can't find any project with server ID " + server_project_id + " nor equipment ID " + server_equipment_id);
                            PrefHelper.getInstance().reloadProjects();
                            PrefHelper.getInstance().reloadEquipments();
                        } else if (project == null) {
                            StringBuilder sbuf = new StringBuilder();
                            sbuf.append("Can't find any project with server ID ");
                            sbuf.append(server_project_id);
                            sbuf.append(" for equipment ");
                            sbuf.append(equipment.name);
                            sbuf.append(". Projects=");
                            for (String name : TableProjects.getInstance().query()) {
                                sbuf.append(name);
                                sbuf.append(":");
                            }
                            sbuf.append(".");
                            Timber.e(sbuf.toString());
                            PrefHelper.getInstance().reloadProjects();
                        } else {
                            Timber.e("Can't find any equipment with server ID " + server_equipment_id + " for project " + project.name);
                            PrefHelper.getInstance().reloadEquipments();
                        }
                        continue;
                    }
                    incoming.collection_id = project.id;
                    incoming.value_id = equipment.id;
                    DataCollectionItem item = TableCollectionEquipmentProject.getInstance().queryByServerId(server_id);
                    if (item == null) {
                        DataCollectionItem match = get(unprocessed, incoming);
                        if (match != null) {
                            // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                            match.server_id = server_id;
                            match.isBootStrap = false;
                            TableCollectionEquipmentProject.getInstance().update(match);
                            if (showDebug) {
                                String projectName = TableProjects.getInstance().queryProjectName(match.collection_id);
                                String equipmentName = TableEquipment.getInstance().queryEquipmentName(match.value_id);
                                Timber.i("Commandeer local: PROJECT COLLECTION " + projectName + " <=> " + equipmentName);
                            }
                            unprocessed.remove(match);
                        } else {
                            // Otherwise just add the new entry.
                            if (showDebug) {
                                String projectName = TableProjects.getInstance().queryProjectName(incoming.collection_id);
                                String equipmentName = TableEquipment.getInstance().queryEquipmentName(incoming.value_id);
                                Timber.i("New project collection: " + projectName + " <=> " + equipmentName);
                            }
                            TableCollectionEquipmentProject.getInstance().add(incoming);
                        }
                    } else {
                        // Change of IDs. A little weird, but we will allow it.
                        if (!incoming.equals(item)) {
                            if (showDebug) {
                                String projectName = TableProjects.getInstance().queryProjectName(item.collection_id);
                                String equipmentName = TableEquipment.getInstance().queryEquipmentName(item.value_id);
                                Timber.i("Change? " + projectName + " <=> " + equipmentName);
                            }
                            incoming.id = item.id;
                            incoming.server_id = item.server_id;
                            TableCollectionEquipmentProject.getInstance().update(incoming);
                        } else {
                            if (showDebug) {
                                String projectName = TableProjects.getInstance().queryProjectName(item.collection_id);
                                String equipmentName = TableEquipment.getInstance().queryEquipmentName(item.value_id);
                                Timber.i("No change: " + projectName + " <=> " + equipmentName);
                            }
                        }
                        unprocessed.remove(item);
                    }
                }
                for (DataCollectionItem item : unprocessed) {
                    if (showDebug) {
                        String projectName = TableProjects.getInstance().queryProjectName(item.collection_id);
                        String equipmentName = TableEquipment.getInstance().queryEquipmentName(item.value_id);
                        Timber.i("Removing: " + projectName + " <=> " + equipmentName);
                    }
                    TableCollectionEquipmentProject.getInstance().remove(item.id);
                }
                if (showDebug) {
                    if (unprocessed.size() == 0) {
                        Timber.i("No unprocessed items.");
                    }
                }
            }
        } catch (Exception ex) {
            TBApplication.ReportError(ex, DCPing.class, "queryEquipments()", "server");
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
            String response = post(NOTES, true);
            if (response == null) {
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
                    short num_digits = (short) ele.getInt("num_digits");
                    DataNote.Type type = DataNote.Type.from(typeStr);
                    DataNote incoming = new DataNote(name, type, num_digits, server_id);
                    DataNote item = TableNote.getInstance().queryByServerId(server_id);
                    if (item == null) {
                        DataNote match = get(unprocessed, incoming);
                        if (match != null) {
                            // If this name already exists, convert the existing one by simply giving it the server_id.
                            match.serverId = server_id;
                            match.num_digits = num_digits;
                            TableNote.getInstance().update(match);
                            Timber.i("Commandeer local: " + match.toString());
                            unprocessed.remove(match);
                        } else {
                            // Otherwise just add the new entry.
                            TableNote.getInstance().add(incoming);
                            Timber.i("New note: " + incoming.toString());
                        }
                    } else {
                        // Change of name, type and/or num_digits
                        if (!incoming.equals(item)) {
                            incoming.id = item.id;
                            incoming.serverId = item.serverId;
                            TableNote.getInstance().update(incoming);
                            Timber.i("Change: " + incoming.toString());
                        } else {
                            Timber.i("No change: " + item.toString());
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
                        continue;
                    }
                    incoming.collection_id = project.id;
                    DataNote note = TableNote.getInstance().queryByServerId(server_note_id);
                    if (note == null) {
                        Timber.e("queryNotes(): Can't find picture_note with ID " + server_note_id);
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
                            Timber.i("New picture_note collection. " + incoming.collection_id + ", " + incoming.value_id);
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
            TBApplication.ReportError(ex, DCPing.class, "queryNotes()", "server");
        }
    }

    void queryTrucks() {
        Timber.i("queryTrucks()");
        try {
            String response = post(TRUCKS, true);
            if (response == null) {
                return;
            }
            JSONObject object = parseResult(response);
            {
                List<DataTruck> unprocessed = TableTruck.getInstance().query();
                JSONArray array = object.getJSONArray("trucks");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject ele = array.getJSONObject(i);
                    DataTruck incoming = new DataTruck();
                    incoming.serverId = ele.getInt("id");
                    if (ele.has("truck_number_string")) {
                        incoming.truckNumber = ele.getString("truck_number_string");
                    } else if (ele.has("truck_number")) {
                        incoming.truckNumber = Integer.toString(ele.getInt("truck_number"));
                    }
                    if (ele.has("license_plate")) {
                        incoming.licensePlateNumber = ele.getString("license_plate");
                    }
                    if (ele.has("project_id")) {
                        int project_server_id = ele.getInt("project_id");
                        DataProject project = TableProjects.getInstance().queryByServerId(project_server_id);
                        if (project == null) {
                            Timber.e("Can't find any project with server ID " + project_server_id + " for truck number " + incoming.truckNumber);
                        } else {
                            incoming.projectNameId = project.id;
                        }
                    }
                    if (ele.has("company_name")) {
                        incoming.companyName = ele.getString("company_name");
                    }
                    DataTruck item = TableTruck.getInstance().queryByServerId(incoming.serverId);
                    if (item == null) {
                        DataTruck match = get(unprocessed, incoming);
                        if (match != null) {
                            incoming.id = match.id;
                            TableTruck.getInstance().save(incoming);
                            Timber.i("Commandeer local: " + incoming.toLongString());
                            unprocessed.remove(match);
                        } else {
                            // Otherwise just add the new entry.
                            Timber.i("New truck: " + incoming.toLongString());
                            TableTruck.getInstance().save(incoming);
                        }
                    } else {
                        // Change of data
                        if (!incoming.equals(item)) {
                            Timber.i("Change: " + incoming.toLongString());
                            incoming.id = item.id;
                            TableTruck.getInstance().save(incoming);
                        } else {
                            Timber.i("No change: " + item.toLongString());
                        }
                        unprocessed.remove(item);
                    }
                }
                // Remove or disable unprocessed elements
                for (DataTruck truck : unprocessed) {
                    TableTruck.getInstance().removeIfUnused(truck);
                }
            }
        } catch (Exception ex) {
            TBApplication.ReportError(ex, DCPing.class, "queryTrucks()", "server");
        }
    }

    DataTruck get(List<DataTruck> items, DataTruck match) {
        for (DataTruck item : items) {
            if (item.equals(match)) {
                return item;
            }
        }
        return null;
    }

    int sendEntries(List<DataEntry> list) {
        int count = 0;
        for (DataEntry entry : list) {
            if (entry.hasError) {
                PrefHelper.getInstance().setDoErrorCheck(true);
            } else if (sendEntry(entry)) {
                count++;
            } else if (++entry.serverErrorCount > FAILED_UPLOADED_TRIGGER) {
                entry.hasError = true;
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
            jsonObject.accumulate("server_id", entry.serverId);
            DataTruck truck = entry.getTruck();
            if (truck != null) {
                if (truck.truckNumber != null) {
                    jsonObject.accumulate("truck_number", truck.truckNumber);
                }
                if (truck.licensePlateNumber != null) {
                    jsonObject.accumulate("license_plate", truck.licensePlateNumber);
                }
            } else {
                PrefHelper.getInstance().setDoErrorCheck(true);
                Timber.e("sendEntry(): Missing truck entry : " + entry.toString() + " (check error enabled)");
                return false;
            }
            DataProject project = entry.getProject();
            if (project == null) {
                Timber.e("sendEntry(): No project name for entry -- abort");
                return false;
            }
            if (project.serverId > 0) {
                jsonObject.accumulate("project_id", project.serverId);
            } else {
                jsonObject.accumulate("project_name", project.name);
            }
            DataAddress address = entry.getAddress();
            if (address == null) {
                Timber.e("sendEntry(): No address for entry -- abort");
                return false;
            }
            if (address.serverId > 0) {
                jsonObject.accumulate("address_id", address.serverId);
            } else {
                jsonObject.accumulate("address", address.getLine());
            }
            if (entry.status != null && entry.status != TruckStatus.UNKNOWN) {
                jsonObject.accumulate("status", entry.status.toString());
            }
            List<DataEquipment> equipments = entry.getEquipment();
            if (equipments.size() > 0) {
                JSONArray jarray = new JSONArray();
                for (DataEquipment equipment : equipments) {
                    JSONObject jobj = new JSONObject();
                    if (equipment.serverId > 0) {
                        jobj.accumulate("equipment_id", equipment.serverId);
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
                    if (!TextUtils.isEmpty(picture.note)) {
                        jobj.put("note", picture.note);
                    }
                    jarray.put(jobj);
                }
                jsonObject.put("picture", jarray);
            }
            List<DataNote> notes = entry.getNotesWithValuesOnly();
            if (notes.size() > 0) {
                JSONArray jarray = new JSONArray();
                for (DataNote note : notes) {
                    JSONObject jobj = new JSONObject();
                    if (note.serverId > 0) {
                        jobj.put("id", note.serverId);
                    } else {
                        jobj.put("name", note.name);
                    }
                    jobj.put("value", note.value);
                    jarray.put(jobj);
                }
                jsonObject.put("notes", jarray);
            }
            Timber.i("SENDING " + jsonObject.toString());
            String result = post(ENTER, jsonObject, true);
            if (result != null) {
                if (TextUtils.isDigitsOnly(result)) {
                    entry.uploadedMaster = true;
                    entry.serverErrorCount = 0;
                    entry.hasError = false;
                    entry.serverId = Integer.parseInt(result);
                    TableEntry.getInstance().saveUploaded(entry);
                    success = true;
                    Timber.i("SUCCESS, ENTRY SERVER ID is " + entry.serverId);
                } else {
                    StringBuilder sbuf = new StringBuilder();
                    sbuf.append("While trying to send entry: ");
                    sbuf.append(entry.toString());
                    sbuf.append("\nERROR: ");
                    sbuf.append(result);
                    TBApplication.ShowError(sbuf.toString());
                    Timber.e(sbuf.toString());
                }
            }
        } catch (Exception ex) {
            TBApplication.ReportError(ex, DCPing.class, "sendEntry()", "server");
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

    String post(String target, boolean sendErrors) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("device_id", ServerHelper.getInstance().getDeviceId());
            jsonObject.accumulate("tech_id", PrefHelper.getInstance().getTechID());
            return post(target, jsonObject, sendErrors);
        } catch (Exception ex) {
            String msg = "Whle sending to " + target + "\n" + ex.getMessage();
            if (sendErrors) {
                Timber.e(TAG, msg);
            } else {
                Log.e(TAG, msg);
            }
            return null;
        }
    }

    String post(String target, JSONObject json, boolean sendErrors) {
        try {
            URL url = new URL(target);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write(json.toString());
            writer.close();
            String result = getResult(connection);
            connection.disconnect();
            return result;
        } catch (Exception ex) {
            if (sendErrors) {
                TBApplication.ReportServerError(ex, DCPing.class, "post()", "server");
            } else {
                String msg = "While sending to " + target + "\n" + ex.getMessage();
                Log.e(TAG, msg);
                TBApplication.ShowError(msg);
            }
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
            jsonObject.accumulate("message", line.message);
            jsonObject.accumulate("trace", line.trace);
            jsonObject.accumulate("app_version", line.version);
            String result = post(MESSAGE, jsonObject, false);
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

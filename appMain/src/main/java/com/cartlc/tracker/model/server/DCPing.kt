/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.server

import android.content.Context
import android.text.TextUtils
import android.util.Log

import com.cartlc.tracker.BuildConfig
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.data.*
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.misc.TruckStatus
import com.cartlc.tracker.model.event.EventRefreshProjects
import com.cartlc.tracker.model.sql.*
import com.cartlc.tracker.model.table.DatabaseTable

import org.json.JSONArray
import org.json.JSONObject

import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by dug on 8/24/17.
 */

class DCPing(private val context: Context) : DCPost() {

    companion object {

        private val TAG = "DCPing"

        private val SERVER_URL_DEVELOPMENT = "http://fleetdev.arqnetworks.com/"
        private val SERVER_URL_RELEASE = "http://fleettlc.arqnetworks.com/"

        private val UPLOAD_RESET_TRIGGER = "reset_upload"
        private val RE_REGISTER_TRIGGER = "re-register"
        private val RELOAD_CODE = "reload_code"

        // After this many times indicate to the user if there is a problem that needs to be
        // addressed with the tableEntry.
        private val FAILED_UPLOADED_TRIGGER = 5

        private val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'z"
    }

    private val SERVER_URL: String
    private val REGISTER: String
    private val ENTER: String
    private val PING: String
    private val PROJECTS: String
    private val COMPANIES: String
    private val EQUIPMENTS: String
    private val NOTES: String
    private val TRUCKS: String
    private val MESSAGE: String
    private val VEHICLE: String
    private val VEHICLES: String
    private val STRINGS: String
    private var mVersion: String? = null

    @Inject
    lateinit var repo: CarRepository

    private val prefHelper: PrefHelper
        get() = repo.prefHelper

    private val db: DatabaseTable
        get() = repo.db

    private val app: TBApplication
        get() = context.applicationContext as TBApplication

    private val version: String?
        get() {
            if (mVersion == null) {
                try {
                    mVersion = (context.applicationContext as TBApplication).version
                } catch (ex: Exception) {
                    TBApplication.ReportError(ex, DCPing::class.java, "getVersion()", "server")
                }

            }
            return mVersion
        }

    init {
        app.carRepoComponent.inject(this)

        if (prefHelper.isDevelopment) {
            SERVER_URL = SERVER_URL_DEVELOPMENT
        } else {
            SERVER_URL = SERVER_URL_RELEASE
        }
        REGISTER = SERVER_URL + "register"
        ENTER = SERVER_URL + "enter"
        PING = SERVER_URL + "ping"
        PROJECTS = SERVER_URL + "projects"
        COMPANIES = SERVER_URL + "companies"
        EQUIPMENTS = SERVER_URL + "equipments"
        NOTES = SERVER_URL + "notes"
        MESSAGE = SERVER_URL + "message"
        TRUCKS = SERVER_URL + "trucks"
        VEHICLE = SERVER_URL + "vehicle"
        VEHICLES = SERVER_URL + "vehicles"
        STRINGS = SERVER_URL + "strings"
    }

    fun sendRegistration() {
        Timber.i("sendRegistration()")
        try {
            val deviceId = ServerHelper.instance.deviceId
            val jsonObject = JSONObject()
            jsonObject.accumulate("first_name", prefHelper.firstName)
            jsonObject.accumulate("last_name", prefHelper.lastName)
            if (prefHelper.isSecondaryEnabled && prefHelper.hasSecondaryName) {
                jsonObject.accumulate("secondary_first_name", prefHelper.secondaryFirstName)
                jsonObject.accumulate("secondary_last_name", prefHelper.secondaryLastName)
            }
            jsonObject.accumulate("device_id", deviceId)
            val result = post(REGISTER, jsonObject, true)
            if (result != null) {
                if (parseRegistrationResult(result)) {
                    prefHelper.registrationHasChanged = false
                }
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "sendRegistration()", "server")
        }
    }

    private fun parseRegistrationResult(result: String): Boolean {
        try {
            if (result.contains(":")) {
                val pos = result.indexOf(':')
                var word = result.substring(0, pos)
                var tech_id = Integer.parseInt(word)
                prefHelper.techID = tech_id
                Timber.i("TECH ID=$tech_id")

                word = result.substring(pos + 1)
                tech_id = Integer.parseInt(word)
                prefHelper.secondaryTechID = tech_id
                Timber.i("SECONDARY TECH ID=$tech_id")
                return true
            }
            if (TextUtils.isDigitsOnly(result)) {
                val tech_id = Integer.parseInt(result)
                prefHelper.techID = tech_id
                prefHelper.secondaryTechID = 0
                Timber.i("TECH ID=$tech_id")
                return true
            }
            Timber.e("sendRegistration() failed on: $result")
        } catch (ex: NumberFormatException) {
            Timber.e("sendRegistration(): PARSE ERROR on: $result")
        }
        return false
    }

    @Synchronized
    fun ping() {
        Timber.i("ping()")
        if (prefHelper.techID == 0) {
            return
        }
        val deviceId = ServerHelper.instance.deviceId
        val jsonObject = JSONObject()
        jsonObject.accumulate("device_id", deviceId)
        jsonObject.accumulate("tech_id", prefHelper.techID)
        jsonObject.accumulate("app_version", version)
        val response = post(PING, jsonObject, true) ?: return
        val blob = parseResult(response)
        if (blob.has(UPLOAD_RESET_TRIGGER)) {
            if (blob.getBoolean(UPLOAD_RESET_TRIGGER)) {
                Timber.i("UPLOAD RESET!")
                repo.clearUploaded()
            }
        }
        if (blob.has(RE_REGISTER_TRIGGER)) {
            if (blob.getBoolean(RE_REGISTER_TRIGGER)) {
                Timber.i("RE-REGISTER DETECTED!")
                sendRegistration()
            }
        }
        if (blob.has(RELOAD_CODE)) {
            val reload_code = blob.getString(RELOAD_CODE)
            if (!TextUtils.isEmpty(reload_code)) {
                if (reload_code.contains("p")) {
                    prefHelper.versionProject = 0
                }
                if (reload_code.contains("e")) {
                    prefHelper.versionEquipment = 0
                }
                if (reload_code.contains("n")) {
                    prefHelper.versionNote = 0
                }
                if (reload_code.contains("c")) {
                    prefHelper.versionCompany = 0
                }
                if (reload_code.contains("t")) {
                    prefHelper.versionTruck = 0
                }
                if (reload_code.contains("v")) {
                    prefHelper.versionVehicleNames = 0
                }
            }
        }
        val version_project = blob.getInt(PrefHelper.VERSION_PROJECT)
        val version_equipment = blob.getInt(PrefHelper.VERSION_EQUIPMENT)
        val version_note = blob.getInt(PrefHelper.VERSION_NOTE)
        val version_company = blob.getInt(PrefHelper.VERSION_COMPANY)
        val version_truck = blob.getInt(PrefHelper.VERSION_TRUCK)
        val version_vehicle_names = blob.getInt(PrefHelper.VERSION_VEHICLE_NAMES)
        if (prefHelper.versionProject != version_project) {
            Timber.i("New project version $version_project")
            if (!queryProjects()) {
                return
            }
            prefHelper.versionProject = version_project
            EventBus.getDefault().post(EventRefreshProjects())
        }
        if (prefHelper.versionVehicleNames != version_vehicle_names) {
            Timber.i("New vehicle name version $version_vehicle_names")
            if (!queryVehicleNames()) {
                return
            }
            prefHelper.versionVehicleNames = version_vehicle_names
        }
        if (prefHelper.versionCompany != version_company) {
            Timber.i("New company version $version_company")
            if (!queryCompanies()) {
                return
            }
            prefHelper.versionCompany = version_company
        }
        if (prefHelper.versionEquipment != version_equipment) {
            Timber.i("New equipment version $version_equipment")
            if (!queryEquipments()) {
                return
            }
            prefHelper.versionEquipment = version_equipment
        }
        if (prefHelper.versionNote != version_note) {
            Timber.i("New note version $version_note")
            if (!queryNotes()) {
                return
            }
            prefHelper.versionNote = version_note
        }
        if (prefHelper.versionTruck != version_truck) {
            Timber.i("New truck version $version_truck")
            if (!queryTrucks()) {
                return
            }
            prefHelper.versionTruck = version_truck
        }
        var entries = db.tableEntry.queryPendingDataToUploadToMaster()
        var count = 0
        if (entries.isNotEmpty()) {
            count = sendEntries(entries)
        }
        if (count > 0) {
            EventBus.getDefault().post(EventRefreshProjects())
        }
        entries = db.tableEntry.queryPendingPicturesToUpload()
        if (entries.size > 0) {
            if (AmazonHelper.instance.sendPictures(context, entries)) {
                EventBus.getDefault().post(EventRefreshProjects())
            }
        }
        db.tablePictureCollection.clearUploadedUnscaledPhotos()
        val lines = db.tableCrash.queryNeedsUploading()
        sendCrashLines(lines)
        // If any entries do not yet have server-id's, try to get them.
        entries = db.tableEntry.queryServerIds()
        if (entries.isNotEmpty()) {
            Timber.i("FOUND " + entries.size + " entries needing to be uploaded")
            sendEntries(entries)
        } else {
            Timber.i("All entries have server ids")
        }
        val vehicles = db.tableVehicle.queryNotUploaded()
        if (vehicles.isNotEmpty()) {
            Timber.i("FOUND " + vehicles.size + " vehicles needing to be uploaded")
            sendVehicles(vehicles)
        } else {
            Timber.i("All vehicles have uploaded")
        }
        val strings = db.tableString.queryNotUploaded()
        if (strings.isNotEmpty()) {
            Timber.i("Query strings needed.")
            queryStrings()
        }
    }

    private fun queryProjects(): Boolean {
        Timber.i("queryProjects()")
        try {
            val response = post(PROJECTS, true)
            if (response == null) {
                Timber.e("queryProjects(): Unexpected NULL response from server")
                return false
            }
            val unprocessed = db.tableProjects.query().toMutableList()
            val obj = parseResult(response)
            val array = obj.getJSONArray("projects")
            for (i in 0 until array.length()) {
                val ele = array.getJSONObject(i)
                val server_id = ele.getInt("id")
                val name = ele.getString("name")
                val disabled = ele.getBoolean("disabled")
                val project = db.tableProjects.queryByServerId(server_id)
                if (project == null) {
                    if (TextUtils.isEmpty(name)) {
                        TBApplication.ReportError("Got empty project name from server", DCPing::class.java, "queryProjects()", "server")
                    } else if (unprocessed.contains(name)) {
                        // If this name already exists, convert the existing one by simply giving it the server_id.
                        val existing = db.tableProjects.queryByName(name)
                        existing!!.serverId = server_id
                        existing.isBootStrap = false
                        existing.disabled = disabled
                        db.tableProjects.update(existing)
                        Timber.i("Commandeer local: $name")
                    } else {
                        // Otherwise just add the new project.
                        Timber.i("New project: $name")
                        db.tableProjects.add(name, server_id, disabled)
                    }
                } else {
                    // Name change?
                    if (name != project.name) {
                        Timber.i("New name: $name")
                        project.name = name
                        project.disabled = disabled
                        db.tableProjects.update(project)
                    } else if (project.disabled != disabled) {
                        Timber.i("Project " + name + " " + if (disabled) "disabled" else "enabled")
                        project.disabled = disabled
                        db.tableProjects.update(project)
                    } else {
                        Timber.i("No change: $name")
                    }
                }
                unprocessed.remove(name)
            }
            // Remaining unprocessed elements are disabled if they have entries.
            for (name in unprocessed) {
                val existing = db.tableProjects.queryByName(name)
                if (existing != null) {
                    Timber.i("Project disable or delete: $name")
                    db.tableProjects.removeOrDisable(existing)
                }
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryProjects()", "server")
            return false
        }
        return true
    }

    private fun queryCompanies(): Boolean {
        Timber.i("queryCompanies()")
        try {
            val response = post(COMPANIES, true) ?: return false
            val unprocessed = db.tableAddress.query().toMutableList()
            val obj = parseResult(response)
            val array = obj.getJSONArray("companies")
            var name: String
            var street: String?
            var city: String?
            var state: String?
            var zipcode: String?
            for (i in 0 until array.length()) {
                val ele = array.getJSONObject(i)
                val server_id = ele.getInt("id")
                name = ele.getString("name")

                if (name.isBlank()) {
                    TBApplication.ReportError("Got empty company name", DCPing::class.java, "queryCompanies()", "server")
                    continue
                }
                if (ele.has("street") && ele.has("city") && ele.has("state")) {
                    street = ele.getString("street")
                    city = ele.getString("city")
                    state = ele.getString("state")
                } else {
                    TBApplication.ReportError("Missing street, city, or state for company $name, server id $server_id", DCPing::class.java, "queryCompanies()", "server")
                    continue
                }
                if (ele.has("zipcode")) {
                    zipcode = ele.getString("zipcode")
                } else {
                    zipcode = null
                }
                val incoming = DataAddress(server_id, name, street, city, state, zipcode)
                val item = db.tableAddress.queryByServerId(server_id)
                if (item == null) {
                    val match = get(unprocessed, incoming)
                    if (match != null) {
                        // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                        match.serverId = server_id
                        match.isLocal = false
                        match.isBootStrap = false
                        db.tableAddress.update(match)
                        Timber.i("Commandeer local: " + match.toString())
                        unprocessed.remove(match)
                    } else {
                        // Otherwise just add the new tableEntry.
                        db.tableAddress.add(incoming)
                        Timber.i("New company: " + incoming.toString())
                    }
                } else {
                    // Change of name, street, city or state?
                    if (!incoming.equals(item) || incoming.isLocal != item.isLocal) {
                        incoming.id = item.id
                        incoming.serverId = item.serverId
                        incoming.isLocal = false
                        Timber.i("Change: " + incoming.toString())
                        db.tableAddress.update(incoming)
                    } else {
                        Timber.i("No change: " + incoming.toString())
                    }
                    unprocessed.remove(item)
                }
            }
            // Remaining unprocessed elements are disabled if they have entries.
            for (item in unprocessed) {
                db.tableAddress.removeOrDisable(item)
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryCompanies()", "server")
            return false
        }
        return true
    }

    private operator fun get(items: List<DataAddress>, match: DataAddress): DataAddress? {
        for (item in items) {
            if (item.equals(match)) {
                return item
            }
        }
        return null
    }

    private fun queryEquipments(): Boolean {
        val showDebug = BuildConfig.DEBUG
        Timber.i("queryEquipments()")
        try {
            val response = post(EQUIPMENTS, true) ?: return false
            val blob = parseResult(response)
            run {
                val unprocessed = db.tableEquipment.query().toMutableList()
                val array = blob.getJSONArray("equipments")
                for (i in 0 until array.length()) {
                    val ele = array.getJSONObject(i)
                    val server_id = ele.getInt("id")
                    val name = ele.getString("name")
                    val incoming = DataEquipment(name, server_id)
                    val item = db.tableEquipment.queryByServerId(server_id)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            // If this name already exists, convert the existing one by simply giving it the server_id.
                            match.serverId = server_id.toLong()
                            match.isBootStrap = false
                            match.isLocal = false
                            db.tableEquipment.update(match)
                            Timber.i("Commandeer local: $name")
                            unprocessed.remove(match)
                        } else {
                            // Otherwise just add the new tableEntry.
                            Timber.i("New equipment: $name")
                            db.tableEquipment.add(incoming)
                        }
                    } else {
                        // Change of name
                        if (!incoming.equals(item)) {
                            Timber.i("Change: $name")
                            incoming.id = item.id
                            incoming.serverId = item.serverId
                            incoming.isLocal = false
                            db.tableEquipment.update(incoming)
                        } else {
                            Timber.i("No change: $name")
                        }
                        unprocessed.remove(item)
                    }
                }
                // Remaining unprocessed elements are disabled if they have entries.
                for (item in unprocessed) {
                    db.tableEquipment.removeOrDisable(item)
                }
            }
            run {
                val unprocessed = db.tableCollectionEquipmentProject.query().toMutableList()
                val array = blob.getJSONArray("project_equipment")
                for (i in 0 until array.length()) {
                    val ele = array.getJSONObject(i)
                    val server_id = ele.getInt("id")
                    val server_project_id = ele.getInt("project_id")
                    val server_equipment_id = ele.getInt("equipment_id")
                    val incoming = DataCollectionItem()
                    incoming.server_id = server_id
                    // Note: project ID is from the perspective of the server, not the APP.
                    val project = db.tableProjects.queryByServerId(server_project_id)
                    val equipment = db.tableEquipment.queryByServerId(server_equipment_id)
                    if (project == null || equipment == null) {
                        if (project == null && equipment == null) {
                            Timber.e("Can't find any project with server ID $server_project_id nor equipment ID $server_equipment_id")
                            prefHelper.reloadProjects()
                            prefHelper.reloadEquipments()
                        } else if (project == null) {
                            val sbuf = StringBuilder()
                            sbuf.append("Can't find any project with server ID ")
                            sbuf.append(server_project_id)
                            sbuf.append(" for tableEquipment ")
                            sbuf.append(equipment!!.name)
                            sbuf.append(". Projects=")
                            for (name in db.tableProjects.query()) {
                                sbuf.append(name)
                                sbuf.append(":")
                            }
                            sbuf.append(".")
                            Timber.e(sbuf.toString())
                            prefHelper.reloadProjects()
                        } else {
                            Timber.e("Can't find any equipment with server ID " + server_equipment_id + " for project " + project.name)
                            prefHelper.reloadEquipments()
                        }
                        continue
                    }
                    incoming.collection_id = project.id
                    incoming.value_id = equipment.id
                    val item = db.tableCollectionEquipmentProject.queryByServerId(server_id)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                            match.server_id = server_id
                            match.isBootstrap = false
                            db.tableCollectionEquipmentProject.update(match)
                            if (showDebug) {
                                val projectName = db.tableProjects.queryProjectName(match.collection_id)
                                val equipmentName = db.tableEquipment.queryEquipmentName(match.value_id)
                                Timber.i("Commandeer local: PROJECT COLLECTION $projectName <=> $equipmentName")
                            }
                            unprocessed.remove(match)
                        } else {
                            // Otherwise just add the new tableEntry.
                            if (showDebug) {
                                val projectName = db.tableProjects.queryProjectName(incoming.collection_id)
                                val equipmentName = db.tableEquipment.queryEquipmentName(incoming.value_id)
                                Timber.i("New project collection: $projectName <=> $equipmentName")
                            }
                            db.tableCollectionEquipmentProject.add(incoming)
                        }
                    } else {
                        // Change of IDs. A little weird, but we will allow it.
                        if (!incoming.equals(item)) {
                            if (showDebug) {
                                val projectName = db.tableProjects.queryProjectName(item.collection_id)
                                val equipmentName = db.tableEquipment.queryEquipmentName(item.value_id)
                                Timber.i("Change? $projectName <=> $equipmentName")
                            }
                            incoming.id = item.id
                            incoming.server_id = item.server_id
                            db.tableCollectionEquipmentProject.update(incoming)
                        } else {
                            if (showDebug) {
                                val projectName = db.tableProjects.queryProjectName(item.collection_id)
                                val equipmentName = db.tableEquipment.queryEquipmentName(item.value_id)
                                Timber.i("No change: $projectName <=> $equipmentName")
                            }
                        }
                        unprocessed.remove(item)
                    }
                }
                for (item in unprocessed) {
                    if (showDebug) {
                        val projectName = db.tableProjects.queryProjectName(item.collection_id)
                        val equipmentName = db.tableEquipment.queryEquipmentName(item.value_id)
                        Timber.i("Removing: $projectName <=> $equipmentName")
                    }
                    db.tableCollectionEquipmentProject.remove(item.id)
                }
                if (showDebug) {
                    if (unprocessed.size == 0) {
                        Timber.i("No unprocessed items.")
                    }
                }
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryEquipments()", "server")
            return false
        }
        return true
    }

    private operator fun get(items: List<DataEquipment>, match: DataEquipment): DataEquipment? {
        for (item in items) {
            if (item.equals(match)) {
                return item
            }
        }
        return null
    }

    private operator fun get(items: List<DataCollectionItem>, match: DataCollectionItem): DataCollectionItem? {
        for (item in items) {
            if (item.equals(match)) {
                return item
            }
        }
        return null
    }

    private fun queryNotes(): Boolean {
        Timber.i("queryNotes()")
        try {
            val response = post(NOTES, true) ?: return false
            val obj = parseResult(response)
            run {
                val unprocessed = db.tableNote.query().toMutableList()
                val array = obj.getJSONArray("notes")
                for (i in 0 until array.length()) {
                    val ele = array.getJSONObject(i)
                    val server_id = ele.getInt("id")
                    val name = ele.getString("name")
                    val typeStr = ele.getString("type")
                    val num_digits = ele.getInt("num_digits").toShort()
                    val type = DataNote.Type.from(typeStr)
                    val incoming = DataNote(name, type, num_digits, server_id)
                    val item = db.tableNote.queryByServerId(server_id)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            // If this name already exists, convert the existing one by simply giving it the server_id.
                            match.serverId = server_id
                            match.num_digits = num_digits
                            db.tableNote.update(match)
                            Timber.i("Commandeer local: " + match.toString())
                            unprocessed.remove(match)
                        } else {
                            // Otherwise just add the new tableEntry.
                            db.tableNote.add(incoming)
                            Timber.i("New note: " + incoming.toString())
                        }
                    } else {
                        // Change of name, type and/or num_digits
                        if (incoming != item) {
                            incoming.id = item.id
                            incoming.serverId = item.serverId
                            db.tableNote.update(incoming)
                            Timber.i("Change: " + incoming.toString())
                        } else {
                            Timber.i("No change: " + item.toString())
                        }
                        unprocessed.remove(item)
                    }
                }
                // Remove or disable unprocessed elements
                for (note in unprocessed) {
                    db.tableNote.removeIfUnused(note)
                }
            }
            run {
                val unprocessed = db.tableCollectionNoteProject.query().toMutableList()
                val array = obj.getJSONArray("project_note")
                for (i in 0 until array.length()) {
                    val ele = array.getJSONObject(i)
                    val server_id = ele.getInt("id")
                    val server_project_id = ele.getInt("project_id")
                    val server_note_id = ele.getInt("note_id")
                    val incoming = DataCollectionItem()
                    incoming.server_id = server_id
                    // Note: project ID is from the perspective of the server, not the APP.
                    val project = db.tableProjects.queryByServerId(server_project_id) ?: continue
                    incoming.collection_id = project.id
                    val note = db.tableNote.queryByServerId(server_note_id)
                    if (note == null) {
                        Timber.e("queryNotes(): Can't find picture_note with ID $server_note_id")
                        continue
                    }
                    incoming.value_id = note.id
                    val item = db.tableCollectionNoteProject.queryByServerId(server_id)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                            match.server_id = server_id
                            db.tableCollectionNoteProject.update(match)
                            Timber.i("Commandeer local: NOTE COLLECTION " + match.collection_id + ", " + match.value_id)
                            unprocessed.remove(match)
                        } else {
                            // Otherwise just add the new tableEntry.
                            Timber.i("New picture_note collection. " + incoming.collection_id + ", " + incoming.value_id)
                            db.tableCollectionNoteProject.add(incoming)
                        }
                    } else {
                        // Change of IDs. A little weird, but we will allow it.
                        if (!incoming.equals(item)) {
                            Timber.i("Change? " + item.collection_id + ", " + item.value_id)
                            incoming.id = item.id
                            incoming.server_id = item.server_id
                            db.tableCollectionNoteProject.update(incoming)
                        }
                    }
                }
                for (item in unprocessed) {
                    db.tableCollectionNoteProject.removeIfGone(item)
                }
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryNotes()", "server")
            return false
        }
        return true
    }

    private fun queryTrucks(): Boolean {
        Timber.i("queryTrucks()")
        try {
            val response = post(TRUCKS, true) ?: return false
            val obj = parseResult(response)
            run {
                val unprocessed = db.tableTruck.query().toMutableList()
                val array = obj.getJSONArray("trucks")
                for (i in 0 until array.length()) {
                    val ele = array.getJSONObject(i)
                    val incoming = DataTruck()
                    incoming.serverId = ele.getLong("id")
                    if (ele.has("truck_number_string")) {
                        incoming.truckNumber = ele.getString("truck_number_string")
                    } else if (ele.has("truck_number")) {
                        incoming.truckNumber = Integer.toString(ele.getInt("truck_number"))
                    }
                    if (ele.has("license_plate")) {
                        incoming.licensePlateNumber = ele.getString("license_plate")
                    }
                    if (ele.has("project_id")) {
                        val project_server_id = ele.getInt("project_id")
                        val project = db.tableProjects.queryByServerId(project_server_id)
                        if (project == null) {
                            Timber.e("Can't find any project with server ID " + project_server_id + " for truck number " + incoming.truckNumber)
                        } else {
                            incoming.projectNameId = project.id
                        }
                    }
                    if (ele.has("company_name")) {
                        incoming.companyName = ele.getString("company_name")
                    }
                    if (ele.has("has_entries")) {
                        incoming.hasEntry = ele.getBoolean("has_entries")
                    }
                    val item = db.tableTruck.queryByServerId(incoming.serverId)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            incoming.id = match.id
                            db.tableTruck.save(incoming)
                            Timber.i("Commandeer local truck: " + incoming.toLongString(db))
                            unprocessed.removeAll { it.id == match.id }
                        } else {
                            // Otherwise just add the new truck.
                            Timber.i("New truck: " + incoming.toLongString(db))
                            db.tableTruck.save(incoming)
                        }
                    } else {
                        // Change of data
                        if (!incoming.equals(item)) {
                            Timber.i("Change: [" + incoming.toLongString(db) + "] from [" + item.toLongString(db) + "]")
                            incoming.id = item.id
                            db.tableTruck.save(incoming)
                        } else {
                            Timber.i("No change: " + item.toLongString(db))
                        }
                        unprocessed.removeAll { it.id == item.id }
                    }
                }
                // Remove or disable unprocessed elements
                for (truck in unprocessed) {
                    db.tableTruck.removeIfUnused(truck)
                }
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryTrucks()", "server")
            return false
        }
        return true
    }

    private operator fun get(items: List<DataTruck>, match: DataTruck): DataTruck? {
        for (item in items) {
            if (item.equals(match)) {
                return item
            }
        }
        return null
    }

    private fun queryVehicleNames(): Boolean {
        Timber.i("queryVehicleNames()")
        try {
            val response = post(VEHICLES, true) ?: return false
            val obj = parseResult(response)
            run {
                val unprocessed = db.tableVehicleName.query().toMutableList()
                val array = obj.getJSONArray("names")
                for (i in 0 until array.length()) {
                    val ele = array.getJSONObject(i)
                    val incoming = DataVehicleName()
                    incoming.name = ele.getString("name")
                    incoming.number = ele.getInt("number")
                    val item = db.tableVehicleName.queryByNumber(incoming.number)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            incoming.id = match.id
                            db.tableVehicleName.save(incoming)
                            Timber.i("Commandeer local vehicle name: " + incoming.toString())
                            unprocessed.removeAll { it.id == match.id }
                        } else {
                            // Otherwise just add the new vehicle name.
                            Timber.i("New vehicle name: " + incoming.toString())
                            db.tableVehicleName.save(incoming)
                        }
                    } else {
                        // Change of data
                        if (incoming != item) {
                            Timber.i("Change: [" + incoming.toString() + "] from [" + item.toString() + "]")
                            incoming.id = item.id
                            db.tableVehicleName.save(incoming)
                        } else {
                            Timber.i("No change: " + item.toString())
                        }
                        unprocessed.removeAll { it.id == item.id }
                    }
                }
                // Remove or disable unprocessed elements
                for (item in unprocessed) {
                    db.tableVehicleName.remove(item)
                }
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryTrucks()", "server")
            return false
        }
        return true
    }

    private operator fun get(items: List<DataVehicleName>, match: DataVehicleName): DataVehicleName? {
        for (item in items) {
            if (item.name.equals(match.name)) {
                return item
            }
        }
        return null
    }

    private fun sendEntries(list: List<DataEntry>): Int {
        var count = 0
        for (entry in list) {
            if (entry.hasError) {
                prefHelper.doErrorCheck = true
            } else if (sendEntry(entry)) {
                count++
            } else {
                entry.serverErrorCount = (entry.serverErrorCount + 1).toShort()
                if (entry.serverErrorCount > FAILED_UPLOADED_TRIGGER) {
                    entry.hasError = true
                }
            }
        }
        return count
    }

    private fun sendEntry(entry: DataEntry): Boolean {
        var success = false
        Timber.i("sendEntry(" + entry.id + ")")
        try {
            val jsonObject = JSONObject()
            jsonObject.accumulate("tech_id", prefHelper.techID)
            if (prefHelper.isSecondaryEnabled && prefHelper.secondaryTechID > 0) {
                jsonObject.accumulate("secondary_tech_id", prefHelper.secondaryTechID)
            }
            jsonObject.accumulate("date_string", entry.dateString)
            jsonObject.accumulate("server_id", entry.serverId)

            val truck = entry.truck
            if (truck != null) {
                if (truck.serverId > 0) {
                    jsonObject.accumulate("truck_id", truck.serverId)
                }
                if (truck.truckNumber != null) {
                    jsonObject.accumulate("truck_number_string", truck.truckNumber)
                }
                if (truck.licensePlateNumber != null) {
                    jsonObject.accumulate("license_plate", truck.licensePlateNumber)
                }
            } else {
                prefHelper.doErrorCheck = true
                Timber.e("sendEntry(): Missing truck entry : " + entry.toLongString(db) + " (check error enabled)")
                return false
            }
            val project = entry.project
            if (project == null) {
                Timber.e("sendEntry(): No project name for entry -- abort")
                return false
            }
            if (project.serverId > 0) {
                jsonObject.accumulate("project_id", project.serverId)
            } else {
                jsonObject.accumulate("project_name", project.name)
            }
            val address = entry.address
            if (address == null) {
                Timber.e("sendEntry(): No address for entry -- abort")
                return false
            }
            if (address.serverId > 0) {
                jsonObject.accumulate("address_id", address.serverId)
            } else {
                jsonObject.accumulate("address", address.line)
            }
            if (entry.status != null && entry.status !== TruckStatus.UNKNOWN) {
                jsonObject.accumulate("status", entry.status!!.toString())
            }
            val equipments = entry.equipment
            if (equipments!!.size > 0) {
                val jarray = JSONArray()
                for (equipment in equipments) {
                    val jobj = JSONObject()
                    if (equipment.serverId > 0) {
                        jobj.accumulate("equipment_id", equipment.serverId)
                    } else {
                        jobj.accumulate("equipment_name", equipment.name)
                    }
                    jarray.put(jobj)
                }
                jsonObject.put("equipment", jarray)
            }
            val pictures = entry.pictures
            if (pictures.size > 0) {
                val jarray = JSONArray()
                for (picture in pictures) {
                    val jobj = JSONObject()
                    jobj.put("filename", picture.tailname)
                    if (!TextUtils.isEmpty(picture.note)) {
                        jobj.put("note", picture.note)
                    }
                    jarray.put(jobj)
                }
                jsonObject.put("picture", jarray)
            }
            val notes = entry.notesWithValuesOnly
            if (notes.size > 0) {
                val jarray = JSONArray()
                for (note in notes) {
                    val jobj = JSONObject()
                    if (note.serverId > 0) {
                        jobj.put("id", note.serverId)
                    } else {
                        jobj.put("name", note.name)
                    }
                    jobj.put("value", note.value)
                    jarray.put(jobj)
                }
                jsonObject.put("notes", jarray)
            }
            Timber.i("SENDING " + jsonObject.toString())
            val result = post(ENTER, jsonObject, true)
            if (result != null) {
                if (TextUtils.isDigitsOnly(result)) {
                    entry.uploadedMaster = true
                    entry.serverErrorCount = 0.toShort()
                    entry.hasError = false
                    entry.serverId = Integer.parseInt(result)
                    db.tableEntry.saveUploaded(entry)
                    success = true
                    Timber.i("SUCCESS, ENTRY SERVER ID is " + entry.serverId)
                } else {
                    val sbuf = StringBuilder()
                    sbuf.append("While trying to send entry: ")
                    sbuf.append(entry.toLongString(db))
                    sbuf.append("\nERROR: ")
                    sbuf.append(result)
                    TBApplication.ShowError(sbuf.toString())
                    Timber.e(sbuf.toString())
                }
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "sendEntry()", "server")
            return false
        }
        return success
    }

    private fun sendVehicles(list: List<DataVehicle>) {
        for (vehicle in list) {
            sendVehicle(vehicle)
        }
        return
    }

    private fun sendVehicle(vehicle: DataVehicle) {
        Timber.i("sendVehicle(" + vehicle.id + ")")
        try {
            val dateString = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date(System.currentTimeMillis()))
            val jsonObject = JSONObject()
            jsonObject.accumulate("tech_id", prefHelper.techID)
            jsonObject.accumulate("date_string", dateString)
            jsonObject.accumulate("server_id", vehicle.serverId)
            jsonObject.accumulate("inspecting", vehicle.inspectingValue)
            jsonObject.accumulate("type_of_inspection", vehicle.typeOfInspectionValue)
            jsonObject.accumulate("mileage", vehicle.mileage)
            jsonObject.accumulate("head_lights", vehicle.headLights.serverMash())
            jsonObject.accumulate("tail_lights", vehicle.tailLights.serverMash())
            jsonObject.accumulate("exterior_light_issues", vehicle.exteriorLightIssues)
            jsonObject.accumulate("fluid_checks", vehicle.fluidChecks.serverMash())
            jsonObject.accumulate("fluid_problems_detected", vehicle.fluidProblemsDetected)
            jsonObject.accumulate("tire_inspection", vehicle.tireInspection.serverMash())
            jsonObject.accumulate("exterior_damage", vehicle.exteriorDamage)
            jsonObject.accumulate("other", vehicle.other)

            Timber.i("SENDING " + jsonObject.toString())
            val result = post(VEHICLE, jsonObject, true)
            if (result != null) {
                if (TextUtils.isDigitsOnly(result)) {
                    vehicle.uploaded = true
                    vehicle.serverId = result.toLong()
                    db.tableVehicle.saveUploaded(vehicle)
                    Timber.i("SUCCESS, VEHICLE SERVER ID is " + vehicle.serverId)
                } else {
                    val sbuf = StringBuilder()
                    sbuf.append("While trying to send vehicle: ")
                    sbuf.append(vehicle.toString())
                    sbuf.append("\nERROR: ")
                    sbuf.append(result)
                    TBApplication.ShowError(sbuf.toString())
                    Timber.e(sbuf.toString())
                }
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "sendVehicle()", "server")
        }
    }

    private fun queryStrings() {
        Timber.i("queryStrings()")
        try {
            val response = post(STRINGS, true) ?: return
            val obj = parseResult(response)
            val array = obj.getJSONArray("strings")
            for (i in 0 until array.length()) {
                val ele = array.getJSONObject(i)
                val incoming = DataString()
                incoming.serverId = ele.getLong("id")
                incoming.value = ele.getString("value")
                val item = db.tableString.queryByServerId(incoming.serverId)
                if (item == null) {
                    db.tableString.save(incoming)
                } else {
                    // Change of data
                    if (!incoming.equals(item)) {
                        Timber.i("Change: [" + incoming.toString() + "] from [" + item.toString() + "]")
                        incoming.id = item.id
                        db.tableString.save(incoming)
                    } else {
                        Timber.i("No change: " + item.toString())
                    }
                }
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryTrucks()", "server")
        }
    }

    private operator fun get(items: List<DataNote>, match: DataNote): DataNote? {
        for (item in items) {
            if (item == match) {
                return item
            }
        }
        return null
    }

    private fun post(target: String, sendErrors: Boolean): String? {
        try {
            val jsonObject = JSONObject()
            jsonObject.accumulate("device_id", ServerHelper.instance.deviceId)
            jsonObject.accumulate("tech_id", prefHelper.techID)
            return post(target, jsonObject, sendErrors)
        } catch (ex: Exception) {
            val msg = "While sending to " + target + "\n" + ex.message
            if (sendErrors) {
                Timber.e("$TAG:$msg")
            } else {
                Log.e(TAG, msg)
            }
            return null
        }
    }

    private fun post(target: String, json: JSONObject, sendErrors: Boolean): String? {
        try {
            val url = URL(target)
            val connection = url.openConnection() as HttpURLConnection
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            val stream = OutputStreamWriter(connection.outputStream, "UTF-8")
            val writer = BufferedWriter(stream!!)
            writer.write(json.toString())
            writer.close()
            val result = getResult(connection)
            connection.disconnect()
            return result
        } catch (ex: Exception) {
            if (sendErrors) {
                TBApplication.ReportServerError(ex, DCPing::class.java, "post()", "server")
            } else {
                val msg = "While sending to " + target + "\n" + ex.message
                Log.e(TAG, msg)
                TBApplication.ShowError(msg)
            }
            return null
        }
    }

    private fun parseResult(result: String): JSONObject {
        try {
            return JSONObject(result)
        } catch (ex: Exception) {
            Timber.e("Got bad result back from server: " + result + "\n" + ex.message)
        }
        return JSONObject()
    }

    private fun sendCrashLines(lines: List<SqlTableCrash.CrashLine>) {
        for (line in lines) {
            sendCrashLine(line)
        }
    }

    private fun sendCrashLine(line: SqlTableCrash.CrashLine) {
        try {
            val jsonObject = JSONObject()
            jsonObject.accumulate("tech_id", prefHelper.techID)
            jsonObject.accumulate("date", line.date)
            jsonObject.accumulate("code", line.code)
            jsonObject.accumulate("message", line.message)
            jsonObject.accumulate("trace", line.trace)
            jsonObject.accumulate("app_version", line.version)
            val result = post(MESSAGE, jsonObject, false)
            if (result != null && Integer.parseInt(result) == 0) {
                db.tableCrash.setUploaded(line)
            } else {
                Log.e(TAG, "Unable to send previously trapped message: " + line.message!!)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Exception: " + ex.message)
        }
    }

}

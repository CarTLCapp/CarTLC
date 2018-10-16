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
import javax.inject.Inject

/**
 * Created by dug on 8/24/17.
 */

class DCPing(internal val context: Context) : DCPost() {

    companion object {

        internal val TAG = "DCPing"

        internal val SERVER_URL_DEVELOPMENT = "http://fleetdev.arqnetworks.com/"
        internal val SERVER_URL_RELEASE = "http://fleettlc.arqnetworks.com/"

        internal val UPLOAD_RESET_TRIGGER = "reset_upload"
        internal val RE_REGISTER_TRIGGER = "re-register"
        internal val RELOAD_CODE = "reload_code"

        // After this many times indicate to the user if there is a problem that needs to be
        // addressed with the entry.
        internal val FAILED_UPLOADED_TRIGGER = 5
    }

    internal val SERVER_URL: String
    internal val REGISTER: String
    internal val ENTER: String
    internal val PING: String
    internal val PROJECTS: String
    internal val COMPANIES: String
    internal val EQUIPMENTS: String
    internal val NOTES: String
    internal val TRUCKS: String
    internal val MESSAGE: String
    internal var mVersion: String? = null

    @Inject
    lateinit var repo: CarRepository

    private val prefHelper: PrefHelper
        get() = repo.prefHelper

    private val db: DatabaseTable
        get() = repo.db

    private val app: TBApplication
        get() = context.applicationContext as TBApplication

    internal val version: String?
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
        app.appComponent.inject(this)

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
    }

    fun sendRegistration() {
        Timber.i("sendRegistration()")
        try {
            val deviceId = ServerHelper.instance.deviceId
            val jsonObject = JSONObject()
            jsonObject.accumulate("first_name", prefHelper.firstName)
            jsonObject.accumulate("last_name", prefHelper.lastName)
            if (prefHelper.isSecondaryEnabled && prefHelper.hasSecondaryName()) {
                jsonObject.accumulate("secondary_first_name", prefHelper.secondaryFirstName)
                jsonObject.accumulate("secondary_last_name", prefHelper.secondaryLastName)
            }
            jsonObject.accumulate("device_id", deviceId)
            val result = post(REGISTER, jsonObject, true)
            if (result != null) {
                if (parseRegistrationResult(result)) {
                    prefHelper.setRegistrationChanged(false)
                }
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "sendRegistration()", "server")
        }
    }

    internal fun parseRegistrationResult(result: String): Boolean {
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
        try {
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
                }
            }
            val version_project = blob.getInt(PrefHelper.VERSION_PROJECT)
            val version_equipment = blob.getInt(PrefHelper.VERSION_EQUIPMENT)
            val version_note = blob.getInt(PrefHelper.VERSION_NOTE)
            val version_company = blob.getInt(PrefHelper.VERSION_COMPANY)
            val version_truck = blob.getInt(PrefHelper.VERSION_TRUCK)
            if (prefHelper.versionProject != version_project) {
                Timber.i("New project version $version_project")
                queryProjects()
                prefHelper.versionProject = version_project
            }
            if (prefHelper.versionCompany != version_company) {
                Timber.i("New company version $version_company")
                queryCompanies()
                prefHelper.versionCompany = version_company
            }
            if (prefHelper.versionEquipment != version_equipment) {
                Timber.i("New equipment version $version_equipment")
                queryEquipments()
                prefHelper.versionEquipment = version_equipment
            }
            if (prefHelper.versionNote != version_note) {
                Timber.i("New note version $version_note")
                queryNotes()
                prefHelper.versionNote = version_note
            }
            if (prefHelper.versionTruck != version_truck) {
                Timber.i("New truck version $version_truck")
                queryTrucks()
                prefHelper.versionTruck = version_truck
            }
            var entries = db.entry.queryPendingDataToUploadToMaster()
            var count = 0
            if (entries.isNotEmpty()) {
                count = sendEntries(entries)
            }
            if (count > 0) {
                EventBus.getDefault().post(EventRefreshProjects())
            }
            entries = db.entry.queryPendingPicturesToUpload()
            if (entries.size > 0) {
                if (AmazonHelper.instance.sendPictures(context, entries)) {
                    EventBus.getDefault().post(EventRefreshProjects())
                }
            }
            db.pictureCollection.clearUploadedUnscaledPhotos()
            val lines = db.crash.queryNeedsUploading()
            sendCrashLines(lines)
            // If any entries do not yet have server-id's, try to get them.
            entries = db.entry.queryServerIds()
            if (entries.isNotEmpty()) {
                Timber.i("FOUND " + entries.size + " entries without server IDS")
                sendEntries(entries)
            } else {
                Timber.i("All entries have server ids")
            }
        } catch (ex: Exception) {
            TBApplication.ReportServerError(ex, DCPing::class.java, "ping()", "server")
        }
    }

    internal fun queryProjects() {
        Timber.i("queryProjects()")
        try {
            val response = post(PROJECTS, true)
            if (response == null) {
                Timber.e("queryProjects(): Unexpected NULL response from server")
                return
            }
            val unprocessed = db.projects.query().toMutableList()
            val `object` = parseResult(response)
            val array = `object`.getJSONArray("projects")
            for (i in 0 until array.length()) {
                val ele = array.getJSONObject(i)
                val server_id = ele.getInt("id")
                val name = ele.getString("name")
                val disabled = ele.getBoolean("disabled")
                val project = db.projects.queryByServerId(server_id)
                if (project == null) {
                    if (TextUtils.isEmpty(name)) {
                        TBApplication.ReportError("Got empty project name from server", DCPing::class.java, "queryProjects()", "server")
                    } else if (unprocessed.contains(name)) {
                        // If this name already exists, convert the existing one by simply giving it the server_id.
                        val existing = db.projects.queryByName(name)
                        existing!!.serverId = server_id
                        existing.isBootStrap = false
                        existing.disabled = disabled
                        db.projects.update(existing)
                        Timber.i("Commandeer local: $name")
                    } else {
                        // Otherwise just add the new project.
                        Timber.i("New project: $name")
                        db.projects.add(name, server_id, disabled)
                    }
                } else {
                    // Name change?
                    if (name != project.name) {
                        Timber.i("New name: $name")
                        project.name = name
                        project.disabled = disabled
                        db.projects.update(project)
                    } else if (project.disabled != disabled) {
                        Timber.i("Project " + name + " " + if (disabled) "disabled" else "enabled")
                        project.disabled = disabled
                        db.projects.update(project)
                    } else {
                        Timber.i("No change: $name")
                    }
                }
                unprocessed.remove(name)
            }
            // Remaining unprocessed elements are disabled if they have entries.
            for (name in unprocessed) {
                val existing = db.projects.queryByName(name)
                if (existing != null) {
                    Timber.i("Project disable or delete: $name")
                    db.projects.removeOrDisable(existing)
                }
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryProjects()", "server")
        }

    }

    internal fun queryCompanies() {
        Timber.i("queryCompanies()")
        try {
            val response = post(COMPANIES, true) ?: return
            val unprocessed = db.address.query().toMutableList()
            val `object` = parseResult(response)
            val array = `object`.getJSONArray("companies")
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
                val item = db.address.queryByServerId(server_id)
                if (item == null) {
                    val match = get(unprocessed, incoming)
                    if (match != null) {
                        // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                        match.serverId = server_id
                        match.isLocal = false
                        match.isBootStrap = false
                        db.address.update(match)
                        Timber.i("Commandeer local: " + match.toString())
                        unprocessed.remove(match)
                    } else {
                        // Otherwise just add the new entry.
                        db.address.add(incoming)
                        Timber.i("New company: " + incoming.toString())
                    }
                } else {
                    // Change of name, street, city or state?
                    if (!incoming.equals(item) || incoming.isLocal != item.isLocal) {
                        incoming.id = item.id
                        incoming.serverId = item.serverId
                        incoming.isLocal = false
                        Timber.i("Change: " + incoming.toString())
                        db.address.update(incoming)
                    } else {
                        Timber.i("No change: " + incoming.toString())
                    }
                    unprocessed.remove(item)
                }
            }
            // Remaining unprocessed elements are disabled if they have entries.
            for (item in unprocessed) {
                db.address.removeOrDisable(item)
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryCompanies()", "server")
        }

    }

    internal operator fun get(items: List<DataAddress>, match: DataAddress): DataAddress? {
        for (item in items) {
            if (item.equals(match)) {
                return item
            }
        }
        return null
    }

    internal fun queryEquipments() {
        val showDebug = BuildConfig.DEBUG
        Timber.i("queryEquipments()")
        try {
            val response = post(EQUIPMENTS, true) ?: return
            val blob = parseResult(response)
            run {
                val unprocessed = db.equipment.query().toMutableList()
                val array = blob.getJSONArray("equipments")
                for (i in 0 until array.length()) {
                    val ele = array.getJSONObject(i)
                    val server_id = ele.getInt("id")
                    val name = ele.getString("name")
                    val incoming = DataEquipment(name, server_id)
                    val item = db.equipment.queryByServerId(server_id)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            // If this name already exists, convert the existing one by simply giving it the server_id.
                            match.serverId = server_id.toLong()
                            match.isBootStrap = false
                            match.isLocal = false
                            db.equipment.update(match)
                            Timber.i("Commandeer local: $name")
                            unprocessed.remove(match)
                        } else {
                            // Otherwise just add the new entry.
                            Timber.i("New equipment: $name")
                            db.equipment.add(incoming)
                        }
                    } else {
                        // Change of name
                        if (!incoming.equals(item)) {
                            Timber.i("Change: $name")
                            incoming.id = item.id
                            incoming.serverId = item.serverId
                            incoming.isLocal = false
                            db.equipment.update(incoming)
                        } else {
                            Timber.i("No change: $name")
                        }
                        unprocessed.remove(item)
                    }
                }
                // Remaining unprocessed elements are disabled if they have entries.
                for (item in unprocessed) {
                    db.equipment.removeOrDisable(item)
                }
            }
            run {
                val unprocessed = db.collectionEquipmentProject.query().toMutableList()
                val array = blob.getJSONArray("project_equipment")
                for (i in 0 until array.length()) {
                    val ele = array.getJSONObject(i)
                    val server_id = ele.getInt("id")
                    val server_project_id = ele.getInt("project_id")
                    val server_equipment_id = ele.getInt("equipment_id")
                    val incoming = DataCollectionItem()
                    incoming.server_id = server_id
                    // Note: project ID is from the perspective of the server, not the APP.
                    val project = db.projects.queryByServerId(server_project_id)
                    val equipment = db.equipment.queryByServerId(server_equipment_id)
                    if (project == null || equipment == null) {
                        if (project == null && equipment == null) {
                            Timber.e("Can't find any project with server ID $server_project_id nor equipment ID $server_equipment_id")
                            prefHelper.reloadProjects()
                            prefHelper.reloadEquipments()
                        } else if (project == null) {
                            val sbuf = StringBuilder()
                            sbuf.append("Can't find any project with server ID ")
                            sbuf.append(server_project_id)
                            sbuf.append(" for equipment ")
                            sbuf.append(equipment!!.name)
                            sbuf.append(". Projects=")
                            for (name in db.projects.query()) {
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
                    val item = db.collectionEquipmentProject.queryByServerId(server_id)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                            match.server_id = server_id
                            match.isBootstrap = false
                            db.collectionEquipmentProject.update(match)
                            if (showDebug) {
                                val projectName = db.projects.queryProjectName(match.collection_id)
                                val equipmentName = db.equipment.queryEquipmentName(match.value_id)
                                Timber.i("Commandeer local: PROJECT COLLECTION $projectName <=> $equipmentName")
                            }
                            unprocessed.remove(match)
                        } else {
                            // Otherwise just add the new entry.
                            if (showDebug) {
                                val projectName = db.projects.queryProjectName(incoming.collection_id)
                                val equipmentName = db.equipment.queryEquipmentName(incoming.value_id)
                                Timber.i("New project collection: $projectName <=> $equipmentName")
                            }
                            db.collectionEquipmentProject.add(incoming)
                        }
                    } else {
                        // Change of IDs. A little weird, but we will allow it.
                        if (!incoming.equals(item)) {
                            if (showDebug) {
                                val projectName = db.projects.queryProjectName(item.collection_id)
                                val equipmentName = db.equipment.queryEquipmentName(item.value_id)
                                Timber.i("Change? $projectName <=> $equipmentName")
                            }
                            incoming.id = item.id
                            incoming.server_id = item.server_id
                            db.collectionEquipmentProject.update(incoming)
                        } else {
                            if (showDebug) {
                                val projectName = db.projects.queryProjectName(item.collection_id)
                                val equipmentName = db.equipment.queryEquipmentName(item.value_id)
                                Timber.i("No change: $projectName <=> $equipmentName")
                            }
                        }
                        unprocessed.remove(item)
                    }
                }
                for (item in unprocessed) {
                    if (showDebug) {
                        val projectName = db.projects.queryProjectName(item.collection_id)
                        val equipmentName = db.equipment.queryEquipmentName(item.value_id)
                        Timber.i("Removing: $projectName <=> $equipmentName")
                    }
                    db.collectionEquipmentProject.remove(item.id)
                }
                if (showDebug) {
                    if (unprocessed.size == 0) {
                        Timber.i("No unprocessed items.")
                    }
                }
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryEquipments()", "server")
        }

    }

    internal operator fun get(items: List<DataEquipment>, match: DataEquipment): DataEquipment? {
        for (item in items) {
            if (item.equals(match)) {
                return item
            }
        }
        return null
    }

    internal operator fun get(items: List<DataCollectionItem>, match: DataCollectionItem): DataCollectionItem? {
        for (item in items) {
            if (item.equals(match)) {
                return item
            }
        }
        return null
    }

    internal fun queryNotes() {
        Timber.i("queryNotes()")
        try {
            val response = post(NOTES, true) ?: return
            val `object` = parseResult(response)
            run {
                val unprocessed = db.note.query().toMutableList()
                val array = `object`.getJSONArray("notes")
                for (i in 0 until array.length()) {
                    val ele = array.getJSONObject(i)
                    val server_id = ele.getInt("id")
                    val name = ele.getString("name")
                    val typeStr = ele.getString("type")
                    val num_digits = ele.getInt("num_digits").toShort()
                    val type = DataNote.Type.from(typeStr)
                    val incoming = DataNote(name, type, num_digits, server_id)
                    val item = db.note.queryByServerId(server_id)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            // If this name already exists, convert the existing one by simply giving it the server_id.
                            match.serverId = server_id
                            match.num_digits = num_digits
                            db.note.update(match)
                            Timber.i("Commandeer local: " + match.toString())
                            unprocessed.remove(match)
                        } else {
                            // Otherwise just add the new entry.
                            db.note.add(incoming)
                            Timber.i("New note: " + incoming.toString())
                        }
                    } else {
                        // Change of name, type and/or num_digits
                        if (incoming != item) {
                            incoming.id = item.id
                            incoming.serverId = item.serverId
                            db.note.update(incoming)
                            Timber.i("Change: " + incoming.toString())
                        } else {
                            Timber.i("No change: " + item.toString())
                        }
                        unprocessed.remove(item)
                    }
                }
                // Remove or disable unprocessed elements
                for (note in unprocessed) {
                    db.note.removeIfUnused(note)
                }
            }
            run {
                val unprocessed = db.collectionNoteProject.query().toMutableList()
                val array = `object`.getJSONArray("project_note")
                for (i in 0 until array.length()) {
                    val ele = array.getJSONObject(i)
                    val server_id = ele.getInt("id")
                    val server_project_id = ele.getInt("project_id")
                    val server_note_id = ele.getInt("note_id")
                    val incoming = DataCollectionItem()
                    incoming.server_id = server_id
                    // Note: project ID is from the perspective of the server, not the APP.
                    val project = db.projects.queryByServerId(server_project_id) ?: continue
                    incoming.collection_id = project.id
                    val note = db.note.queryByServerId(server_note_id)
                    if (note == null) {
                        Timber.e("queryNotes(): Can't find picture_note with ID $server_note_id")
                        continue
                    }
                    incoming.value_id = note.id
                    val item = db.collectionNoteProject.queryByServerId(server_id)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                            match.server_id = server_id
                            db.collectionNoteProject.update(match)
                            Timber.i("Commandeer local: NOTE COLLECTION " + match.collection_id + ", " + match.value_id)
                            unprocessed.remove(match)
                        } else {
                            // Otherwise just add the new entry.
                            Timber.i("New picture_note collection. " + incoming.collection_id + ", " + incoming.value_id)
                            db.collectionNoteProject.add(incoming)
                        }
                    } else {
                        // Change of IDs. A little weird, but we will allow it.
                        if (!incoming.equals(item)) {
                            Timber.i("Change? " + item.collection_id + ", " + item.value_id)
                            incoming.id = item.id
                            incoming.server_id = item.server_id
                            db.collectionNoteProject.update(incoming)
                        }
                    }
                }
                for (item in unprocessed) {
                    db.collectionNoteProject.removeIfGone(item)
                }
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryNotes()", "server")
        }

    }

    internal fun queryTrucks() {
        Timber.i("queryTrucks()")
        try {
            val response = post(TRUCKS, true) ?: return
            val `object` = parseResult(response)
            run {
                val unprocessed = db.truck.query().toMutableList()
                val array = `object`.getJSONArray("trucks")
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
                        val project = db.projects.queryByServerId(project_server_id)
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
                    val item = db.truck.queryByServerId(incoming.serverId)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            incoming.id = match.id
                            db.truck.save(incoming)
                            Timber.i("Commandeer local: " + incoming.toLongString(db))
                            unprocessed.removeAll { it.id == match.id }
                        } else {
                            // Otherwise just add the new entry.
                            Timber.i("New truck: " + incoming.toLongString(db))
                            db.truck.save(incoming)
                        }
                    } else {
                        // Change of data
                        if (!incoming.equals(item)) {
                            Timber.i("Change: [" + incoming.toLongString(db) + "] from [" + item.toLongString(db) + "]")
                            incoming.id = item.id
                            db.truck.save(incoming)
                        } else {
                            Timber.i("No change: " + item.toLongString(db))
                        }
                        unprocessed.removeAll { it.id == item.id }
                    }
                }
                // Remove or disable unprocessed elements
                for (truck in unprocessed) {
                    db.truck.removeIfUnused(truck)
                }
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryTrucks()", "server")
        }
    }

    internal operator fun get(items: List<DataTruck>, match: DataTruck): DataTruck? {
        for (item in items) {
            if (item.equals(match)) {
                return item
            }
        }
        return null
    }

    internal fun sendEntries(list: List<DataEntry>): Int {
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

    internal fun sendEntry(entry: DataEntry): Boolean {
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
                    db.entry.saveUploaded(entry)
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

    internal operator fun get(items: List<DataNote>, match: DataNote): DataNote? {
        for (item in items) {
            if (item == match) {
                return item
            }
        }
        return null
    }

    internal fun post(target: String, sendErrors: Boolean): String? {
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

    internal fun post(target: String, json: JSONObject, sendErrors: Boolean): String? {
        try {
            val url = URL(target)
            val connection = url.openConnection() as HttpURLConnection
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            val stream = OutputStreamWriter(connection.outputStream, "UTF-8")
            val writer = BufferedWriter(stream)
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

    internal fun parseResult(result: String): JSONObject {
        try {
            return JSONObject(result)
        } catch (ex: Exception) {
            Timber.e("Got bad result back from server: " + result + "\n" + ex.message)
        }

        return JSONObject()
    }

    internal fun sendCrashLines(lines: List<SqlTableCrash.CrashLine>) {
        for (line in lines) {
            sendCrashLine(line)
        }
    }

    internal fun sendCrashLine(line: SqlTableCrash.CrashLine) {
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
                db.crash.setUploaded(line)
            } else {
                Log.e(TAG, "Unable to send previously trapped message: " + line.message!!)
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.message)
        }
    }

}

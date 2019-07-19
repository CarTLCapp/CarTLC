/*
 * *
 *   * Copyright 2019, FleetTLC. All rights reserved
 *
 */
package com.cartlc.tracker.fresh.service.endpoint

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.annotation.VisibleForTesting

import com.cartlc.tracker.BuildConfig
import com.cartlc.tracker.fresh.model.core.data.*
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.fresh.model.misc.TruckStatus
import com.cartlc.tracker.fresh.model.event.EventRefreshProjects
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.sql.SqlTableCrash
import com.cartlc.tracker.fresh.service.help.ServerHelper

import org.json.JSONArray
import org.json.JSONObject

import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by dug on 8/24/17.
 */

class DCPing(
        private val context: Context,
        private val repo: CarRepository
) : DCPost() {

    companion object {

        private const val TAG = "DCPing"
        private const val LOG = true

        private const val SERVER_URL_DEVELOPMENT = "https://fleetdev.arqnetworks.com/"
        private const val SERVER_URL_RELEASE = "http://fleettlc.arqnetworks.com/"

        private const val UPLOAD_RESET_TRIGGER = "reset_upload"
        private const val RE_REGISTER_TRIGGER = "re-register"
        private const val RELOAD_CODE = "reload_code"

        // After this many times indicate to the user if there is a problem that needs to be
        // addressed with the tableEntry.
        private const val FAILED_UPLOADED_TRIGGER = 5

        private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'z"

        private const val COMPANY_PAGE_SIZE = 100
    }

    private val serverUrl: String
    private val registerUrl: String
    private val enterUrl: String
    private val pingUrl: String
    private val projectsUrl: String
    private val companiesUrl: String
    private val equipmentsUrl: String
    private val notesUrl: String
    private val trucksUrl: String
    private val messageUrl: String
    private val vehicleUrl: String
    private val vehiclesUrl: String
    private val stringsUrl: String
    private var appVersion: String? = null

    private val prefHelper: PrefHelper
        get() = repo.prefHelper

    private val db: DatabaseTable
        get() = repo.db

    private val app: TBApplication
        get() = context.applicationContext as TBApplication

    private val version: String?
        get() {
            if (appVersion == null) {
                try {
                    appVersion = app.version
                } catch (ex: Exception) {
                    TBApplication.ReportError(ex, DCPing::class.java, "getVersion()", "server")
                }
            }
            return appVersion
        }

    @VisibleForTesting
    var openConnection: (target: String) -> HttpURLConnection = { target -> openTargetConnection(target) }

    init {
        serverUrl = if (repo.isDevelopment) {
            SERVER_URL_DEVELOPMENT
        } else {
            SERVER_URL_RELEASE
        }
        registerUrl = serverUrl + "register"
        enterUrl = serverUrl + "enter"
        pingUrl = serverUrl + "ping"
        projectsUrl = serverUrl + "projects/root"
        companiesUrl = serverUrl + "companies"
        equipmentsUrl = serverUrl + "equipments/root"
        notesUrl = serverUrl + "notes/root"
        messageUrl = serverUrl + "message"
        trucksUrl = serverUrl + "trucks"
        vehicleUrl = serverUrl + "vehicle"
        vehiclesUrl = serverUrl + "vehicles"
        stringsUrl = serverUrl + "strings"
    }

    fun sendRegistration(techCode: String, secondaryTechCode: String?): String? {
        var errorMessage: String?
        Timber.i("sendRegistration($techCode, $secondaryTechCode)")
        try {
            val deviceId = ServerHelper.instance.deviceId
            val jsonObject = JSONObject()
            jsonObject.accumulate("code", techCode)
            if (secondaryTechCode != null) {
                jsonObject.accumulate("secondary_code", secondaryTechCode)
            }
            jsonObject.accumulate("device_id", deviceId)
            val response = post(registerUrl, jsonObject, false)
            val result = parseRegistrationResult(response)
            if (result.errorMessage == null) {
                prefHelper.firstTechCode = techCode
                prefHelper.secondaryTechCode = secondaryTechCode

                prefHelper.techID = result.techID
                prefHelper.techFirstName = result.techFirstName
                prefHelper.techLastName = result.techLastName
                prefHelper.secondaryTechID = result.secondaryTechID ?: 0
                prefHelper.secondaryTechFirstName = result.secondaryFirstName
                prefHelper.secondaryTechLastName = result.secondaryLastName
            }
            errorMessage = result.errorMessage
        } catch (ex: IOException) {
            errorMessage = ex.message
        } catch (ex: Exception) {
            errorMessage = ex.message
        }
        return errorMessage
    }

    data class RegResult(
            val techID: Int,
            val techFirstName: String,
            val techLastName: String,
            val secondaryTechID: Int?,
            val secondaryFirstName: String?,
            val secondaryLastName: String?,
            val errorMessage: String?
    )

    private fun parseRegistrationResult(response: String): RegResult {
        val blob = parseResult(response)
        try {
            val techId = blob.getInt("tech_id")
            val techFirstName = blob.getString("tech_first_name")
            val techLastName = blob.getString("tech_last_name")

            val secondaryTechId: Int?
            val secondaryTechFirstName: String?
            val secondaryTechLastName: String?

            if (blob.has("secondary_tech_id")) {
                secondaryTechId = blob.getInt("secondary_tech_id")
                secondaryTechFirstName = blob.getString("secondary_tech_first_name")
                secondaryTechLastName = blob.getString("secondary_tech_last_name")
            } else {
                secondaryTechId = null
                secondaryTechFirstName = null
                secondaryTechLastName = null
            }
            return RegResult(
                    techId, techFirstName, techLastName,
                    secondaryTechId, secondaryTechFirstName, secondaryTechLastName,
                    null
            )
        } catch (ex: NumberFormatException) {
            val msg = "sendRegistration(): PARSE ERROR on: ${ex.message}"
            return RegResult(0, "", "", null, null, null, msg)
        } catch (ex: Exception) {
            val msg = "sendRegistration(): ERROR on: ${ex.message}"
            return RegResult(0, "", "", null, null, null, msg)
        }
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
        val response: String
        try {
            response = post(pingUrl, jsonObject)
        } catch (ex: IOException) {
            return
        } catch (ex: Exception) {
            return
        }
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
                prefHelper.firstTechCode?.let {
                    sendRegistration(it, prefHelper.secondaryTechCode)
                }
            }
        }
        if (blob.has(RELOAD_CODE)) {
            val reloadCode = blob.getString(RELOAD_CODE)
            if (!TextUtils.isEmpty(reloadCode)) {
                if (reloadCode.contains("p")) {
                    prefHelper.versionProject = 0
                }
                if (reloadCode.contains("e")) {
                    prefHelper.versionEquipment = 0
                }
                if (reloadCode.contains("n")) {
                    prefHelper.versionNote = 0
                }
                if (reloadCode.contains("c")) {
                    prefHelper.versionCompany = 0
                }
                if (reloadCode.contains("t")) {
                    prefHelper.versionTruck = 0
                }
                if (reloadCode.contains("v")) {
                    prefHelper.versionVehicleNames = 0
                }
            }
        }
        val version_project = blob.getInt(PrefHelper.VERSION_PROJECT)
        val version_equipment = blob.getInt(PrefHelper.VERSION_EQUIPMENT)
        val version_note = blob.getInt(PrefHelper.VERSION_NOTE)
        val version_company = blob.getInt(PrefHelper.VERSION_COMPANY)
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
//        if (QUERY_TRUCKS) {
//            if (prefHelper.versionTruck != version_truck) {
//                Timber.i("New truck version $version_truck")
//                if (!queryTrucks()) {
//                    return
//                }
//                prefHelper.versionTruck = version_truck
//            }
//        }
        var entries = db.tableEntry.queryPendingDataToUploadToMaster()
        var count = 0
        if (entries.isNotEmpty()) {
            count = sendEntries(entries)
        }
        if (count > 0) {
            EventBus.getDefault().post(EventRefreshProjects())
        }
        entries = db.tableEntry.queryPendingPicturesToUpload()
        if (entries.isNotEmpty()) {
            if (app.amazonHelper.sendPictures(context, entries)) {
                EventBus.getDefault().post(EventRefreshProjects())
            }
        }
        db.tablePictureCollection.clearUploadedUnscaledPhotos()
        val lines = db.tableCrash.queryNeedsUploading()
        sendCrashLines(lines)
        // If any entries do not yet have server-id's, try to get them.
        entries = db.tableEntry.queryServerIds()
        if (entries.isNotEmpty()) {
            Timber.i("FOUND ${entries.size} entries needing to be uploaded")
            sendEntries(entries)
        } else {
            Timber.i("All entries have server ids")
        }
        val vehicles = db.tableVehicle.queryNotUploaded()
        if (vehicles.isNotEmpty()) {
            Timber.i("FOUND ${vehicles.size} vehicles needing to be uploaded")
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
            val response = post(projectsUrl)
            if (response == null) {
                Timber.e("queryProjects(): Unexpected NULL response from server")
                return false
            }
            val unprocessed = ProjectProcessed(db)
            val obj = parseResult(response)
            val array = obj.getJSONArray("projects")
            for (i in 0 until array.length()) {
                val ele = array.getJSONObject(i)
                val serverId = ele.getInt("id")
                val rootProject = ele.getString("root_project")
                val subProject = ele.getString("sub_project")
                if (rootProject.isNullOrEmpty()) {
                    TBApplication.ReportError("Got empty root project name from server", DCPing::class.java, "queryProjects()", "server")
                    continue
                }
                if (subProject.isNullOrEmpty()) {
                    TBApplication.ReportError("Got empty sub project name from server", DCPing::class.java, "queryProjects()", "server")
                    continue
                }
                val disabled = ele.getBoolean("disabled")
                val project = db.tableProjects.queryByServerId(serverId)
                if (project == null) {
                    if (unprocessed.contains(rootProject, subProject)) {
                        // If this name already exists, convert the existing one by simply giving it the server_id.
                        val existing = db.tableProjects.queryByName(rootProject, subProject)
                        existing!!.serverId = serverId
                        existing.isBootStrap = false
                        existing.disabled = disabled
                        db.tableProjects.update(existing)
                        unprocessed.delete(rootProject, subProject)
                    } else {
                        // Otherwise just add the new project.
                        db.tableProjects.add(rootProject, subProject, serverId, disabled)
                    }
                } else {
                    // Name change?
                    when {
                        subProject != project.name || rootProject != project.rootProject -> {
                            Timber.i("New name. Root: $rootProject Sub: $subProject")
                            unprocessed.delete(project.name, project.rootProject)

                            project.name = subProject
                            project.rootProject = rootProject
                            project.disabled = disabled
                            db.tableProjects.update(project)
                        }
                        project.disabled != disabled -> {
                            Timber.i("Disable flag change. Root: $rootProject Sub: $subProject Disabled now: $disabled")
                            project.disabled = disabled
                            db.tableProjects.update(project)
                        }
                        else -> Timber.i("No change to project: $rootProject - $subProject")
                    }
                    unprocessed.delete(rootProject, subProject)
                }
            }
            // Remaining unprocessed elements are disabled if they have entries.
            unprocessed.disableRemaining()
        } catch (ex: IOException) {
            return false
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryProjects()", "server")
            return false
        }
        return true
    }

    private fun queryCompanies(): Boolean {
        val unprocessed = db.tableAddress.query().toMutableList()
        val numPages = queryCompanies(0, unprocessed)
        if (numPages == 0) {
            return false
        }
        for (page in 1 until numPages) {
            if (queryCompanies(page, unprocessed) == 0) {
                return false
            }
        }
        // Remaining unprocessed elements are disabled if they have entries.
        for (item in unprocessed) {
            db.tableAddress.removeOrDisable(item)
        }
        return true
    }

    private fun queryCompanies(page: Int, unprocessed: MutableList<DataAddress>): Int {
        Timber.i("queryCompanies()")
        val numPages: Int
        try {
            val response = post(companiesUrl, page, COMPANY_PAGE_SIZE)
            val obj = parseResult(response)
            numPages = obj.getInt("numPages")
            val array = obj.getJSONArray("companies")
            var name: String
            var street: String?
            var city: String?
            var state: String?
            var zipcode: String?
            for (i in 0 until array.length()) {
                val ele = array.getJSONObject(i)
                val serverId = ele.getInt("id")
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
                    TBApplication.ReportError("Missing street, city, or state for company $name, server id $serverId", DCPing::class.java, "queryCompanies()", "server")
                    continue
                }
                if (ele.has("zipcode")) {
                    zipcode = ele.getString("zipcode")
                } else {
                    zipcode = null
                }
                val incoming = DataAddress(serverId, name, street, city, state, zipcode)
                val item = db.tableAddress.queryByServerId(serverId)
                if (item == null) {
                    val match = get(unprocessed, incoming)
                    if (match != null) {
                        // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                        match.serverId = serverId
                        match.isLocal = false
                        match.isBootStrap = false
                        db.tableAddress.update(match)
                        Timber.i("Commandeer local: $match")
                        unprocessed.remove(match)
                    } else {
                        // Otherwise just add the new tableEntry.
                        db.tableAddress.add(incoming)
                        Timber.i("New company: $incoming")
                    }
                } else {
                    // Change of name, street, city or state?
                    if (!incoming.equals(item) || incoming.isLocal != item.isLocal) {
                        incoming.id = item.id
                        incoming.serverId = item.serverId
                        incoming.isLocal = false
                        Timber.i("Change: $incoming")
                        db.tableAddress.update(incoming)
                    } else {
                        Timber.i("No change: $incoming")
                    }
                    unprocessed.remove(item)
                }
            }
        } catch (ex: IOException) {
            return 0
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryCompanies()", "server")
            return 0
        }
        return numPages
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
            val response = post(equipmentsUrl) ?: return false
            val blob = parseResult(response)
            run {
                val unprocessed = db.tableEquipment.query().toMutableList()
                val array = blob.getJSONArray("equipments")
                for (i in 0 until array.length()) {
                    val ele = array.getJSONObject(i)
                    val serverId = ele.getInt("id")
                    val name = ele.getString("name")
                    val incoming = DataEquipment(name, serverId)
                    val item = db.tableEquipment.queryByServerId(serverId)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            // If this name already exists, convert the existing one by simply giving it the server_id.
                            match.serverId = serverId.toLong()
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
                        if (incoming != item) {
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
                    val serverId = ele.getInt("id")
                    val serverProjectId = ele.getInt("project_id")
                    val serverEquipmentId = ele.getInt("equipment_id")
                    val incoming = DataCollectionItem()
                    incoming.server_id = serverId
                    // Note: project ID is from the perspective of the server, not the APP.
                    val project = db.tableProjects.queryByServerId(serverProjectId)
                    val equipment = db.tableEquipment.queryByServerId(serverEquipmentId)
                    if (project == null || equipment == null) {
                        if (project == null && equipment == null) {
                            Timber.e("Can't find any project with server ID $serverProjectId nor equipment ID $serverEquipmentId")
                            prefHelper.reloadProjects()
                            prefHelper.reloadEquipments()
                        } else if (project == null) {
                            val sbuf = StringBuilder()
                            sbuf.append("Can't find any project with server ID ")
                            sbuf.append(serverProjectId)
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
                            Timber.e("Can't find any equipment with server ID $serverEquipmentId for project ${project.name}")
                            prefHelper.reloadEquipments()
                        }
                        continue
                    }
                    incoming.collection_id = project.id
                    incoming.value_id = equipment.id
                    val item = db.tableCollectionEquipmentProject.queryByServerId(serverId)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                            match.server_id = serverId
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
                        if (incoming != item) {
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
        } catch (ex: IOException) {
            return false
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryEquipments()", "server")
            return false
        }
        return true
    }

    private operator fun get(items: List<DataEquipment>, match: DataEquipment): DataEquipment? {
        for (item in items) {
            if (item == match) {
                return item
            }
        }
        return null
    }

    private operator fun get(items: List<DataCollectionItem>, match: DataCollectionItem): DataCollectionItem? {
        for (item in items) {
            if (item == match) {
                return item
            }
        }
        return null
    }

    private fun queryNotes(): Boolean {
        Timber.i("queryNotes()")
        try {
            val response = post(notesUrl) ?: return false
            val obj = parseResult(response)
            run {
                val unprocessed = db.tableNote.query().toMutableList()
                val array = obj.getJSONArray("notes")
                for (i in 0 until array.length()) {
                    val ele = array.getJSONObject(i)
                    val serverId = ele.getInt("id")
                    val name = ele.getString("name")
                    val typeStr = ele.getString("type")
                    val numDigits = ele.getInt("num_digits").toShort()
                    val type = DataNote.Type.from(typeStr)
                    val incoming = DataNote(name, type, numDigits, serverId)
                    val item = db.tableNote.queryByServerId(serverId)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            // If this name already exists, convert the existing one by simply giving it the server_id.
                            match.serverId = serverId
                            match.num_digits = numDigits
                            db.tableNote.update(match)
                            Timber.i("Commandeer local: $match")
                            unprocessed.remove(match)
                        } else {
                            // Otherwise just add the new tableEntry.
                            db.tableNote.add(incoming)
                            Timber.i("New note: $incoming")
                        }
                    } else {
                        // Change of name, type and/or num_digits
                        if (incoming != item) {
                            incoming.id = item.id
                            incoming.serverId = item.serverId
                            db.tableNote.update(incoming)
                            Timber.i("Change: $incoming")
                        } else {
                            Timber.i("No change: $item")
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
                    val serverId = ele.getInt("id")
                    val serverProjectId = ele.getInt("project_id")
                    val serverNoteId = ele.getInt("note_id")
                    val incoming = DataCollectionItem()
                    incoming.server_id = serverId
                    // Note: project ID is from the perspective of the server, not the APP.
                    val project = db.tableProjects.queryByServerId(serverProjectId) ?: continue
                    incoming.collection_id = project.id
                    val note = db.tableNote.queryByServerId(serverNoteId)
                    if (note == null) {
                        Timber.e("queryNotes(): Can't find picture_note with ID $serverNoteId")
                        continue
                    }
                    incoming.value_id = note.id
                    val item = db.tableCollectionNoteProject.queryByServerId(serverId)
                    if (item == null) {
                        val match = get(unprocessed, incoming)
                        if (match != null) {
                            // If this name already existsUnscaled, convert the existing one by simply giving it the server_id.
                            match.server_id = serverId
                            db.tableCollectionNoteProject.update(match)
                            Timber.i("Commandeer local: NOTE COLLECTION ${match.collection_id}, ${match.value_id}")
                            unprocessed.remove(match)
                        } else {
                            // Otherwise just add the new tableEntry.
                            Timber.i("New picture_note collection. ${incoming.collection_id}, ${incoming.value_id}")
                            db.tableCollectionNoteProject.add(incoming)
                        }
                    } else {
                        // Change of IDs. A little weird, but we will allow it.
                        if (incoming != item) {
                            Timber.i("Change? ${item.collection_id}, ${item.value_id}")
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
        } catch (ex: IOException) {
            return false
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryNotes()", "server")
            return false
        }
        return true
    }

//    private fun queryTrucks(): Boolean {
//        val unprocessed = db.tableTruck.query().toMutableList()
//        val numPages = queryTrucks(0, unprocessed)
//        if (numPages == 0) {
//            return false
//        }
//        for (page in 1 until numPages) {
//            if (queryTrucks(page, unprocessed) == 0) {
//                return false
//            }
//        }
//        // Remove or disable unprocessed elements
//        for (truck in unprocessed) {
//            db.tableTruck.removeIfUnused(truck)
//        }
//        return true
//    }

//    private fun queryTrucks(page: Int, unprocessed: MutableList<DataTruck>): Int {
//        Timber.i("queryTrucks()")
//        val numPages: Int
//        try {
//            val response = post(trucksUrl, page, TRUCK_PAGE_SIZE, true) ?: return 0
//            val obj = parseResult(response)
//            run {
//                val array = obj.getJSONArray("trucks")
//                numPages = obj.getInt("numPages")
//                for (i in 0 until array.length()) {
//                    val ele = array.getJSONObject(i)
//                    val incoming = DataTruck()
//                    incoming.serverId = ele.getLong("id")
//                    if (ele.has("truck_number_string")) {
//                        incoming.truckNumber = ele.getString("truck_number_string")
//                    } else if (ele.has("truck_number")) {
//                        incoming.truckNumber = Integer.toString(ele.getInt("truck_number"))
//                    }
//                    if (ele.has("license_plate")) {
//                        incoming.licensePlateNumber = ele.getString("license_plate")
//                    }
//                    if (ele.has("project_id")) {
//                        val projectServerId = ele.getInt("project_id")
//                        val project = db.tableProjects.queryByServerId(projectServerId)
//                        if (project == null) {
//                            Timber.e("Can't find any project with server ID $projectServerId for truck number ${incoming.truckNumber}")
//                        } else {
//                            incoming.projectNameId = project.id
//                        }
//                    }
//                    if (ele.has("company_name")) {
//                        incoming.companyName = ele.getString("company_name")
//                    }
//                    if (ele.has("has_entries")) {
//                        incoming.hasEntry = ele.getBoolean("has_entries")
//                    }
//                    val item = db.tableTruck.queryByServerId(incoming.serverId)
//                    if (item == null) {
//                        val match = get(unprocessed, incoming)
//                        if (match != null) {
//                            incoming.id = match.id
//                            db.tableTruck.save(incoming)
//                            Timber.i("Commandeer local truck: ${incoming.toLongString(db)}")
//                            unprocessed.removeAll { it.id == match.id }
//                        } else {
//                            // Otherwise just add the new truck.
//                            Timber.i("New truck: ${incoming.toLongString(db)}")
//                            db.tableTruck.save(incoming)
//                        }
//                    } else {
//                        // Change of data
//                        if (!incoming.equals(item)) {
//                            Timber.i("Change: [${incoming.toLongString(db)}] from [${item.toLongString(db)}]")
//                            incoming.id = item.id
//                            db.tableTruck.save(incoming)
//                        } else {
//                            Timber.i("No change: ${item.toLongString(db)}")
//                        }
//                        unprocessed.removeAll { it.id == item.id }
//                    }
//                }
//            }
//        } catch (ex: Exception) {
//            TBApplication.ReportError(ex, DCPing::class.java, "queryTrucks()", "server")
//            return 0
//        }
//        return numPages
//    }

//    private operator fun get(items: List<DataTruck>, match: DataTruck): DataTruck? {
//        for (item in items) {
//            if (item.equals(match)) {
//                return item
//            }
//        }
//        return null
//    }

    private fun queryVehicleNames(): Boolean {
        Timber.i("queryVehicleNames()")
        try {
            val response = post(vehiclesUrl) ?: return false
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
                            Timber.i("Commandeer local vehicle name: $incoming")
                            unprocessed.removeAll { it.id == match.id }
                        } else {
                            // Otherwise just add the new vehicle name.
                            Timber.i("New vehicle name: $incoming")
                            db.tableVehicleName.save(incoming)
                        }
                    } else {
                        // Change of data
                        if (incoming != item) {
                            Timber.i("Change: [$incoming] from [$item]")
                            incoming.id = item.id
                            db.tableVehicleName.save(incoming)
                        } else {
                            Timber.i("No change: $item")
                        }
                        unprocessed.removeAll { it.id == item.id }
                    }
                }
                // Remove or disable unprocessed elements
                for (item in unprocessed) {
                    db.tableVehicleName.remove(item)
                }
            }
        } catch (ex: IOException) {
            return false
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryTrucks()", "server")
            return false
        }
        return true
    }

    private operator fun get(items: List<DataVehicleName>, match: DataVehicleName): DataVehicleName? {
        for (item in items) {
            if (item.name == match.name) {
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
        try {
            val jsonObject = JSONObject()
            jsonObject.accumulate("tech_id", prefHelper.techID)
            if (prefHelper.hasSecondary && prefHelper.secondaryTechID > 0) {
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
                Timber.e("sendEntry(): Missing truck entry : ${entry.toLongString(db)} (check error enabled)")
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
            val equipments = entry.equipment ?: emptyList()
            if (equipments.isNotEmpty()) {
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
            if (pictures.isNotEmpty()) {
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
            if (notes.isNotEmpty()) {
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
            Timber.i("SENDING $jsonObject")
            val result = post(enterUrl, jsonObject)
            if (TextUtils.isDigitsOnly(result)) {
                entry.uploadedMaster = true
                entry.serverErrorCount = 0.toShort()
                entry.hasError = false
                entry.serverId = Integer.parseInt(result)
                db.tableEntry.saveUploaded(entry)
                success = true
                Timber.i("SUCCESS, ENTRY SERVER ID is ${entry.serverId}")
            } else {
                val sbuf = StringBuilder()
                sbuf.append("While trying to send entry: ")
                sbuf.append(entry.toLongString(db))
                sbuf.append("\nERROR: ")
                sbuf.append(result)
                TBApplication.ShowError(sbuf.toString())
                Timber.e(sbuf.toString())
            }
        } catch (ex: IOException) {
            return false
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

    private fun sendVehicle(vehicle: DataVehicle): Boolean {
        Timber.i("sendVehicle(${vehicle.id})")
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

            Timber.i("SENDING $jsonObject")
            val result = post(vehicleUrl, jsonObject)
            if (TextUtils.isDigitsOnly(result)) {
                vehicle.uploaded = true
                vehicle.serverId = result.toLong()
                db.tableVehicle.saveUploaded(vehicle)
                Timber.i("SUCCESS, VEHICLE SERVER ID is ${vehicle.serverId}")
            } else {
                val sbuf = StringBuilder()
                sbuf.append("While trying to send vehicle: ")
                sbuf.append(vehicle.toString())
                sbuf.append("\nERROR: ")
                sbuf.append(result)
                TBApplication.ShowError(sbuf.toString())
                Timber.e(sbuf.toString())
            }
        } catch (ex: IOException) {
            return false
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "sendVehicle()", "server")
            return false
        }
        return true
    }

    private fun queryStrings(): Boolean {
        Timber.i("queryStrings()")
        try {
            val response = post(stringsUrl) ?: return false
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
                    if (incoming != item) {
                        Timber.i("Change: [$incoming] from [$item")
                        incoming.id = item.id
                        db.tableString.save(incoming)
                    } else {
                        Timber.i("No change: $item")
                    }
                }
            }
        } catch (ex: IOException) {
            return false
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCPing::class.java, "queryTrucks()", "server")
            return false
        }
        return true
    }

    private operator fun get(items: List<DataNote>, match: DataNote): DataNote? {
        for (item in items) {
            if (item == match) {
                return item
            }
        }
        return null
    }

    @Throws(IOException::class)
    private fun post(target: String): String? {
        return post(target, -1, 0)
    }

    @Throws(IOException::class)
    private fun post(target: String, page: Int, pageSize: Int): String {
        val jsonObject = JSONObject()
        jsonObject.accumulate("device_id", ServerHelper.instance.deviceId)
        jsonObject.accumulate("tech_id", prefHelper.techID)
        if (page >= 0 && pageSize > 0) {
            jsonObject.accumulate("page", page)
            jsonObject.accumulate("page_size", pageSize)
        }
        return post(target, jsonObject)
    }

    private fun openTargetConnection(target: String): HttpURLConnection {
        return URL(target).openConnection() as HttpURLConnection
    }

    @Throws(IOException::class)
    private fun post(target: String, json: JSONObject, sendErrors: Boolean = true): String {
        try {
            if (LOG) {
                Log.d(TAG, "POST: $target: $json")
            }
            val connection = openConnection(target)
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            val stream = OutputStreamWriter(connection.outputStream, "UTF-8")
            val writer = BufferedWriter(stream)
            writer.write(json.toString())
            writer.close()
            val result = getResult(connection)
            connection.disconnect()
            if (LOG) {
                Log.d(TAG, "GOT RESULT: $result")
            }
            return result
        } catch (ex: IOException) {
            val msg: String
            if (sendErrors) {
                msg = TBApplication.ReportServerError(ex, DCPing::class.java, "post()", "server")
            } else {
                msg = ex.message ?: "unknown error"
                Log.e(TAG, "While sending to $target\n$msg")
                if (sendErrors) {
                    TBApplication.ShowError(msg)
                }
            }
            throw(IOException(msg))
        } catch (ex: Exception) {
            val msg: String
            if (sendErrors) {
                msg = TBApplication.ReportServerError(ex, DCPing::class.java, "post()", "server")
            } else {
                msg = ex.message ?: "unknown error"
                if (sendErrors) {
                    TBApplication.ShowError(msg)
                }
            }
            throw(IOException(msg))
        }
    }

    private fun parseResult(result: String): JSONObject {
        try {
            return JSONObject(result)
        } catch (ex: Exception) {
            Timber.e("Got bad result back from server: $result\n$ex.message")
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
            val result = post(messageUrl, jsonObject, false)
            if (Integer.parseInt(result) == 0) {
                db.tableCrash.delete(line)
            } else {
                Log.e(TAG, "Unable to send previously trapped message: " + line.message)
            }
        } catch (ex: IOException) {
            Log.e(TAG, "Exception: " + ex.message)
        } catch (ex: Exception) {
            Log.e(TAG, "Unknown: " + ex.message)
        }
    }

    // region support classes

    private inner class ProjectProcessed(private val db: DatabaseTable) {

        val unprocessed = db.tableProjects.query().toMutableList()

        fun contains(rootName: String, subProject: String): Boolean {
            return find(rootName, subProject) != null
        }

        fun delete(rootName: String?, subProject: String?) {
            if (rootName != null && subProject != null) {
                val ele = find(rootName, subProject)
                if (ele != null) {
                    unprocessed.remove(ele)
                }
            }
        }

        fun disableRemaining() {
            for (project in unprocessed) {
                Timber.i("Project disable or delete: ${project.dashName}")
                db.tableProjects.removeOrDisable(project)
                prefHelper.clearCurProjectIfMatching(project.rootProject, project.subProject)
            }
        }

        private fun find(rootName: String, subProject: String): DataProject? {
            for (ele in unprocessed) {
                if (ele.name == subProject && ele.rootProject == rootName) {
                    return ele
                }
            }
            return null
        }
    }
    // endregion support classes

}

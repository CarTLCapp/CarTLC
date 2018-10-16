/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.pref

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.cartlc.tracker.model.data.*

import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.model.misc.TruckStatus
import com.cartlc.tracker.model.sql.*
import com.cartlc.tracker.model.table.DatabaseTable

import java.io.File
import java.text.SimpleDateFormat

import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by dug on 4/17/17.
 */

class PrefHelper internal constructor(
        ctx: Context,
        private val db: DatabaseTable
) : PrefHelperBase(ctx) {

    companion object {

        val KEY_PROJECT = "project"
        val KEY_COMPANY = "company"
        val KEY_STREET = "street"
        val KEY_STATE = "state"
        val KEY_CITY = "city"
        val KEY_ZIPCODE = "zipcode"
        val KEY_TRUCK = "truck" // Number & License
        val KEY_STATUS = "status"
        internal val KEY_CURRENT_PROJECT_GROUP_ID = "current_project_group_id"
        internal val KEY_SAVED_PROJECT_GROUP_ID = "saved_project_group_id"
        internal val KEY_FIRST_NAME = "first_name"
        internal val KEY_LAST_NAME = "last_name"
        internal val KEY_SECONDARY_FIRST_NAME = "secondary_first_name"
        internal val KEY_SECONDARY_LAST_NAME = "secondary_last_name"
        internal val KEY_HAS_SECONDARY = "has_secondary"
        internal val KEY_TRUCK_NUMBER = "truck_number_string"
        internal val KEY_LICENSE_PLATE = "license_plate"
        internal val KEY_EDIT_ENTRY_ID = "edit_id"
        internal val KEY_NEXT_PICTURE_COLLECTION_ID = "next_picture_collection_id"
        internal val KEY_NEXT_EQUIPMENT_COLLECTION_ID = "next_equipment_collection_id"
        internal val KEY_NEXT_NOTE_COLLECTION_ID = "next_note_collection_id"
        internal val KEY_CURRENT_PICTURE_COLLECTION_ID = "picture_collection_id"
        internal val KEY_TECH_ID = "tech_id"
        internal val KEY_SECONDARY_TECH_ID = "secondary_tech_id"
        internal val KEY_REGISTRATION_CHANGED = "registration_changed"
        internal val KEY_IS_DEVELOPMENT = "is_development"
        internal val KEY_SPECIAL_UPDATE_CHECK = "special_update_check"
        internal val KEY_DO_ERROR_CHECK = "do_error_check"
        internal val KEY_AUTO_ROTATE_PICTURE = "auto_rotate_picture"

        val VERSION_PROJECT = "version_project"
        val VERSION_COMPANY = "version_company"
        val VERSION_EQUIPMENT = "version_equipment"
        val VERSION_NOTE = "version_note"
        val VERSION_TRUCK = "version_truck"

        internal val PICTURE_DATE_FORMAT = "yy-MM-dd_HH:mm:ss"
        internal val VERSION_RESET = -1
    }

    var techID: Int
        get() = getInt(KEY_TECH_ID, 0)
        set(id) = setInt(KEY_TECH_ID, id)

    var secondaryTechID: Int
        get() = getInt(KEY_SECONDARY_TECH_ID, 0)
        set(id) = setInt(KEY_SECONDARY_TECH_ID, id)

    var street: String?
        get() = getString(KEY_STREET, null)
        set(value) = setString(KEY_STREET, value)

    var state: String?
        get() = getString(KEY_STATE, null)
        set(value) = setString(KEY_STATE, value)

    var company: String?
        get() = getString(KEY_COMPANY, null)
        set(value) = setString(KEY_COMPANY, value)

    var city: String?
        get() = getString(KEY_CITY, null)
        set(value) = setString(KEY_CITY, value)

    var zipCode: String?
        get() = getString(KEY_ZIPCODE, null)
        set(value) = setString(KEY_ZIPCODE, value)

    val projectName: String?
        get() = getString(KEY_PROJECT, null)

    val projectId: Long?
        get() {
            projectName?.let {
                val projectNameId = db.projects.queryProjectName(it)
                return if (projectNameId >= 0) {
                    projectNameId
                } else null
            } ?: return null
        }

    var currentProjectGroupId: Long
        get() = getLong(KEY_CURRENT_PROJECT_GROUP_ID, -1L)
        set(id) = setLong(KEY_CURRENT_PROJECT_GROUP_ID, id)

    var savedProjectGroupId: Long
        get() = getLong(KEY_SAVED_PROJECT_GROUP_ID, -1L)
        set(id) = setLong(KEY_SAVED_PROJECT_GROUP_ID, id)

    var currentPictureCollectionId: Long
        get() = getLong(KEY_CURRENT_PICTURE_COLLECTION_ID, 0)
        set(id) = setLong(KEY_CURRENT_PICTURE_COLLECTION_ID, id)

    var firstName: String?
        get() = getString(KEY_FIRST_NAME, null)
        set(name) = setString(KEY_FIRST_NAME, name)

    var lastName: String?
        get() = getString(KEY_LAST_NAME, null)
        set(name) = setString(KEY_LAST_NAME, name)

    var secondaryFirstName: String?
        get() = getString(KEY_SECONDARY_FIRST_NAME, null)
        set(name) = setString(KEY_SECONDARY_FIRST_NAME, name)

    var secondaryLastName: String?
        get() = getString(KEY_SECONDARY_LAST_NAME, null)
        set(name) = setString(KEY_SECONDARY_LAST_NAME, name)

    var isSecondaryEnabled: Boolean
        get() = getInt(KEY_HAS_SECONDARY, 0) != 0
        set(flag) = setInt(KEY_HAS_SECONDARY, if (flag) 1 else 0)

    var truckNumber: String?
        get() = getString(KEY_TRUCK_NUMBER, null)
        set(id) = setString(KEY_TRUCK_NUMBER, id)

    var licensePlate: String?
        get() = getString(KEY_LICENSE_PLATE, null)
        set(id) = setString(KEY_LICENSE_PLATE, id)

    var versionProject: Int
        get() = getInt(VERSION_PROJECT, VERSION_RESET)
        set(value) = setInt(VERSION_PROJECT, value)

    var versionEquipment: Int
        get() = getInt(VERSION_EQUIPMENT, VERSION_RESET)
        set(value) = setInt(VERSION_EQUIPMENT, value)

    var versionNote: Int
        get() = getInt(VERSION_NOTE, VERSION_RESET)
        set(value) = setInt(VERSION_NOTE, value)

    var versionCompany: Int
        get() = getInt(VERSION_COMPANY, VERSION_RESET)
        set(value) = setInt(VERSION_COMPANY, value)

    var versionTruck: Int
        get() = getInt(VERSION_TRUCK, VERSION_RESET)
        set(value) = setInt(VERSION_TRUCK, value)

    var status: TruckStatus?
        get() = TruckStatus.from(getInt(KEY_STATUS, TruckStatus.UNKNOWN.ordinal))
        set(status) = if (status == null) {
            setInt(KEY_STATUS, TruckStatus.UNKNOWN.ordinal)
        } else {
            setInt(KEY_STATUS, status.ordinal)
        }

    var currentEditEntryId: Long
        get() = getLong(KEY_EDIT_ENTRY_ID, 0)
        set(id) = setLong(KEY_EDIT_ENTRY_ID, id)

    val currentEditEntry: DataEntry?
        get() = db.entry.query(currentEditEntryId)

    var doErrorCheck: Boolean
        get() = getInt(KEY_DO_ERROR_CHECK, 1) != 0
        set(flag) = setInt(KEY_DO_ERROR_CHECK, if (flag) 1 else 0)

    val isDevelopment: Boolean
        get() = getInt(KEY_IS_DEVELOPMENT, if (TBApplication.IsDevelopmentServer()) 1 else 0) != 0

    var currentProjectGroup: DataProjectAddressCombo?
        get() {
            val projectGroupId = currentProjectGroupId
            return db.projectAddressCombo.query(projectGroupId)
        }
        set(group) {
            group?.let {
                currentProjectGroupId = group.id
                setProject(group.projectName)
                setAddress(group.address)
            }
        }

    // Note: ID zero has a special meaning, it means that the set is pending.
    val nextPictureCollectionID: Long
        get() = getLong(KEY_NEXT_PICTURE_COLLECTION_ID, 1L)

    val nextEquipmentCollectionID: Long
        get() = getLong(KEY_NEXT_EQUIPMENT_COLLECTION_ID, 0L)

    val nextNoteCollectionID: Long
        get() = getLong(KEY_NEXT_NOTE_COLLECTION_ID, 1L)

    val address: String
        get() {
            val sbuf = StringBuilder()
            val company = company
            if (company != null) {
                sbuf.append(company)
            }
            val street = street
            if (street != null) {
                if (sbuf.length > 0) {
                    sbuf.append("\n")
                }
                sbuf.append(street)
            }
            val city = city
            if (city != null) {
                if (sbuf.length > 0) {
                    sbuf.append("\n")
                }
                sbuf.append(city)
            }
            val state = state
            if (state != null) {
                if (sbuf.length > 0) {
                    sbuf.append(", ")
                }
                sbuf.append(state)
            }
            val zip = zipCode
            if (zip != null) {
                if (sbuf.length > 0) {
                    sbuf.append(" ")
                }
                sbuf.append(zip)
            }
            return sbuf.toString()
        }

    val truckValue: String
        get() {
            val value: String
            val license = licensePlate
            val number = truckNumber
            if (TextUtils.isEmpty(license)) {
                if (number != null) {
                    value = number
                } else {
                    value = ""
                }
            } else {
                if (number != null) {
                    value = DataTruck.toString(number, license)
                } else {
                    value = license ?: ""
                }
            }
            return value
        }

    val numPicturesTaken: Int
        get() {
            val picture_collection_id = currentPictureCollectionId
            return db.pictureCollection.countPictures(picture_collection_id)
        }

    val numEquipPossible: Int
        get() {
            val curGroup = currentProjectGroup
            val collection = db.collectionEquipmentProject.queryForProject(curGroup!!.projectNameId)
            return collection.equipment.size
        }

    val autoRotatePicture: Int
        get() = getInt(KEY_AUTO_ROTATE_PICTURE, 0)

    fun setProject(value: String?) {
        setString(KEY_PROJECT, value)
    }

    fun hasName(): Boolean {
        return !TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)
    }

    fun hasSecondaryName(): Boolean {
        return !TextUtils.isEmpty(secondaryFirstName) && !TextUtils.isEmpty(secondaryLastName)
    }

    fun getKeyValue(key: String): String? {
        return getString(key, null)
    }

    fun setKeyValue(key: String, value: String?) {
        setString(key, value)
    }

    fun reloadFromServer() {
        versionEquipment = VERSION_RESET
        versionProject = VERSION_RESET
        versionNote = VERSION_RESET
        versionCompany = VERSION_RESET
        versionTruck = VERSION_RESET
    }

    fun detectSpecialUpdateCheck() {
        val value = getInt(KEY_SPECIAL_UPDATE_CHECK, 0)
        if (value < 1) {
            reloadFromServer()
            setInt(KEY_SPECIAL_UPDATE_CHECK, 1)
        }
    }

    fun hasRegistrationChanged(): Boolean {
        return getInt(KEY_REGISTRATION_CHANGED, 0) != 0
    }

    fun setRegistrationChanged(flag: Boolean) {
        setInt(KEY_REGISTRATION_CHANGED, if (flag) 1 else 0)
    }

    fun setFromCurrentProjectId() {
        val projectGroup = currentProjectGroup
        if (projectGroup != null) {
            setProject(projectGroup.projectName)
            val address = projectGroup.address
            if (address != null) {
                setAddress(address)
            }
        }
    }

    fun recoverProject() {
        currentProjectGroupId = savedProjectGroupId
        setFromCurrentProjectId()
    }

    fun clearCurProject() {
        clearLastEntry()
        state = null
        city = null
        company = null
        street = null
        zipCode = null
        setProject(null)
        savedProjectGroupId = currentProjectGroupId
        currentProjectGroupId = -1L
        db.equipment.clearChecked()
    }

    fun clearLastEntry() {
        truckNumber = null
        licensePlate = null
        setKeyValue(KEY_TRUCK, null)
        status = null
        currentEditEntryId = 0
        currentPictureCollectionId = 0
        db.pictureCollection.clearPendingPictures()
        db.note.clearValues()
        db.equipment.clearChecked()
    }

    fun saveProjectAndAddressCombo(modifyCurrent: Boolean): Boolean {
        val project = projectName
        if (project.isNullOrBlank()) {
            Timber.e("project name cannot be blank")
            return false
        }
        val company = company
        if (company.isNullOrBlank()) {
            Timber.e("company cannot be blank")
            return false
        }
        val state = state
        if (state.isNullOrBlank()) {
            Timber.e("state cannot be blank.")
            return false
        }
        val street = street
        if (street.isNullOrBlank()) {
            Timber.e("street cannot be blank.")
            return false
        }
        val city = city
        if (city.isNullOrBlank()) {
            Timber.e("city cannot be blank.")
            return false
        }
        val zipcode = zipCode
        var addressId: Long
        addressId = db.address.queryAddressId(company!!, street!!, city!!, state!!, zipcode)
        if (addressId < 0) {
            val address = DataAddress(company, street, city, state, zipcode)
            address.isLocal = true
            addressId = db.address.add(address)
            if (addressId < 0) {
                Timber.e("saveProjectAndAddressCombo(): could not find address: " + address.toString())
                return false
            }
        }
        val projectNameId = db.projects.queryProjectName(project ?: "")
        if (projectNameId < 0) {
            Timber.e("saveProjectAndAddressCombo(): could not find project: $project")
            return false
        }
        var projectGroupId: Long
        if (modifyCurrent) {
            projectGroupId = currentProjectGroupId
            if (projectGroupId < 0) {
                Timber.e("saveProjectAndAddressCombo(): could not modify current project, none is alive")
                return false
            }
            val combo = db.projectAddressCombo.query(projectGroupId) ?: return false
            combo.reset(projectNameId, addressId)
            if (!db.projectAddressCombo.save(combo)) {
                Timber.e("saveProjectAndAddressCombo(): could not update project combo")
                return false
            }
            db.projectAddressCombo.mergeIdenticals(combo)
            val count = db.entry.reUploadEntries(combo)
            if (count > 0) {
                Timber.i("saveProjectAddressCombo(): re-upload $count entries")
            }
            db.projectAddressCombo.updateUsed(projectGroupId)
        } else {
            projectGroupId = db.projectAddressCombo.queryProjectGroupId(projectNameId, addressId)
            if (projectGroupId < 0) {
                projectGroupId = db.projectAddressCombo.add(DataProjectAddressCombo(db, projectNameId, addressId))
            } else {
                db.projectAddressCombo.updateUsed(projectGroupId)
            }
            currentProjectGroupId = projectGroupId
        }
        return true
    }

    internal fun setAddress(address: DataAddress?) {
        company = address!!.company
        street = address.street
        city = address.city
        state = address.state
        zipCode = address.zipcode
    }

    fun incNextPictureCollectionID() {
        setLong(KEY_NEXT_PICTURE_COLLECTION_ID, nextPictureCollectionID + 1)
    }

    fun incNextEquipmentCollectionID() {
        setLong(KEY_NEXT_EQUIPMENT_COLLECTION_ID, nextEquipmentCollectionID + 1)
    }

    fun incNextNoteCollectionID() {
        setLong(KEY_NEXT_NOTE_COLLECTION_ID, nextEquipmentCollectionID + 1)
    }

    fun createEntry(): DataEntry? {
        val projectGroupId = currentProjectGroupId
        if (projectGroupId < 0) {
            return null
        }
        val projectGroup = db.projectAddressCombo.query(projectGroupId) ?: return null
        val entry = DataEntry(db)
        entry.projectAddressCombo = projectGroup
        entry.equipmentCollection = DataCollectionEquipmentEntry(db, nextEquipmentCollectionID)
        entry.equipmentCollection!!.addChecked()
        entry.pictureCollection = db.pictureCollection.createCollectionFromPending(nextPictureCollectionID)
        entry.truckId = db.truck.save(
                truckNumber ?: "",
                licensePlate ?: "",
                projectGroup.projectNameId,
                projectGroup.companyName!!)
        entry.status = status
        entry.saveNotes(nextNoteCollectionID)
        entry.date = System.currentTimeMillis()
        return entry
    }

    fun setFromEntry(entry: DataEntry) {
        currentEditEntryId = entry.id
        currentProjectGroupId = entry.projectAddressCombo!!.id
        currentPictureCollectionId = entry.pictureCollection!!.id
        entry.equipmentCollection!!.setChecked()
        val truck = entry.truck
        if (truck != null) {
            truckNumber = truck.truckNumber
            licensePlate = truck.licensePlateNumber
        } else {
            truckNumber = null
            licensePlate = null
        }
        status = entry.status
        db.note.clearValues()
    }

    fun saveEntry(): DataEntry? {
        val entry = currentEditEntry ?: return createEntry()
        entry.equipmentCollection!!.addChecked()
        var truck = entry.truck
        if (truck == null) {
            truck = DataTruck()
        }
        truck.truckNumber = truckNumber
        truck.licensePlateNumber = licensePlate
        truck.hasEntry = true
        entry.truckId = db.truck.save(truck)

        Log.d("CarTLC", "SAVED TRUCK: " + db.truck.query(entry.truckId)!!.toLongString(db))

        entry.status = status
        entry.uploadedMaster = false
        entry.uploadedAws = false
        entry.hasError = false
        entry.serverErrorCount = 0.toShort()
        // Be careful here: I use the date to match an entry when looking up the server id for older APP versions.
        if (entry.serverId > 0) {
            entry.date = System.currentTimeMillis()
        }
        entry.saveNotes()
        return entry
    }

    fun genPictureFilename(): String {
        val tech_id = techID.toLong()
        val project_id = projectId!!
        val sbuf = StringBuilder()
        sbuf.append("picture_t")
        sbuf.append(tech_id)
        sbuf.append("_p")
        sbuf.append(project_id)
        sbuf.append("_d")
        val fmt = SimpleDateFormat(PICTURE_DATE_FORMAT, Locale.getDefault())
        sbuf.append(fmt.format(Date(System.currentTimeMillis())))
        sbuf.append(".jpg")
        return sbuf.toString()
    }

    fun genFullPictureFile(): File {
        return File(mCtx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), genPictureFilename())
    }

    fun clearUploaded() {
        versionNote = VERSION_RESET
        versionProject = VERSION_RESET
        versionEquipment = VERSION_RESET
        versionCompany = VERSION_RESET
        versionTruck = VERSION_RESET
    }

    fun reloadProjects() {
        versionProject = VERSION_RESET
    }

    fun reloadEquipments() {
        versionEquipment = VERSION_RESET
    }

    fun parseTruckValue(value: String) {
        if (value.contains(":")) {
            val ele = value.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (ele.size >= 2) {
                truckNumber = ele[0].trim { it <= ' ' }
                licensePlate = ele[1].trim { it <= ' ' }
            } else if (ele.size == 1) {
                if (TextUtils.isDigitsOnly(ele[0])) {
                    truckNumber = ele[0].trim { it <= ' ' }
                } else {
                    licensePlate = ele[0].trim { it <= ' ' }
                }
            } else {
                truckNumber = null
                licensePlate = null
            }
        } else if (TextUtils.isDigitsOnly(value)) {
            truckNumber = value
        } else {
            licensePlate = value
        }
    }

    fun incAutoRotatePicture(degrees: Int) {
        val newValue = (autoRotatePicture + degrees) % 360
        setInt(KEY_AUTO_ROTATE_PICTURE, newValue)
    }

    fun clearAutoRotatePicture() {
        setInt(KEY_AUTO_ROTATE_PICTURE, 0)
    }

}

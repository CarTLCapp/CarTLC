/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.data

import android.content.Context

import com.cartlc.tracker.R
import com.cartlc.tracker.model.misc.TruckStatus
import com.cartlc.tracker.model.event.EventRefreshProjects
import com.cartlc.tracker.model.table.DatabaseTable
import org.greenrobot.eventbus.EventBus

import java.text.SimpleDateFormat

import java.util.*

/**
 * Created by dug on 5/13/17.
 */

class DataEntry(private val db: DatabaseTable) {

    companion object {
        internal val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'z"
    }

    var id: Long = 0
    var date: Long = 0
    var projectAddressCombo: DataProjectAddressCombo? = null
    var equipmentCollection: DataCollectionEquipmentEntry? = null
    var pictureCollection: DataPictureCollection? = null
    var noteCollectionId: Long = 0
    var truckId: Long = 0
    var status: TruckStatus? = null
    var serverId: Int = 0
    var serverErrorCount: Short = 0
    var uploadedMaster: Boolean = false
    var uploadedAws: Boolean = false
    var hasError: Boolean = false

    val dateString: String
        get() = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date(date))

    val projectName: String
        get() = projectAddressCombo?.projectName ?: ""

    val project: DataProject?
        get() = projectAddressCombo?.project

    val address: DataAddress?
        get() = projectAddressCombo?.address

    val addressBlock: String?
        get() {
            val address = address
            return address?.block
        }

    val addressLine: String
        get() {
            val address = address
            return address?.line ?: "Invalid"
        }

    // Return all the notes, with values overlaid.
    // Place these values into SqlTableNote as well so they are persisted forward.
    // These are all the notes.
    // Get value overrides
    // Add to result notes without values.
    val notesAllWithValuesOverlaid: List<DataNote>
        get() {
            val allNotes = notesByProject
            val valueNotes = notesWithValuesOnly
            val result = ArrayList<DataNote>()
            for (note in allNotes) {
                val valueNote = getNoteFrom(valueNotes, note)
                if (valueNote != null) {
                    db.note.update(valueNote)
                    result.add(valueNote)
                } else {
                    result.add(note)
                }
            }
            return result
        }

    // Get all the notes as indicated by the project.
    // This will also include any current edits in place as well.
    val notesByProject: List<DataNote>
        get() = db.collectionNoteProject.getNotes(projectAddressCombo!!.projectNameId)

    // Return only the notes with values.
    val notesWithValuesOnly: List<DataNote>
        get() = db.collectionNoteEntry.query(noteCollectionId)

    val notesLine: String
        get() {
            val sbuf = StringBuilder()
            for (note in notesWithValuesOnly) {
                if (!note.value.isNullOrBlank()) {
                    if (sbuf.length > 0) {
                        sbuf.append(", ")
                    }
                    sbuf.append(note.value)
                }
            }
            return sbuf.toString()
        }

    val equipmentNames: List<String>?
        get() = if (equipmentCollection != null) {
            equipmentCollection!!.equipmentNames
        } else null

    val equipment: List<DataEquipment>?
        get() = if (equipmentCollection != null) {
            equipmentCollection!!.equipment
        } else null

    val pictures: List<DataPicture>
        get() = pictureCollection!!.pictures

    val truck: DataTruck?
        get() = db.truck.query(truckId)

    fun getStatus(ctx: Context): String {
        return if (status != null) {
            status!!.getString(ctx)
        } else TruckStatus.UNKNOWN.getString(ctx)
    }

    internal fun getNoteFrom(list: List<DataNote>, check: DataNote): DataNote? {
        for (note in list) {
            if (note.id == check.id) {
                return note
            }
        }
        return null
    }

    fun saveNotes(collectionId: Long) {
        noteCollectionId = collectionId
        db.collectionNoteEntry.save(noteCollectionId, notesByProject)
    }

    fun saveNotes() {
        db.collectionNoteEntry.save(noteCollectionId, notesByProject)
    }

    fun checkPictureUploadComplete(): Boolean {
        for (item in pictureCollection!!.pictures) {
            if (!item.uploaded) {
                return false
            }
        }
        uploadedAws = true
        db.entry.saveUploaded(this)
        return true
    }

    fun getTruckLine(ctx: Context): String {
        val truck = truck
        return truck?.toString() ?: ctx.getString(R.string.error_missing_truck_short)
    }

    fun getEquipmentLine(ctx: Context): String {
        val sbuf = StringBuilder()
        for (name in equipmentNames!!) {
            if (sbuf.length > 0) {
                sbuf.append(", ")
            }
            sbuf.append(name)
        }
        if (sbuf.length == 0) {
            sbuf.append(ctx.getString(R.string.status_no_equipment))
        }
        return sbuf.toString()
    }

    fun toLongString(db: DatabaseTable): String {
        val sbuf = StringBuilder()
        sbuf.append("ID=")
        sbuf.append(id)
        if (projectAddressCombo != null) {
            sbuf.append("\nCOMBO=[")
            sbuf.append(projectAddressCombo!!.toString())
            sbuf.append("]")
        }
        if (equipmentCollection != null) {
            sbuf.append("\nEQUIP=")
            sbuf.append(equipmentCollection!!.toString())
        }
        sbuf.append("\nNOTES=[")
        for (note in notesWithValuesOnly) {
            sbuf.append("[")
            sbuf.append(note.toString())
            sbuf.append("] ")
        }
        sbuf.append("]\n")
        val truck = truck
        if (truck != null) {
            sbuf.append("TRUCK=")
            sbuf.append(truck.toLongString(db))
            sbuf.append("\n")
        }
        if (date > 0) {
            sbuf.append("DATE=")
            sbuf.append(SimpleDateFormat("yy-MM-dd_HH:mm:ss", Locale.getDefault()).format(date))
            sbuf.append(" ")
        }
        sbuf.append("SERVERID=")
        sbuf.append(serverId)
        if (status != null) {
            sbuf.append(", STATUS=")
            sbuf.append(status!!.toString())
        }
        sbuf.append(", FLAGS=[")
        if (uploadedAws) {
            sbuf.append("UPLOADAWS")
        }
        if (uploadedMaster) {
            sbuf.append(" UPLOADMASTER")
        }
        if (hasError) {
            sbuf.append(" ERROR")
        }
        sbuf.append("]")
        return sbuf.toString()
    }

}

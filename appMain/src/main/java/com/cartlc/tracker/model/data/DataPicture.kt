/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.data

import com.cartlc.tracker.ui.util.helper.BitmapHelper

import java.io.File

/**
 * Created by dug on 5/16/17.
 */

class DataPicture(
        _id: Long = 0,
        val unscaledFilename: String,
        _scaledFilename: String? = null,
        _note: String? = null,
        _uploaded: Boolean = false
) {

    companion object {
        internal val MAX_NOTE_LENGTH = 1000
    }

    var id: Long = _id

    var scaledFilename: String? = _scaledFilename
        get() {
            if (field == null) {
                field = BitmapHelper.createScaledFilename(unscaledFilename)
            }
            return field
        }

    var note: String? = _note
        set(value) {
            if (value == null) {
                field = value
            } else {
                if (value.length > MAX_NOTE_LENGTH) {
                    field = value.substring(0, MAX_NOTE_LENGTH)
                } else {
                    field = value
                }
            }
        }

    var uploaded: Boolean = _uploaded

    val existsScaled: Boolean
        get() = scaledFile?.exists() ?: false

    val unscaledFile: File
        get() = File(unscaledFilename)

    val existsUnscaled: Boolean
        get() = unscaledFile.exists()

    val scaledFile: File?
        get() = scaledFilename?.let { return File(scaledFilename) }

    fun buildScaledFile() = scaledFilename?.let {
        BitmapHelper.createScaled(unscaledFile, it)
    } ?: false

    val tailname: String
        get() {
            val pos = unscaledFilename.lastIndexOf("/")
            return if (pos >= 0) {
                unscaledFilename.substring(pos + 1)
            } else unscaledFilename
        }

    fun remove() {
        unscaledFile.delete()
        scaledFile?.delete()
    }

    fun rotateCW(): Int {
        BitmapHelper.rotate(unscaledFile, 90)
        return 90
    }

    fun rotateCCW(): Int {
        BitmapHelper.rotate(unscaledFile, -90)
        return -90
    }

    override fun toString(): String {
        val sbuf = StringBuilder()
        sbuf.append(id)
        sbuf.append(", ")
        sbuf.append(unscaledFilename)
        if (scaledFilename != null) {
            sbuf.append(", ")
            sbuf.append(scaledFilename)
        }
        if (!note.isNullOrEmpty()) {
            sbuf.append(", note=")
            sbuf.append(note)
        }
        if (uploaded) {
            sbuf.append(", UPLOADED")
        }
        return sbuf.toString()
    }
}

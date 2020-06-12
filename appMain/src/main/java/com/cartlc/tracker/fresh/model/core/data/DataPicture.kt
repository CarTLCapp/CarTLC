/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.data

import com.cartlc.tracker.fresh.model.flow.Stage
import com.cartlc.tracker.ui.util.helper.BitmapHelper
import com.cartlc.tracker.ui.util.helper.BitmapResult

import java.io.File

/**
 * Created by dug on 5/16/17.
 */
class DataPicture(
        private val _id: Long = 0,
        val unscaledFilename: String,
        val collectionId: Long? = null,
        val stage: Stage,
        private val _scaledFilename: String? = null,
        private val _uploaded: Boolean = false
) : Comparable<DataPicture> {

    constructor(other: DataPicture, collectionId: Long) :
            this(other._id,
                    other.unscaledFilename,
                    collectionId,
                    other.stage,
                    other._scaledFilename,
                    other._uploaded)

    var id: Long = _id

    var scaledFilename: String? = _scaledFilename
        get() {
            if (field == null) {
                field = BitmapHelper.createScaledFilename(unscaledFilename)
            }
            return field
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

    fun buildScaledFile(): BitmapResult {
        return scaledFilename?.let {
            BitmapHelper.createScaled(unscaledFile, it)
        } ?: run {
            BitmapResult.FILE_NAME_NULL
        }
    }

    val tailname: String
        get() {
            val pos = unscaledFilename.lastIndexOf("/")
            return if (pos >= 0) {
                unscaledFilename.substring(pos + 1)
            } else unscaledFilename
        }

    val file: File?
        get() {
            return when {
                existsUnscaled -> unscaledFile
                existsScaled -> scaledFile
                else -> null
            }
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
        sbuf.append(collectionId)
        sbuf.append(", stage=")
        sbuf.append(stage.toString())
        sbuf.append(", ")
        sbuf.append(unscaledFilename)
        if (scaledFilename != null) {
            sbuf.append(", ")
            sbuf.append(scaledFilename)
        }
        if (uploaded) {
            sbuf.append(", UPLOADED")
        }
        return sbuf.toString()
    }

    override fun compareTo(other: DataPicture): Int {
        return ordinal - other.ordinal
    }

    companion object {
        private const val MULT = 10000
    }

    private val ordinal: Int
        get() {
            if (stage !is Stage.CUSTOM_FLOW) {
                return stage.ord * MULT
            }
            if (stage.isFirstElement) {
                return stage.ord * MULT
            }
            if (stage.isLastElement) {
                return (stage.ord+1) * MULT - 1
            }
            return stage.ord * MULT + stage.flowElementId.toInt() + 1
        }
}

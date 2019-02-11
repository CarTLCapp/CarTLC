/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.util

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

import com.cartlc.tracker.R
import com.cartlc.tracker.model.data.DataEntry
import com.cartlc.tracker.model.event.EventRefreshProjects
import org.greenrobot.eventbus.EventBus

/**
 * Created by dug on 10/14/17.
 */

class CheckError {

    companion object {

        lateinit var instance: CheckError
            internal set

        fun Init() {
            CheckError()
        }
    }

    internal var mDialog: AlertDialog? = null
    internal var mErrorEntry: CheckErrorEntry

    interface CheckErrorResult {
        fun doEdit()
        fun doDelete(entry: DataEntry)
        fun setFromEntry(entry: DataEntry)
    }

    init {
        mErrorEntry = CheckErrorEntry()
        instance = this
    }

    fun cleanup() {
        if (mDialog != null) {
            mDialog!!.dismiss()
            mDialog = null
        }
    }

    internal fun cleanup(dialog: DialogInterface) {
        dialog.dismiss()
        mDialog = null
    }

    fun showTruckError(act: Activity, entry: DataEntry, callback: CheckErrorResult) {
        mErrorEntry.showTruckError(act, entry, callback)
    }

    internal inner class CheckErrorEntry {

        fun showTruckError(act: Activity, entry: DataEntry, callback: CheckErrorResult) {
            val builder = AlertDialog.Builder(act)
            builder.setTitle(R.string.title_error)
            builder.setMessage(getMissingTruckError(act, entry))
            builder.setPositiveButton(R.string.btn_edit) { dialog, _ ->
                callback.setFromEntry(entry)
                cleanup(dialog)
                callback.doEdit()
            }
            builder.setNegativeButton(R.string.btn_delete) { dialog, _ ->
                cleanup(dialog)
                showConfirmDelete(act, callback, entry)
            }
            builder.setNeutralButton(R.string.btn_later) { dialog, _ -> cleanup(dialog) }
            mDialog = builder.create()
            mDialog!!.show()
        }

        fun getMissingTruckError(act: Activity, entry: DataEntry): String {
            val sbuf = StringBuilder()
            sbuf.append(act.getString(R.string.error_missing_truck_long))
            sbuf.append("\n  ")
            sbuf.append(act.getString(R.string.title_project_))
            sbuf.append(" ")
            sbuf.append(entry.projectName)
            sbuf.append("\n  ")
            sbuf.append(entry.addressLine)
            sbuf.append("\n  ")
            sbuf.append(act.getString(R.string.title_status_))
            sbuf.append(" ")
            sbuf.append(entry.getStatus(act))
            sbuf.append("\n  ")
            sbuf.append(act.getString(R.string.title_notes_))
            sbuf.append(" ")
            sbuf.append(entry.notesLine)
            sbuf.append("\n  ")
            sbuf.append(act.getString(R.string.title_equipment_installed_))
            sbuf.append(entry.getEquipmentLine(act))
            return sbuf.toString()
        }

        private fun showConfirmDelete(act: Activity, callback: CheckErrorResult, entry: DataEntry) {
            val builder = AlertDialog.Builder(act)
            builder.setTitle(R.string.title_confirmation)
            builder.setMessage(R.string.dialog_confirm_delete)
            builder.setPositiveButton(R.string.btn_delete) { dialog, _ ->
                cleanup(dialog)
                callback.doDelete(entry)
                EventBus.getDefault().post(EventRefreshProjects())
            }
            builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> cleanup(dialog) }
            mDialog = builder.create()
            mDialog!!.show()
        }

        fun hasValidAddress(entry: DataEntry): Boolean {
            return entry.address!!.hasValidState()
        }
    }



}

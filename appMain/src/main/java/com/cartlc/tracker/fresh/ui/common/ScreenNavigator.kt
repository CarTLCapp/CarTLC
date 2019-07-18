/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.common

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.ui.act.ListEntryActivity
import com.cartlc.tracker.ui.act.VehicleActivity
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.ui.util.CheckError
import java.io.File

class ScreenNavigator(
        private val act: Activity
) {

    companion object {
        private const val PRIVACY_POLICY_URL = "https://www.iubenda.com/privacy-policy/10260978"
    }

    fun dispatchPictureRequest(pictureFile: File, requestCode: Int): Boolean {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(act.packageManager) == null) {
            return false
        }
        val pictureUri = TBApplication.getUri(act, pictureFile)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)
        // Grant permissions
        val resInfoList = act.packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            act.grantUriPermission(packageName, pictureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        // Start Camera activity
        act.startActivityForResult(takePictureIntent, requestCode)
        return true
    }

    fun showTruckError(entry: DataEntry, callback: CheckError.CheckErrorResult) {
        CheckError.instance.showTruckError(act, entry, callback)
    }

    fun showPictureToast(pictureCount: Int) {
        val msgId = when {
            pictureCount <= 0 -> R.string.picture_help_1
            pictureCount <= 1 -> R.string.picture_help_2
            pictureCount <= 2 -> R.string.picture_help_3
            else -> return
        }
        val toast = Toast.makeText(act, msgId, Toast.LENGTH_LONG)
        val top = toast.view as ViewGroup
        val view = top.getChildAt(0)
        if (view is TextView) {
            view.textSize = act.resources.getDimension(R.dimen.picture_toast_size)
        }
        toast.show()
    }

    fun showViewProjectActivity(requestCode: Int) {
        val intent = Intent(act, ListEntryActivity::class.java)
        act.startActivityForResult(intent, requestCode)
    }

    fun showVehiclesActivity() {
        val intent = Intent(act, VehicleActivity::class.java)
        act.startActivity(intent)
    }

    fun showVehiclesPendingDialog() {
        val builder = AlertDialog.Builder(act)
        builder.setTitle(R.string.vehicle_pending_dialog_title)
        builder.setMessage(R.string.vehicle_pending_dialog_message)
        builder.create().show()
    }

    fun showPrivacyPolicy() {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(PRIVACY_POLICY_URL)
        act.startActivity(i)
    }
}
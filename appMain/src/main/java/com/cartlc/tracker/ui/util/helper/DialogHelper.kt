/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.util.helper

import android.app.Activity
import android.content.DialogInterface
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog

import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataNote

/**
 * Created by dug on 12/2/17.
 */

class DialogHelper(
        private var mAct: Activity
) {

    internal var mDialog: AlertDialog? = null

    interface DialogListener {
        fun onOkay()
        fun onCancel()
    }

    fun clearDialog() {
        if (mDialog != null) {
            mDialog!!.dismiss()
            mDialog = null
        }
    }

    fun showMessage(message: String, listener: DialogListener? = null) {
        clearDialog()
        val builder = AlertDialog.Builder(mAct)
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            clearDialog()
            listener?.onOkay()
        }
        mDialog = builder.create()
        mDialog!!.show()
    }

    fun showError(message: String, listener: DialogListener?) {
        clearDialog()
        val builder = AlertDialog.Builder(mAct)
        builder.setTitle(R.string.title_error)
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            clearDialog()
            listener?.onOkay()
        }
        mDialog = builder.create()
        mDialog!!.show()
    }

    fun showServerError(message: String, listener: DialogListener) {
        clearDialog()
        val builder = AlertDialog.Builder(mAct)
        builder.setTitle(R.string.title_error)
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.ok) { _, _ -> clearDialog() }
        builder.setNegativeButton(R.string.btn_stop) { _, _ ->
            clearDialog()
            listener.onCancel()
        }
        mDialog = builder.create()
        mDialog!!.show()
    }

    internal inner class ConfirmDialogData {
        var view: View
        var mQuestions = arrayOfNulls<CheckBox>(7)

        init {
            view = mAct.layoutInflater.inflate(R.layout.confirm_dialog, null)
            setup(0, R.id.question_1)
            setup(1, R.id.question_2)
            setup(2, R.id.question_3)
            setup(3, R.id.question_4)
            setup(4, R.id.question_5)
            setup(5, R.id.question_6)
            setup(6, R.id.question_7)
        }

        fun setup(index: Int, resId: Int) {
            mQuestions[index] = view.findViewById<View>(resId) as CheckBox
            mQuestions[index]!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener
            { _, _ -> mDialog!!.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = confirmAllDone() })
        }

        fun confirmAllDone(): Boolean {
            for (i in mQuestions.indices) {
                if (!mQuestions[i]!!.isChecked) {
                    return false
                }
            }
            return true
        }
    }

    fun showConfirmDialog(listener: DialogListener) {
        clearDialog()
        val custom = ConfirmDialogData()
        val builder = AlertDialog.Builder(mAct)
        builder.setTitle(R.string.confirm_title)
        builder.setView(custom.view)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            clearDialog()
            listener.onOkay()
        }
        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
            clearDialog()
            listener.onCancel()
        }
        mDialog = builder.create()
        mDialog!!.setOnShowListener { mDialog!!.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false }
        mDialog!!.show()
    }

}

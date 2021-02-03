/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.common

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.msg.MessageHandler
import com.cartlc.tracker.fresh.model.msg.StringMessage
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.cartlc.tracker.ui.util.helper.DialogHelper

class DialogNavigator(
        private val context: Context,
        private val messageHandler: MessageHandler
) {

    private val layoutInflater = LayoutInflater.from(context)

    fun showNoteError(notes: List<DataNote>, onOkay: () -> Unit = {}) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.title_notes)
        val sbuf = StringBuilder()
        for (note in notes) {
            if (note.numDigits > 0 && note.valueLength() > 0 && note.valueLength() != note.numDigits.toInt()) {
                sbuf.append("    ")
                sbuf.append(note.name)
                sbuf.append(": ")
                sbuf.append(messageHandler.getString(StringMessage.error_incorrect_note_count(note.valueLength(), note.numDigits.toInt())))
                sbuf.append("\n")
            }
        }
        val msg = messageHandler.getString(StringMessage.error_incorrect_digit_count(sbuf.toString()))
        builder.setMessage(msg)
        builder.setPositiveButton(android.R.string.yes) { dialog, _ -> showNoteErrorOk(dialog, onOkay) }
        builder.setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showNoteErrorOk(dialog: DialogInterface, onOkay: () -> Unit) {
        onOkay()
        dialog.dismiss()
    }

    fun showDialog(msg: String, onDone: () -> Unit) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setMessage(msg)
        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
            onDone()
        }
        builder.create().show()
    }

    fun showTruckDamageQuery(onDone: (answer: Boolean) -> Unit) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle(R.string.truck_damage_query)
        builder.setPositiveButton(R.string.word_yes) { dialog, _ ->
            dialog.dismiss()
            onDone(true)
        }
        builder.setNegativeButton(R.string.word_no) { dialog, _ ->
            dialog.dismiss()
            onDone(false)
        }
        builder.create().show()
    }

    fun showPartialInstallReasonDialog(initialValue: String?, onDone: (reason: String?) -> Unit) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle(R.string.dialog_partial_install_title)
        val view: View = layoutInflater.inflate(R.layout.dialog_partial_install, null)
        val editView = view.findViewById<EditText>(R.id.value)
        editView.setText(initialValue)
        builder.setView(view)
        builder.setPositiveButton(R.string.btn_done) { dialog, _ ->
            val reason = editView.text.toString()
            dialog.dismiss()
            onDone(reason)
        }
        builder.create().show()
    }

    fun showDebugDialog(key: String, onDone: (code: String?) -> Unit) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle(R.string.title_debug)
        val view: View = layoutInflater.inflate(R.layout.dialog_debug, null)
        val labelView = view.findViewById<TextView>(R.id.label)
        labelView.text = context.getString(R.string.dialog_debug_code, key)
        val editView = view.findViewById<EditText>(R.id.value)
        builder.setView(view)
        builder.setPositiveButton(R.string.btn_done) { dialog, _ ->
            val code = editView.text.toString()
            dialog.dismiss()
            onDone(code)
        }
        builder.create().show()
    }

    // region showFinalConfirmDialog()

    private var dialog: AlertDialog? = null

    private inner class FinalConfirmDialogData {
        var view: View = layoutInflater.inflate(R.layout.dialog_final_confirm, null)
        var mQuestions = arrayOfNulls<CheckBox>(7)

        init {
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
            mQuestions[index]!!.setOnCheckedChangeListener() { _, _ ->
                dialog!!.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = confirmAllDone()
            }
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

    fun showFinalConfirmDialog(listener: DialogHelper.DialogListener) {
        clearDialog()
        val custom = FinalConfirmDialogData()
        val builder = AlertDialog.Builder(context)
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
        dialog = builder.create()
        dialog!!.setOnShowListener { dialog!!.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false }
        dialog!!.show()
    }

    fun clearDialog() {
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
    }

    // endregion showFinalConfirmDialog()


//    fun showPictureNoteDialog(item: DataPicture, onDone: () -> Unit) {
//        val builder = android.app.AlertDialog.Builder(context)
//        val noteView = layoutInflater.inflate(R.layout.picture_note, null)
//        builder.setView(noteView)
//
//        val edt = noteView.findViewById<View>(R.id.note) as EditText
//        edt.setText(item.note)
//
//        builder.setTitle(R.string.picture_note_title)
//        builder.setPositiveButton("Done") { dialog, _ ->
//            item.note = edt.text.toString().trim { it <= ' ' }
//            dialog.dismiss()
//            onDone()
//        }
//        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
//        val b = builder.create()
//        b.show()
//    }

}


package com.cartlc.tracker.fresh.ui.common

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.model.msg.MessageHandler
import com.cartlc.tracker.model.msg.StringMessage
import android.content.Context

class DialogNavigator(
        private val context: Context,
        private val messageHandler: MessageHandler
) {

    fun showNoteError(notes: List<DataNote>, onOkay: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.title_notes)
        val sbuf = StringBuilder()
        for (note in notes) {
            if (note.num_digits > 0 && note.valueLength() > 0 && note.valueLength() != note.num_digits.toInt()) {
                sbuf.append("    ")
                sbuf.append(note.name)
                sbuf.append(": ")
                sbuf.append(messageHandler.getString(StringMessage.error_incorrect_note_count(note.valueLength(), note.num_digits.toInt())))
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

}
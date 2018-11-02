package com.cartlc.tracker.ui.act

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cartlc.tracker.R
import com.cartlc.tracker.model.misc.EntryHint
import com.cartlc.tracker.model.misc.ErrorMessage
import com.cartlc.tracker.ui.util.DialogHelper
import kotlinx.android.synthetic.main.content_main.*

open class BaseActivity : AppCompatActivity() {

    protected lateinit var dialogHelper: DialogHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogHelper = DialogHelper(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogHelper.clearDialog()
    }

    fun showError(error: ErrorMessage) {
        return showError(getErrorMessage(error))
    }

    protected fun showError(error: String) {
        dialogHelper.showError(error, object : DialogHelper.DialogListener {
            override fun onOkay() {
                onErrorDialogOkay()
            }

            override fun onCancel() {}
        })
    }

    open protected fun onErrorDialogOkay() {}

    private fun getErrorMessage(error: ErrorMessage): String =
            getString(when (error) {
                ErrorMessage.ENTER_YOUR_NAME -> R.string.error_enter_your_name
                ErrorMessage.NEED_A_TRUCK -> R.string.error_need_a_truck_number
                ErrorMessage.NEED_NEW_COMPANY -> R.string.error_need_new_company
                ErrorMessage.NEED_EQUIPMENT -> R.string.error_need_equipment
                ErrorMessage.NEED_COMPANY -> R.string.error_need_company
                ErrorMessage.NEED_STATUS -> R.string.error_need_status
            })

    fun showEntryHint(entryHint: EntryHint) {
        if (entryHint.message.isNotEmpty()) {
            list_entry_hint.setText(entryHint.message)
            if (entryHint.isError) {
                list_entry_hint.setTextColor(ContextCompat.getColor(this, R.color.entry_error_color))
            } else {
                list_entry_hint.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
            list_entry_hint.visibility = View.VISIBLE
        } else {
            list_entry_hint.visibility = View.GONE
        }
    }

}
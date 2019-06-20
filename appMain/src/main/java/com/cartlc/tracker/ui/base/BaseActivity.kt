package com.cartlc.tracker.ui.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cartlc.tracker.R
import com.cartlc.tracker.model.misc.EntryHint
import com.cartlc.tracker.model.msg.ErrorMessage
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.ComponentRoot
import com.cartlc.tracker.ui.util.helper.DialogHelper
import kotlinx.android.synthetic.main.content_main.*

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var dialogHelper: DialogHelper

    lateinit var boundAct: BoundAct
    lateinit var componentRoot: ComponentRoot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        componentRoot = (applicationContext as TBApplication).componentRoot
        boundAct = BoundAct(this)
        dialogHelper = boundAct.dialogHelper
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogHelper.clearDialog()
    }

    fun showError(error: ErrorMessage) {
        return showError(componentRoot.messageHandler.getErrorMessage(error))
    }

    protected fun showError(error: String) {
        dialogHelper.showError(error, object : DialogHelper.DialogListener {
            override fun onOkay() { onErrorDialogOkay() }
            override fun onCancel() {}
        })
    }

    protected open fun onErrorDialogOkay() {}

    fun showEntryHint(entryHint: EntryHint) {
        if (entryHint.message.isNotEmpty()) {
            list_entry_hint.text = entryHint.message
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

    // region Fragment Management
    // endregion Fragment Management
}
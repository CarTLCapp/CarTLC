package com.cartlc.tracker.ui.act

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.R
import com.cartlc.tracker.model.misc.EntryHint
import com.cartlc.tracker.model.misc.ErrorMessage
import com.cartlc.tracker.model.misc.StringMessage
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
            override fun onOkay() { onErrorDialogOkay() }
            override fun onCancel() {}
        })
    }

    protected open fun onErrorDialogOkay() {}

    private fun getErrorMessage(error: ErrorMessage): String =
            getString(when (error) {
                ErrorMessage.NEED_A_TRUCK -> R.string.error_need_a_truck_number
                ErrorMessage.NEED_NEW_COMPANY -> R.string.error_need_new_company
                ErrorMessage.NEED_EQUIPMENT -> R.string.error_need_equipment
                ErrorMessage.NEED_COMPANY -> R.string.error_need_company
                ErrorMessage.NEED_STATUS -> R.string.error_need_status
                ErrorMessage.CANNOT_TAKE_PICTURE -> R.string.error_cannot_take_picture
            })

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

    protected fun getStringMessage(msg: StringMessage): String =
            when (msg) {
                StringMessage.entry_hint_edit_project -> getString(R.string.entry_hint_edit_project)
                StringMessage.entry_hint_truck -> getString(R.string.entry_hint_truck)
                StringMessage.btn_add -> getString(R.string.btn_add)
                StringMessage.btn_prev -> getString(R.string.btn_prev)
                StringMessage.btn_next -> getString(R.string.btn_next)
                StringMessage.btn_edit -> getString(R.string.btn_edit)
                StringMessage.btn_new_project -> getString(R.string.btn_new_project)
                StringMessage.btn_another -> getString(R.string.btn_another)
                StringMessage.btn_done -> getString(R.string.btn_done)
                StringMessage.btn_confirm -> getString(R.string.btn_confirm)
                StringMessage.title_current_project -> getString(R.string.title_current_project)
                StringMessage.title_login -> getString(R.string.title_login)
                StringMessage.title_project -> getString(R.string.title_project)
                StringMessage.title_company -> getString(R.string.title_company)
                StringMessage.title_state -> getString(R.string.title_state)
                StringMessage.title_city -> getString(R.string.title_city)
                StringMessage.title_street -> getString(R.string.title_street)
                StringMessage.title_truck -> getString(R.string.title_truck)
                StringMessage.title_equipment -> getString(R.string.title_equipment)
                StringMessage.title_equipment_installed -> getString(R.string.title_equipment_installed)
                StringMessage.title_notes -> getString(R.string.title_notes)
                StringMessage.title_status -> getString(R.string.title_status)
                StringMessage.title_confirmation -> getString(R.string.title_confirmation)
                StringMessage.title_photo -> getString(R.string.title_photo)
                is StringMessage.title_photos -> getString(R.string.title_photos, msg.count)
                is StringMessage.status_installed_equipments -> getString(R.string.status_installed_equipments, msg.checkedEquipment, msg.maxEquip)
                is StringMessage.status_installed_pictures -> getString(R.string.status_installed_pictures, msg.countPictures)
            }

}
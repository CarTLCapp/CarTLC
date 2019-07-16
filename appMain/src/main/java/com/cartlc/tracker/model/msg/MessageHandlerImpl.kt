package com.cartlc.tracker.model.msg

import android.content.Context
import com.cartlc.tracker.R

class MessageHandlerImpl(
        private val ctx: Context
) : MessageHandler {

    override fun getString(msg: StringMessage): String =
            when (msg) {
                StringMessage.entry_hint_edit_project -> ctx.getString(R.string.entry_hint_edit_project)
                StringMessage.entry_hint_truck -> ctx.getString(R.string.entry_hint_truck)
                StringMessage.btn_add -> ctx.getString(R.string.btn_add)
                StringMessage.btn_prev -> ctx.getString(R.string.btn_prev)
                StringMessage.btn_next -> ctx.getString(R.string.btn_next)
                StringMessage.btn_edit -> ctx.getString(R.string.btn_edit)
                StringMessage.btn_new_project -> ctx.getString(R.string.btn_new_project)
                StringMessage.btn_another -> ctx.getString(R.string.btn_another)
                StringMessage.btn_done -> ctx.getString(R.string.btn_done)
                StringMessage.btn_confirm -> ctx.getString(R.string.btn_confirm)
                StringMessage.title_current_project -> ctx.getString(R.string.title_current_project)
                StringMessage.title_login -> ctx.getString(R.string.title_login)
                StringMessage.title_root_project -> ctx.getString(R.string.title_root_project)
                StringMessage.title_sub_project -> ctx.getString(R.string.title_sub_project)
                StringMessage.title_company -> ctx.getString(R.string.title_company)
                StringMessage.title_state -> ctx.getString(R.string.title_state)
                StringMessage.title_city -> ctx.getString(R.string.title_city)
                StringMessage.title_street -> ctx.getString(R.string.title_street)
                StringMessage.title_truck -> ctx.getString(R.string.title_truck)
                StringMessage.title_equipment -> ctx.getString(R.string.title_equipment)
                StringMessage.title_equipment_installed -> ctx.getString(R.string.title_equipment_installed)
                StringMessage.title_notes -> ctx.getString(R.string.title_notes)
                StringMessage.title_status -> ctx.getString(R.string.title_status)
                StringMessage.title_confirmation -> ctx.getString(R.string.title_confirmation)
                StringMessage.title_photo -> ctx.getString(R.string.title_photo)
                StringMessage.title_entries_ -> ctx.getString(R.string.title_entries_)
                StringMessage.title_uploaded_done -> ctx.getString(R.string.title_uploaded_done)
                is StringMessage.dialog_dialog_entry_done -> ctx.getString(R.string.dialog_entry_done, msg.name)
                is StringMessage.dialog_dialog_entry_done2 -> ctx.getString(R.string.dialog_entry_done2, msg.name, msg.name2)
                is StringMessage.title_photos -> ctx.getString(R.string.title_photos, msg.count)
                is StringMessage.status_installed_equipments -> ctx.getString(R.string.status_installed_equipments, msg.checkedEquipment, msg.maxEquip)
                is StringMessage.status_installed_pictures -> ctx.getString(R.string.status_installed_pictures, msg.countPictures)
                is StringMessage.error_incorrect_note_count -> ctx.getString(R.string.error_incorrect_note_count, msg.length, msg.digits)
                is StringMessage.error_incorrect_digit_count -> ctx.getString(R.string.error_incorrect_digit_count, msg.msg)
            }

    override fun getErrorMessage(error: ErrorMessage): String =
            ctx.getString(when (error) {
                ErrorMessage.NEED_A_TRUCK -> R.string.error_need_a_truck_number
                ErrorMessage.NEED_NEW_COMPANY -> R.string.error_need_new_company
                ErrorMessage.NEED_EQUIPMENT -> R.string.error_need_equipment
                ErrorMessage.NEED_COMPANY -> R.string.error_need_company
                ErrorMessage.NEED_STATUS -> R.string.error_need_status
                ErrorMessage.CANNOT_TAKE_PICTURE -> R.string.error_cannot_take_picture
            })
}
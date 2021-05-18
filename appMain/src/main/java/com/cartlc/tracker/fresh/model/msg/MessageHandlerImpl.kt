package com.cartlc.tracker.fresh.model.msg

import android.content.Context
import androidx.annotation.StringRes
import com.cartlc.tracker.R

class MessageHandlerImpl(
        private val ctx: Context
) : MessageHandler {

    override fun getString(msg: StringMessage): String =
            when (msg) {

                is StringMessage.dialog_dialog_entry_done -> getString(msg.text, msg.name)
                is StringMessage.dialog_dialog_entry_done2 -> getString(msg.text, msg.name, msg.name2)

                is StringMessage.prompt_custom_photo_1 -> getString(msg.text, msg.prompt)
                is StringMessage.prompt_custom_photo_N -> getString(msg.text, msg.count, msg.prompt)
                is StringMessage.prompt_custom_photo_more -> getString(msg.text, msg.count, msg.prompt)
                is StringMessage.prompt_notes -> getString(msg.text, msg.prompt)

                is StringMessage.title_elements -> getString(msg.text, msg.count)
                is StringMessage.title_photos -> getString(msg.text, msg.count, msg.max)

                is StringMessage.status_installed_equipments -> getString(msg.text, msg.checkedEquipment, msg.maxEquip)
                is StringMessage.status_installed_pictures -> getString(msg.text, msg.countPictures)
                is StringMessage.status_notes_used -> getString(msg.text, msg.countNotes)

                is StringMessage.error_incorrect_note_count -> getString(msg.text, msg.length, msg.digits)
                is StringMessage.error_incorrect_digit_count -> getString(msg.text, msg.msg)

                is StringMessage.note_incomplete -> {
                    when {
                        msg.size > 0 -> {
                            getString(R.string.entry_incomplete2, msg.progress, msg.size)
                        }
                        msg.progress > 0 -> {
                            getString(R.string.entry_incomplete1, msg.progress)
                        }
                        else -> {
                            getString(R.string.entry_incomplete)
                        }
                    }
                }
                is StringMessage.hours_time_start_hint_value -> getString(msg.text, msg.value)
                is StringMessage.hours_time_end_hint_value -> getString(msg.text, msg.value)

                else -> {
                    getString(msg.text)
                }
            }

    override fun getErrorMessage(error: ErrorMessage): String =
            getString(when (error) {
                ErrorMessage.NEED_A_TRUCK -> R.string.error_need_a_truck_number
                ErrorMessage.NEED_NEW_COMPANY -> R.string.error_need_new_company
                ErrorMessage.NEED_EQUIPMENT -> R.string.error_need_equipment
                ErrorMessage.NEED_COMPANY -> R.string.error_need_company
                ErrorMessage.NEED_STATUS -> R.string.error_need_status
                ErrorMessage.CANNOT_TAKE_PICTURE -> R.string.error_cannot_take_picture
            })

    private fun getString(@StringRes text: Int): String {
        return ctx.getString(text)
    }

    private fun getString(@StringRes text: Int, arg1: String): String {
        return  ctx.getString(text, arg1)
    }

    private fun getString(@StringRes text: Int, arg1: Int): String {
        return  ctx.getString(text, arg1)
    }

    private fun getString(@StringRes text: Int, arg1: String, arg2: String): String {
        return  ctx.getString(text, arg1, arg2)
    }

    private fun getString(@StringRes text: Int, arg1: Int, arg2: String): String {
        return  ctx.getString(text, arg1, arg2)
    }

    private fun getString(@StringRes text: Int, arg1: Int, arg2: Int): String {
        return  ctx.getString(text, arg1, arg2)
    }

}
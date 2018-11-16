package com.cartlc.tracker.model.misc

sealed class StringMessage {
    object entry_hint_edit_project : StringMessage()
    data class status_installed_equipments(val checkedEquipment: Int, val maxEquip: Int) : StringMessage()
    data class status_installed_pictures(val countPictures: Int) : StringMessage()
}
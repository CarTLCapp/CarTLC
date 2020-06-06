/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.ui.confirm.data

sealed class ConfirmDataType(
        val ord: Int
) {

    data class BASICS(val data: ConfirmDataBasics) : ConfirmDataType(ORD_BASICS)
    data class EQUIPMENT(val data: ConfirmDataEquipment): ConfirmDataType(ORD_EQUIPMENT)
    data class NOTES(val data: ConfirmDataNotes): ConfirmDataType(ORD_NOTES)
    data class PICTURES(val data: ConfirmDataPicture): ConfirmDataType(ORD_PICTURES)

    companion object {

        const val ORD_BASICS = 0
        const val ORD_EQUIPMENT = 1
        const val ORD_NOTES = 2
        const val ORD_PICTURES = 3

    }
}
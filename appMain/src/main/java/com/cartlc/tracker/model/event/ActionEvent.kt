package com.cartlc.tracker.model.event

import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.ui.util.CheckError
import java.io.File

enum class ButtonDialog {
    YES,
    NO
}

sealed class Action {
    object CONFIRM_DIALOG : Action()
    object SUBMIT : Action()
    object NEW_PROJECT : Action()
    object VIEW_PROJECT : Action()
    object VEHICLES : Action()
    object VEHICLES_PENDING : Action()
    object GET_LOCATION : Action()
    object PING: Action()
    object STORE_ROTATION: Action()
    object ADD_PICTURE: Action()
    object SHOW_NOTE_ERROR: Action()
    data class RETURN_PRESSED(val text: String): Action()
    data class BUTTON_DIALOG(val button: ButtonDialog) : Action()
    data class PICTURE_REQUEST(val file: File) : Action()
    data class SHOW_TRUCK_ERROR(val entry: DataEntry, val callback: CheckError.CheckErrorResult) : Action()
    data class SHOW_PICTURE_TOAST(val count: Int): Action()
    data class SET_MAIN_LIST(val list: List<String>) : Action()
    data class SET_PICTURE_LIST(val list: List<DataPicture>): Action()
    data class CONFIRMATION_FILL(val entry: DataEntry): Action()
}

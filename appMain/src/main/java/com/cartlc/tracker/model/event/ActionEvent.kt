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
    object SUBMIT : Action()
    object NEW_PROJECT : Action()
    object VIEW_PROJECT : Action()
    object ADD_PICTURE: Action()
    data class RETURN_PRESSED(val text: String): Action()
    data class BUTTON_DIALOG(val button: ButtonDialog) : Action()
}

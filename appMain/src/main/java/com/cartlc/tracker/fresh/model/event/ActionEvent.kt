package com.cartlc.tracker.fresh.model.event

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

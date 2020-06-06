package com.cartlc.tracker.fresh.model.event

enum class Button {
    BTN_PREV,
    BTN_NEXT,
    BTN_CENTER,
    BTN_CHANGE
}

class ButtonEvent(value: Button) : LiveEvent<Button>(value)
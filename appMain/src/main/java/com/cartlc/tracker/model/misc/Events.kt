package com.cartlc.tracker.model.misc

class ConfirmDialogEvent : LiveEvent<String>("confirm")

class DispatchPictureRequestEvent : LiveEvent<String>("picture")

class EntrySimpleReturnEvent(value: String) : LiveEvent<String>(value)
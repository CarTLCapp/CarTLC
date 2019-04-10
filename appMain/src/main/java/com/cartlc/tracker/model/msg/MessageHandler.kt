package com.cartlc.tracker.model.msg

interface MessageHandler {

    fun getString(msg: StringMessage): String
    fun getErrorMessage(error: ErrorMessage): String

}
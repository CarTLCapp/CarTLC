package com.cartlc.tracker.fresh.model.msg

interface MessageHandler {

    fun getString(msg: StringMessage): String
    fun getErrorMessage(error: ErrorMessage): String

}
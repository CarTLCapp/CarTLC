package com.cartlc.tracker.ui.util.helper

sealed class BitmapResult {
    data class FILE_NOT_FOUND(val filename: String): BitmapResult()
    object MEDIA_NOT_MOUNTED : BitmapResult()
    data class EXCEPTION(val message: String): BitmapResult()
    object FILE_NAME_NULL: BitmapResult()
    object OK: BitmapResult()
}
package com.cartlc.tracker.fresh.ui.bits

import android.text.Editable
import android.text.TextWatcher

open class TextWatcherImpl : TextWatcher {

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}
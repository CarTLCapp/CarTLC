package com.cartlc.tracker.fresh.ui.bits

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import com.cartlc.tracker.fresh.ui.common.observable.BaseObservableImpl

class HideOnSoftKeyboard(
        private val root: ViewGroup
) : BaseObservableImpl<HideOnSoftKeyboard.Listener>(), ViewTreeObserver.OnGlobalLayoutListener {

    interface Listener {

        fun onSoftKeyboardVisible()
        fun onSoftKeyboardHidden()

    }

    companion object {
        private const val ratio = 0.15
    }

    private val imm: InputMethodManager by lazy {
        root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    init {
        root.viewTreeObserver?.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        val rect = Rect()

        root.rootView?.getWindowVisibleDisplayFrame(rect)
        val screenHeight = root.rootView.height
        val keypadHeight = screenHeight - rect.bottom
        if (keypadHeight > screenHeight * ratio) {
            for (listener in listeners) {
                listener.onSoftKeyboardVisible()
            }
        } else {
            for (listener in listeners) {
                listener.onSoftKeyboardHidden()
            }
        }

    }

    fun hideKeyboard(v: View) {
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

}

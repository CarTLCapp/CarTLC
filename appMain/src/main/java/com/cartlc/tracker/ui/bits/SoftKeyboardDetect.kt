package com.cartlc.tracker.ui.bits

import android.graphics.Rect
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.callassistant.util.observable.BaseObservableImpl

class SoftKeyboardDetect(
        private val root: ViewGroup
) : BaseObservableImpl<SoftKeyboardDetect.Listener>(), ViewTreeObserver.OnGlobalLayoutListener {

    interface Listener {

        fun onSoftKeyboardVisible()
        fun onSoftKeyboardHidden()

    }

    companion object {
        private const val ratio = 0.15
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

}

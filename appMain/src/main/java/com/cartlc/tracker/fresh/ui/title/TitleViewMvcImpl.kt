package com.cartlc.tracker.fresh.ui.title

import android.view.View
import android.widget.TextView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl

class TitleViewMvcImpl(
        override val rootView: View
) : ViewMvcImpl(), TitleViewMvc {

    private val mainTitleTextView = findViewById<TextView>(R.id.main_title_text)
    private val subTitleTextView = findViewById<TextView>(R.id.sub_title)
    private val separatorView = findViewById<View>(R.id.main_title_separator)

    override var mainTitleVisible: Boolean
        get() = mainTitleTextView.visibility == View.VISIBLE
        set(value) {
            mainTitleTextView.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var mainTitleText: String?
        get() = mainTitleTextView.text.toString().trim { it <= ' ' }
        set(value) {
            mainTitleTextView.text = value
        }

    override var subTitleVisible: Boolean
        get() = subTitleTextView.visibility == View.VISIBLE
        set(value) {
            subTitleTextView.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var subTitleText: String?
        get() = subTitleTextView.text.toString().trim { it <= ' ' }
        set(value) {
            subTitleTextView.text = value
        }

    override var separatorVisible: Boolean
        get() = separatorView.visibility == View.VISIBLE
        set(value) {
            separatorView.visibility = if (value) View.VISIBLE else View.GONE
        }

}
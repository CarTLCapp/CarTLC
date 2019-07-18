package com.cartlc.tracker.fresh.ui.login

import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvcImpl
import com.cartlc.tracker.ui.bits.TextWatcherImpl

class LoginViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ObservableViewMvcImpl<LoginViewMvc.Listener>(), LoginViewMvc {

    override val rootView: View = inflater.inflate(R.layout.frame_login, container, false) as ViewGroup

    private val firstTechCodeView = findViewById<EditText>(R.id.first_tech_code)
    private val firstTechNameView = findViewById<TextView>(R.id.first_tech_name)
    private val secondaryTechCodeView = findViewById<EditText>(R.id.secondary_tech_code)
    private val secondaryTechNameView = findViewById<TextView>(R.id.secondary_tech_name)
    private val secondaryLoginCheckBox = findViewById<CheckBox>(R.id.secondary_login)

    init {
        firstTechCodeView.addTextChangedListener(object : TextWatcherImpl() {
            override fun afterTextChanged(s: Editable?) {
                for (listener in listeners) {
                    listener.onFirstTechCodeChanged(s.toString())
                }
            }
        })
        secondaryTechCodeView.addTextChangedListener(object : TextWatcherImpl() {
            override fun afterTextChanged(s: Editable?) {
                for (listener in listeners) {
                    listener.onSecondaryTechCodeChanged(s.toString())
                }
            }
        })
        secondaryLoginCheckBox.setOnCheckedChangeListener { _, flag ->
            for (listener in listeners) {
                listener.onSecondaryCheckBoxChanged(flag)
            }
        }
    }

    override var firstTechCode: String
        get() = firstTechCodeView.text.toString()
        set(value) { firstTechCodeView.setText(value) }

    override var firstTechName: String
        get() = firstTechNameView.text.toString()
        set(value) { firstTechNameView.text = value }

    override var secondaryTechCode: String
        get() = secondaryTechCodeView.text.toString()
        set(value) { secondaryTechCodeView.setText(value) }

    override var secondaryTechName: String
        get() = secondaryTechNameView.text.toString()
        set(value) { secondaryTechNameView.text = value }

    override var secondaryTechCodeEnabled: Boolean
        get() = secondaryTechCodeView.isEnabled
        set(value) { secondaryTechCodeView.isEnabled = value }

    override var secondaryCheckBoxChecked: Boolean
        get() = secondaryLoginCheckBox.isChecked
        set(value) { secondaryLoginCheckBox.isChecked = value }
}
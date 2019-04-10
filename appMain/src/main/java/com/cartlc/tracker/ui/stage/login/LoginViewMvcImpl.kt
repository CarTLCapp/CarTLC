package com.cartlc.tracker.ui.stage.login

import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import com.callassistant.util.viewmvc.ObservableViewMvcImpl
import com.cartlc.tracker.R
import com.cartlc.tracker.ui.bits.TextWatcherImpl

class LoginViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ObservableViewMvcImpl<LoginViewMvc.Listener>(), LoginViewMvc {

    override val rootView: View = inflater.inflate(R.layout.frame_login, container, false) as ViewGroup

    private val firstNameView = findViewById<EditText>(R.id.first_name)
    private val lastNameView = findViewById<EditText>(R.id.last_name)
    private val secondaryFirstNameView = findViewById<EditText>(R.id.secondary_first_name)
    private val secondaryLastNameView = findViewById<EditText>(R.id.secondary_last_name)
    private val secondaryLoginCheckBox = findViewById<CheckBox>(R.id.secondary_login)

    init {
        firstNameView.addTextChangedListener(object : TextWatcherImpl() {
            override fun afterTextChanged(s: Editable?) {
                for (listener in listeners) {
                    listener.onFirstNameChanged(s.toString())
                }
            }
        })
        lastNameView.addTextChangedListener(object : TextWatcherImpl() {
            override fun afterTextChanged(s: Editable?) {
                for (listener in listeners) {
                    listener.onLastNameChanged(s.toString())
                }
            }
        })
        secondaryFirstNameView.addTextChangedListener(object : TextWatcherImpl() {
            override fun afterTextChanged(s: Editable?) {
                for (listener in listeners) {
                    listener.onSecondaryFirstNameChanged(s.toString())
                }
            }
        })
        secondaryLastNameView.addTextChangedListener(object : TextWatcherImpl() {
            override fun afterTextChanged(s: Editable?) {
                for (listener in listeners) {
                    listener.onSecondaryLastNameChanged(s.toString())
                }
            }
        })
        secondaryLoginCheckBox.setOnCheckedChangeListener { _, flag ->
            for (listener in listeners) {
                listener.onSecondaryCheckBoxChanged(flag)
            }
        }
    }

    override var firstName: String
        get() = firstNameView.text.toString()
        set(value) { firstNameView.setText(value) }

    override var lastName: String
        get() = lastNameView.text.toString()
        set(value) { lastNameView.setText(value) }

    override var secondaryFirstName: String
        get() = secondaryFirstNameView.text.toString()
        set(value) { secondaryFirstNameView.setText(value) }

    override var secondaryLastName: String
        get() = secondaryLastNameView.text.toString()
        set(value) { secondaryLastNameView.setText(value) }

    override var secondaryFirstNameEnabled: Boolean
        get() = secondaryFirstNameView.isEnabled
        set(value) { secondaryFirstNameView.isEnabled = value }

    override var secondaryLastNameEnabled: Boolean
        get() = secondaryLastNameView.isEnabled
        set(value) { secondaryLastNameView.isEnabled = value }

    var secondaryCheckBoxChecked: Boolean
        get() = secondaryLoginCheckBox.isChecked
        set(value) { secondaryLoginCheckBox.isChecked = value }
}
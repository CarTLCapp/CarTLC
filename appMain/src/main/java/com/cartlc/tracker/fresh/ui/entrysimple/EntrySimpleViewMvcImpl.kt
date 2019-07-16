package com.cartlc.tracker.fresh.ui.entrysimple

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.callassistant.util.viewmvc.ObservableViewMvcImpl
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleViewMvc.YesNo.*

class EntrySimpleViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ObservableViewMvcImpl<EntrySimpleViewMvc.Listener>(), EntrySimpleViewMvc {

    override val rootView: View = inflater.inflate(R.layout.frame_entry_simple, container, false) as ViewGroup

    private val entryTitle = findViewById<TextView>(R.id.entry_title)
    private val entryRadioGroup = findViewById<RadioGroup>(R.id.entry_radio_group)
    private val entryRadioNo = findViewById<RadioButton>(R.id.entry_radio_no)
    private val entryRadioYes = findViewById<RadioButton>(R.id.entry_radio_yes)
    private val entryEditText = findViewById<EditText>(R.id.entry_simple_edit_text)
    private val entryHelpText = findViewById<TextView>(R.id.entry_simple_help_text)

    private val parent = container ?: rootView

    private val autoNext = object : TextView.OnEditorActionListener {
        override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
            for (listener in listeners) {
                listener.onEditTextReturn()
            }
            return false
        }
    }

    init {
        entryEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val value = s.toString()
                for (listener in listeners) {
                    listener.editTextAfterTextChanged(value)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        entryEditText.setOnEditorActionListener(autoNext)
        entryRadioGroup.setOnCheckedChangeListener { _, _ ->
            val value = if (entryRadioYes.isChecked) YES else if (entryRadioNo.isChecked) NO else NONE
            for (listener in listeners) {
                listener.checkButtonChecked(value)
            }
        }
    }

    override var showing: Boolean
        get() = parent.visibility == View.VISIBLE
        set(value) {
            parent.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var titleVisible: Boolean
        get() = entryTitle.visibility == View.VISIBLE
        set(value) {
            entryTitle.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var entryCheckedVisible: Boolean
        get() = entryRadioGroup.visibility == View.VISIBLE
        set(value) {
            entryRadioGroup.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var entryEditTextVisible: Boolean
        get() = entryEditText.visibility == View.VISIBLE
        set(value) {
            entryEditText.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var entryHelpTextVisible: Boolean
        get() = entryHelpText.visibility == View.VISIBLE
        set(value) {
            entryHelpText.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var entryEditTextEms: Int
        get() = TODO("not implemented")
        set(value) {
            entryEditText.setEms(value)
        }

    override var entryEditTextHint: String
        get() = entryEditText.hint.toString()
        set(value) {
            entryEditText.hint = value
        }

    override var entryEditTextValue: String?
        get() = entryEditText.text.toString().trim { it <= ' ' }
        set(value) {
            entryEditText.setText(value)
        }

    override var entryEditTextInputType: Int
        get() = entryEditText.inputType
        set(value) {
            entryEditText.inputType = value
        }

    override var entryHelpTextValue: String?
        get() = entryHelpText.text.toString()
        set(value) {
            entryHelpText.text = value
        }

    override var checkedButton: Int
        get() = entryRadioGroup.checkedRadioButtonId
        set(value) {
            entryRadioGroup.check(value)
        } // -1 clears check

    override var title: String?
        get() = entryTitle.text.toString()
        set(value) {
            entryTitle.text = value
        }

}
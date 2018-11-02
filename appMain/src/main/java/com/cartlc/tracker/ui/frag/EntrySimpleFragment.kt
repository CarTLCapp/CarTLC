package com.cartlc.tracker.ui.frag

import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.cartlc.tracker.databinding.FragEntrySimpleBinding
import com.cartlc.tracker.viewmodel.EntrySimpleViewModel

class EntrySimpleFragment: BaseFragment() {

    lateinit var binding: FragEntrySimpleBinding

    val vm: EntrySimpleViewModel
        get() = baseVM as EntrySimpleViewModel

    val entryTextValue: String
        get() = getEditText(binding.entrySimpleEditText)

    var inputType: Int
        get() = binding.entrySimpleEditText.inputType
        set(value) {
            binding.entrySimpleEditText.inputType = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragEntrySimpleBinding.inflate(layoutInflater, container, false)
        baseVM = EntrySimpleViewModel(activity!!)
        binding.viewModel = vm
        super.onCreateView(inflater, container, savedInstanceState)
        val autoNext = object : TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                vm.dispatchReturnPressedEvent(getEditText(binding.entrySimpleEditText))
                return false
            }
        }
        binding.entrySimpleEditText.setOnEditorActionListener(autoNext)
        return binding.root
    }

    private fun getEditText(text: EditText): String {
        return text.text.toString().trim { it <= ' ' }
    }

    fun reset() {
        vm.showing.set(false)
        vm.helpText.set(null)
        binding.entrySimpleEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS)
    }


}
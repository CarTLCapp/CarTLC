package com.cartlc.tracker.ui.frag

import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import com.cartlc.tracker.databinding.FragEntrySimpleBinding
import com.cartlc.tracker.viewmodel.EntrySimpleViewModel
import com.cartlc.tracker.viewmodel.EntrySimpleViewModel.Checked

class EntrySimpleFragment : BaseFragment() {

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
        binding.entrySimpleEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)

        vm.inputType.observe(this, Observer { type -> binding.entrySimpleEditText.inputType = type })
        vm.checkedButton.observe(this, Observer { value -> binding.entryRadioGroup.check(value) })

        binding.entryRadioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                if (binding.entryRadioYes.isChecked) {
                    vm.onCheckedChanged(Checked.CHECKED_YES)
                } else if (binding.entryRadioNo.isChecked) {
                    vm.onCheckedChanged(Checked.CHECKED_NO)
                } else {
                    vm.onCheckedChanged(Checked.CHECKED_NONE)
                }
            }
        })
        return binding.root
    }

    private fun getEditText(text: EditText): String {
        return text.text.toString().trim { it <= ' ' }
    }

    fun invalidateAll() {
        binding.invalidateAll()
    }

}
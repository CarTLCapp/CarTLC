package com.cartlc.tracker.ui.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cartlc.tracker.databinding.FragButtonsBinding
import com.cartlc.tracker.model.event.Button
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.ui.base.BaseFragment
import com.cartlc.tracker.ui.bits.SoftKeyboardDetect
import com.cartlc.tracker.viewmodel.frag.ButtonsViewModel

class ButtonsFragment : BaseFragment(), SoftKeyboardDetect.Listener {

    lateinit var binding: FragButtonsBinding

    val vm: ButtonsViewModel
        get() = baseVM as ButtonsViewModel

    var softKeyboardDetect: SoftKeyboardDetect? = null
        set(value) {
            field?.unregisterListener(this)
            field = value
            field?.registerListener(this)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragButtonsBinding.inflate(layoutInflater, container, false)
        val componentRoot = app.componentRoot
        baseVM = ButtonsViewModel(repo, componentRoot.messageHandler)
        binding.viewModel = vm
        super.onCreateView(inflater, container, savedInstanceState)
        binding.btnNext.setOnClickListener { btnNext(it) }
        binding.btnPrev.setOnClickListener { btnPrev(it) }
        binding.btnChange.setOnClickListener { btnChangeCompany() }
        binding.btnCenter.setOnClickListener { btnCenter() }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        softKeyboardDetect = null
    }

    private fun clearSoftKeyboard(v: View) {
        vm.showingValue = false
        activity?.let { TBApplication.hideKeyboard(it, v) }
    }

    private fun btnNext(v: View) {
        vm.dispatchButtonEvent(Button.BTN_NEXT)
        clearSoftKeyboard(v)
    }

    private fun btnPrev(v: View) {
        vm.dispatchButtonEvent(Button.BTN_PREV)
        clearSoftKeyboard(v)
    }

    private fun btnCenter() {
        vm.dispatchButtonEvent(Button.BTN_CENTER)
    }

    private fun btnChangeCompany() {
        vm.dispatchButtonEvent(Button.BTN_CHANGE)
    }

    // region SoftKeyboardDetect.Listener
    override fun onSoftKeyboardVisible() {
        if (vm.showingValue) {
            vm.showingValue = false
        }
    }

    override fun onSoftKeyboardHidden() {
        if (!vm.showingValue) {
            vm.showingValue = true
        }
    }

    // endregion SoftKeyboardDetect.Listener

}
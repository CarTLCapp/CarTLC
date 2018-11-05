package com.cartlc.tracker.ui.frag

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.cartlc.tracker.R
import com.cartlc.tracker.databinding.FragButtonsBinding
import com.cartlc.tracker.model.flow.Action
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.viewmodel.ButtonsViewModel

class ButtonsFragment: BaseFragment() {

    private inner class SoftKeyboardDetect : ViewTreeObserver.OnGlobalLayoutListener {

        val ratio = 0.15

        fun clear() {
            vm.showingValue = true
        }

        override fun onGlobalLayout() {
            val rect = Rect()
            root?.let {
                it.rootView?.getWindowVisibleDisplayFrame(rect)
                val screenHeight = it.rootView.height
                val keypadHeight = screenHeight - rect.bottom
                if (keypadHeight > screenHeight * ratio) {
                    hideButtons()
                } else {
                    restoreButtons()
                }
            }
        }

        fun hideButtons() {
            if (vm.showingValue) {
                vm.showingValue = false
            }
        }

        fun restoreButtons() {
            if (!vm.showingValue) {
                vm.showingValue = true
            }
        }
    }

    lateinit var binding: FragButtonsBinding

    var root: ViewGroup? = null
        set(value) {
            field = value
            value?.viewTreeObserver?.addOnGlobalLayoutListener(softKeyboardDetect)
        }

    val vm: ButtonsViewModel
        get() = baseVM as ButtonsViewModel

    private var softKeyboardDetect = SoftKeyboardDetect()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragButtonsBinding.inflate(layoutInflater, container, false)
        baseVM = ButtonsViewModel(activity!!)
        binding.viewModel = vm
        super.onCreateView(inflater, container, savedInstanceState)
        binding.btnNext.setOnClickListener { btnNext(it) }
        binding.btnPrev.setOnClickListener { btnPrev(it) }
        binding.change.setOnClickListener { _ -> btnChangeCompany() }
        binding.btnCenter.setOnClickListener { _ -> btnCenter() }
        return binding.root
    }

    fun reset(flow: Flow) {
        vm.showChangeButtonValue = false
        vm.showCenterButtonValue = false
        vm.centerTextValue = getString(R.string.btn_add)
        vm.showNextButtonValue = flow.next != null
        vm.nextTextValue = getString(R.string.btn_next)
        vm.showPrevButtonValue = flow.prev != null
        vm.prevTextValue = getString(R.string.btn_prev)
    }

    private fun btnNext(v: View) {
        vm.dispatchButtonEvent(Action.BTN_NEXT)
        softKeyboardDetect.clear()
        activity?.let { TBApplication.hideKeyboard(it, v) }
    }

    private fun btnPrev(v: View) {
        vm.dispatchButtonEvent(Action.BTN_PREV)
        softKeyboardDetect.clear()
        activity?.let { TBApplication.hideKeyboard(it, v) }
    }

    private fun btnCenter() {
        vm.dispatchButtonEvent(Action.BTN_CENTER)
    }

    private fun btnChangeCompany() {
        vm.dispatchButtonEvent(Action.BTN_CHANGE)
    }

}
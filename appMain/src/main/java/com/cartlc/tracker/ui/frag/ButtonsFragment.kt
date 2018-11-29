package com.cartlc.tracker.ui.frag

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.cartlc.tracker.databinding.FragButtonsBinding
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.event.Button
import com.cartlc.tracker.ui.act.MainActivity
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.viewmodel.frag.ButtonsViewModel
import com.cartlc.tracker.viewmodel.main.MainButtonsViewModel
import javax.inject.Inject

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

    private val app: TBApplication
        get() = activity!!.applicationContext as TBApplication

    @Inject
    lateinit var repo: CarRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragButtonsBinding.inflate(layoutInflater, container, false)
        app.carRepoComponent.inject(this)
        if (activity is MainActivity) {
            baseVM = MainButtonsViewModel(repo)
        } else {
            baseVM = ButtonsViewModel(repo)
        }
        binding.viewModel = vm
        super.onCreateView(inflater, container, savedInstanceState)
        binding.btnNext.setOnClickListener { btnNext(it) }
        binding.btnPrev.setOnClickListener { btnPrev(it) }
        binding.btnChange.setOnClickListener { btnChangeCompany() }
        binding.btnCenter.setOnClickListener { btnCenter() }
        return binding.root
    }

    private fun clearSoftKeyboard(v: View) {
        softKeyboardDetect.clear()
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

}
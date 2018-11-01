package com.cartlc.tracker.ui.frag

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.cartlc.tracker.R
import com.cartlc.tracker.databinding.FragButtonsBinding
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.viewmodel.ButtonsViewModel
import com.cartlc.tracker.viewmodel.MainViewModel

class ButtonsFragment: BaseFragment() {

    private inner class SoftKeyboardDetect : ViewTreeObserver.OnGlobalLayoutListener {

        val ratio = 0.15

        fun clear() {
            vm.showing = true
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
            if (vm.showing) {
                vm.showing = false
            }
        }

        fun restoreButtons() {
            if (!vm.showing) {
                vm.showing = true
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
    var tmpMainViewModel: MainViewModel? = null

    private var softKeyboardDetect = SoftKeyboardDetect()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragButtonsBinding.inflate(layoutInflater, container, false)
        baseVM = ButtonsViewModel(activity!!, binding)
        binding.viewModel = vm
        super.onCreateView(inflater, container, savedInstanceState)
        binding.btnNext.setOnClickListener({ this.doBtnNextFromUser(it) })
        binding.btnPrev.setOnClickListener({ this.doBtnPrevFromUser(it) })
        binding.change.setOnClickListener({ _ -> doBtnChangeCompany() })
        binding.btnCenter.setOnClickListener({ _ -> doBtnCenter() })
        return binding.root
    }

    fun reset(flow: Flow) {
        vm.showChangeButton = false
        vm.showCenterButton = false
        vm.centerText = getString(R.string.btn_add)
        vm.showNextButton = flow.next != null
        vm.nextText = getString(R.string.btn_next)
        vm.showPrevButton = flow.prev != null
        vm.prevText = getString(R.string.btn_prev)
    }

    private fun doBtnNextFromUser(v: View) {
        tmpMainViewModel?.btnNext()
        softKeyboardDetect.clear()
        activity?.let { TBApplication.hideKeyboard(it, v) }
    }

    private fun doBtnPrevFromUser(v: View) {
        tmpMainViewModel?.btnPrev()
        softKeyboardDetect.clear()
        activity?.let { TBApplication.hideKeyboard(it, v) }
    }

    private fun doBtnCenter() {
        tmpMainViewModel?.btnCenter()
    }

    private fun doBtnChangeCompany() {
        tmpMainViewModel?.autoNarrowOkay = false
        tmpMainViewModel?.btnChangeCompany()
    }

}
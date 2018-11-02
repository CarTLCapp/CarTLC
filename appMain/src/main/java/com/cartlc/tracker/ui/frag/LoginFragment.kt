package com.cartlc.tracker.ui.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cartlc.tracker.databinding.FragLoginBinding
import com.cartlc.tracker.viewmodel.LoginViewModel

class LoginFragment : BaseFragment() {

    lateinit var binding: FragLoginBinding

    val vm: LoginViewModel
        get() = baseVM as LoginViewModel

    var showing: Boolean
        get() = vm.showing.get()
        set(value) {
            vm.showing.set(value)
        }

    fun detectLoginError(): Boolean = vm.detectLoginError()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragLoginBinding.inflate(layoutInflater, container, false)
        baseVM = LoginViewModel(activity!!)
        binding.viewModel = vm
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }
}
package com.cartlc.tracker.ui.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cartlc.tracker.databinding.FragLoginBinding
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.viewmodel.frag.LoginViewModel
import javax.inject.Inject

class LoginFragment : BaseFragment() {

    lateinit var binding: FragLoginBinding

    val vm: LoginViewModel
        get() = baseVM as LoginViewModel

    private val app: TBApplication
        get() = activity!!.applicationContext as TBApplication

    @Inject
    lateinit var repo: CarRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragLoginBinding.inflate(layoutInflater, container, false)
        app.carRepoComponent.inject(this)
        baseVM = LoginViewModel(repo)
        binding.viewModel = vm
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }
}
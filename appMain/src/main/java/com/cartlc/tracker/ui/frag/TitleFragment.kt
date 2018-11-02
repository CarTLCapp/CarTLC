package com.cartlc.tracker.ui.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cartlc.tracker.viewmodel.TitleViewModel
import com.cartlc.tracker.databinding.FragTitleBinding

class TitleFragment: BaseFragment() {

    lateinit var binding: FragTitleBinding

    val vm: TitleViewModel
        get() = baseVM as TitleViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragTitleBinding.inflate(layoutInflater, container, false)
        baseVM = TitleViewModel(activity!!)
        binding.viewModel = vm
        super.onCreateView(inflater, container, savedInstanceState)
        binding.mainTitleSeparator.visibility = View.GONE
        binding.subTitle.visibility = View.GONE
        return binding.root
    }

}
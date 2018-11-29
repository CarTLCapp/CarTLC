package com.cartlc.tracker.ui.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cartlc.tracker.viewmodel.frag.TitleViewModel
import com.cartlc.tracker.databinding.FragTitleBinding
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.ui.app.TBApplication
import javax.inject.Inject

class TitleFragment: BaseFragment() {

    lateinit var binding: FragTitleBinding

    val vm: TitleViewModel
        get() = baseVM as TitleViewModel

    private val app: TBApplication
        get() = activity!!.applicationContext as TBApplication

    @Inject
    lateinit var repo: CarRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragTitleBinding.inflate(layoutInflater, container, false)
        app.carRepoComponent.inject(this)
        baseVM = TitleViewModel(repo)
        binding.viewModel = vm
        super.onCreateView(inflater, container, savedInstanceState)
        binding.mainTitleSeparator.visibility = View.GONE
        binding.subTitle.visibility = View.GONE
        return binding.root
    }

}
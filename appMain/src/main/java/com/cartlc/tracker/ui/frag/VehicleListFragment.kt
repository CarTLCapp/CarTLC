package com.cartlc.tracker.ui.frag

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.databinding.FragVehicleListBinding
import com.cartlc.tracker.model.flow.VehicleStage
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.ui.list.*
import com.cartlc.tracker.viewmodel.VehicleViewModel
import javax.inject.Inject

class VehicleListFragment : BaseFragment() {

    lateinit var binding: FragVehicleListBinding

    @Inject
    lateinit var vm: VehicleViewModel

    val list: RecyclerView
        get() = binding.list
    val title: TextView
        get() = binding.listTitle
    val list2: RecyclerView
        get() = binding.list2
    val title2: TextView
        get() = binding.listTitle2

    private val ctx: Context
        get() = context!!
    private val app: TBApplication
        get() = ctx.applicationContext as TBApplication

    lateinit private var simpleAdapter: SimpleListAdapter
    lateinit private var simpleAdapter2: SimpleListAdapter
    lateinit private var radioAdapter: RadioListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        app.vehicleComponent.inject(this)

        binding = FragVehicleListBinding.inflate(layoutInflater, container, false)
        baseVM = vm
        binding.viewModel = vm

        super.onCreateView(inflater, container, savedInstanceState)

        val linearLayoutManager = LinearLayoutManager(list.context)
        list.layoutManager = linearLayoutManager
        val divider = DividerItemDecoration(list.context, linearLayoutManager.orientation)
        list.addItemDecoration(divider)
        simpleAdapter = SimpleListAdapter(list.context, object : SimpleListAdapter.OnItemSelectedListener {
            override fun onSelectedItem(position: Int, text: String) {
            }
        })

        val linearLayoutManager2 = LinearLayoutManager(list2.context)
        list2.layoutManager = linearLayoutManager2
        val divider2 = DividerItemDecoration(list2.context, linearLayoutManager2.orientation)
        list2.addItemDecoration(divider)
        simpleAdapter2 = SimpleListAdapter(list2.context, object : SimpleListAdapter.OnItemSelectedListener {
            override fun onSelectedItem(position: Int, text: String) {
            }
        })

        radioAdapter = RadioListAdapter(list.context)
        radioAdapter.listener = { _, text -> }

        vm.repo.stage.observe(this, Observer { stage -> onStageChanged(stage) })

        return binding.root
    }

    fun onStageChanged(stage: VehicleStage) {
        when (stage) {
            VehicleStage.STAGE_1 -> {
                list.adapter = radioAdapter
                radioAdapter.list = vm.repo.inspecting
            }
            VehicleStage.STAGE_2 -> {
            }
            VehicleStage.STAGE_3 -> {
            }
            VehicleStage.STAGE_4 -> {
            }
            VehicleStage.STAGE_5 -> {
            }
            VehicleStage.STAGE_6 -> {
            }
        }
    }

}
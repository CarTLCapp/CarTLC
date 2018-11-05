package com.cartlc.tracker.ui.act

import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.R
import com.cartlc.tracker.databinding.ActivityVehicleBinding
import com.cartlc.tracker.model.flow.Action
import com.cartlc.tracker.model.flow.VehicleStage
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.ui.frag.ButtonsFragment
import com.cartlc.tracker.ui.frag.EntrySimpleFragment
import com.cartlc.tracker.ui.frag.TitleFragment
import com.cartlc.tracker.ui.list.CheckBoxListAdapter
import com.cartlc.tracker.ui.list.RadioListAdapter
import com.cartlc.tracker.viewmodel.VehicleViewModel
import javax.inject.Inject

class VehicleActivity : BaseActivity() {

    private lateinit var app: TBApplication

    lateinit var binding: ActivityVehicleBinding

    private lateinit var radioAdapter: RadioListAdapter
    private lateinit var checkboxAdapter: CheckBoxListAdapter
    private lateinit var checkboxAdapter2: CheckBoxListAdapter

    private lateinit var titleFragment: TitleFragment
    private lateinit var buttonsFragment: ButtonsFragment
    private lateinit var stage1EntryEmail: EntrySimpleFragment
    private lateinit var stage2EntryMileage: EntrySimpleFragment
    private lateinit var stage3Entry: EntrySimpleFragment

    @Inject
    lateinit var vm: VehicleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = applicationContext as TBApplication

        app.vehicleComponent.inject(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_vehicle)
        binding.viewModel = vm

        setSupportActionBar(binding.toolbarVehicle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.vehicle_title)

        titleFragment = supportFragmentManager.findFragmentById(R.id.frame_title) as TitleFragment
        buttonsFragment = supportFragmentManager.findFragmentById(R.id.frame_buttons) as ButtonsFragment
        stage1EntryEmail = supportFragmentManager.findFragmentById(R.id.stage1_entry_email) as EntrySimpleFragment
        stage2EntryMileage = supportFragmentManager.findFragmentById(R.id.stage2_entry_mileage) as EntrySimpleFragment
        stage3Entry = supportFragmentManager.findFragmentById(R.id.stage3_entry_simple) as EntrySimpleFragment

        setup(binding.stage1List)
        setup(binding.stage2List)
        setup(binding.stage3List)
        setup(binding.stage3List2)

        radioAdapter = RadioListAdapter(this)
        radioAdapter.listener = { _, text -> }

        checkboxAdapter = CheckBoxListAdapter(this) { pos, text, selected ->
        }
        checkboxAdapter2 = CheckBoxListAdapter(this) { pos, text, selected ->
        }

        binding.stage1List.adapter = radioAdapter
        binding.stage2List.adapter = radioAdapter
        binding.stage3List.adapter = checkboxAdapter
        binding.stage3List2.adapter = checkboxAdapter2

        buttonsFragment.root = binding.root
        buttonsFragment.vm.handleActionEvent().observe(this, Observer { event ->
            event.executeIfNotHandled { onActionDispatch(event.peekContent()) }
        })
        buttonsFragment.vm.showCenterButtonValue = false
        buttonsFragment.vm.showNextButtonValue = true

        stage1EntryEmail.vm.handleGenericEvent().observe(this, Observer { event ->
            vm.doSimpleEntryEmailReturn(event.peekContent())
        })
        stage1EntryEmail.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        stage1EntryEmail.vm.titleValue = getString(R.string.vehicle_email_address)
        stage1EntryEmail.vm.simpleEmsValue = resources.getInteger(R.integer.entry_simple_ems_email)
        stage1EntryEmail.vm.simpleHintValue = getString(R.string.vehicle_email_hint)

        stage2EntryMileage.vm.handleGenericEvent().observe(this, Observer { event ->
            vm.doSimpleEntryMileageReturn(event.peekContent())
        })
        stage2EntryMileage.inputType = InputType.TYPE_CLASS_NUMBER
        stage2EntryMileage.vm.titleValue = getString(R.string.vehicle_mileage)
        stage2EntryMileage.vm.simpleHintValue = getString(R.string.vehicle_mileage_hint)

        stage3Entry.vm.handleGenericEvent().observe(this, Observer { event ->
            vm.doSimpleEntryReturn(event.peekContent())
        })
        stage3Entry.vm.simpleEmsValue = resources.getInteger(R.integer.entry_simple_ems_lights)
        stage3Entry.vm.simpleHintValue = getString(R.string.vehicle_entry_general_hint)
        titleFragment.vm.titleValue = null

        vm.repo.stage.observe(this, Observer { stage -> onStageChanged(stage) })
        vm.handleActionEvent().observe(this, Observer { event ->
            event.executeIfNotHandled { onActionDispatch(event.peekContent()) }
        })
    }

    private fun setup(list: RecyclerView) {
        val linearLayoutManager = LinearLayoutManager(list.context)
        list.layoutManager = linearLayoutManager
        val divider = DividerItemDecoration(list.context, linearLayoutManager.orientation)
        list.addItemDecoration(divider)
    }

    private fun onActionDispatch(action: Action) {
        when (action) {
            Action.BTN_NEXT -> vm.onBtnNext()
            Action.BTN_PREV -> vm.onBtnPrev()
            Action.SUBMIT -> onSubmit()
        }
    }

    private fun onSubmit() {
        finish()
    }

    private fun onStageChanged(stage: VehicleStage) {
        titleFragment.vm.titleValue = null
        titleFragment.vm.subTitleValue = null
        vm.showFrame1Value = false
        vm.showFrame2Value = false
        vm.showFrame3Value = false
        vm.stage3ListTitleValue = null
        vm.stage3List2TitleValue = null
        buttonsFragment.vm.showPrevButtonValue = stage != VehicleStage.STAGE_1
        buttonsFragment.vm.nextTextValue = getString(R.string.btn_next)
        stage3Entry.vm.titleValue = null
        checkboxAdapter.items = emptyList()

        when (stage) {
            VehicleStage.STAGE_1 -> {
                vm.showFrame1Value = true
                radioAdapter.list = vm.repo.inspecting
            }
            VehicleStage.STAGE_2 -> {
                vm.showFrame2Value = true
                radioAdapter.list = vm.repo.typeOfInspection.toList()
            }
            VehicleStage.STAGE_3 -> {
                vm.showFrame3Value = true
                titleFragment.vm.titleValue = getString(R.string.vehicle_lights)
                titleFragment.vm.subTitleValue = getString(R.string.vehicle_lights_description)
                stage3Entry.vm.titleValue = getString(R.string.vehicle_lights_exterior)
                vm.stage3ListTitleValue = getString(R.string.vehicle_lights_head)
                vm.stage3List2TitleValue = getString(R.string.vehicle_lights_tail)
                checkboxAdapter.items = vm.repo.headLights.toList()
                checkboxAdapter2.items = vm.repo.tailLights.toList()
            }
            VehicleStage.STAGE_4 -> {
                vm.showFrame3Value = true
                titleFragment.vm.titleValue = getString(R.string.vehicle_fluids)
                titleFragment.vm.subTitleValue = getString(R.string.vehicle_fluids_description)
                vm.stage3ListTitleValue = getString(R.string.vehicle_fluids_checks)
                checkboxAdapter.items = vm.repo.fluidChecks.toList()
                stage3Entry.vm.titleValue = getString(R.string.vehicle_fluids_detected)
            }
            VehicleStage.STAGE_5 -> {
                vm.showFrame3Value = true
                titleFragment.vm.titleValue = getString(R.string.vehicle_exterior)
                titleFragment.vm.subTitleValue = getString(R.string.vehicle_exterior_description)
                vm.stage3ListTitleValue = getString(R.string.vehicle_tire_inspection)
                checkboxAdapter.items = vm.repo.tireInspection.toList()
                stage3Entry.vm.titleValue = getString(R.string.vehicle_tire_damage)
            }
            VehicleStage.STAGE_6 -> {
                vm.showFrame3Value = true
                titleFragment.vm.titleValue = getString(R.string.vehicle_other_title)
                titleFragment.vm.subTitleValue = getString(R.string.vehicle_other_description)
                buttonsFragment.vm.nextTextValue = getString(R.string.vehicle_submit)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
package com.cartlc.tracker.ui.act

import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.R
import com.cartlc.tracker.databinding.ActivityVehicleBinding
import com.cartlc.tracker.fresh.ui.buttons.ButtonsUseCase
import com.cartlc.tracker.fresh.ui.buttons.ButtonsView
import com.cartlc.tracker.fresh.model.event.Action
import com.cartlc.tracker.fresh.model.flow.ActionUseCase
import com.cartlc.tracker.fresh.model.flow.VehicleStage
import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.fresh.ui.base.BaseActivity
import com.cartlc.tracker.fresh.ui.bits.SoftKeyboardDetect
import com.cartlc.tracker.ui.list.CheckBoxListAdapter
import com.cartlc.tracker.ui.list.RadioListAdapter
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleView
import com.cartlc.tracker.fresh.ui.title.TitleUseCase
import com.cartlc.tracker.fresh.ui.title.TitleView
import com.cartlc.tracker.fresh.model.event.Button
import com.cartlc.tracker.viewmodel.vehicle.VehicleViewModel

class VehicleActivity : BaseActivity(), ActionUseCase.Listener, ButtonsUseCase.Listener {

    private lateinit var app: TBApplication

    lateinit var binding: ActivityVehicleBinding

    private lateinit var radioAdapter: RadioListAdapter
    private lateinit var checkboxAdapter: CheckBoxListAdapter
    private lateinit var checkboxAdapter2: CheckBoxListAdapter

    private lateinit var titleView: TitleView
    private lateinit var buttonsView: ButtonsView
    private lateinit var stage2Entry: EntrySimpleView
    private lateinit var stage345Entry: EntrySimpleView

    private lateinit var buttonsUseCase: ButtonsUseCase
    private lateinit var titleUseCase: TitleUseCase

    lateinit var vm: VehicleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = applicationContext as TBApplication

        vm = app.vehicleViewModel

        binding = DataBindingUtil.setContentView(this, R.layout.activity_vehicle)
        binding.viewModel = vm

        setSupportActionBar(binding.toolbarVehicle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.vehicle_title)

        titleView = findViewById(R.id.frame_title)
        buttonsView = findViewById(R.id.frame_buttons)
        stage2Entry = findViewById(R.id.stage2_entry)
        stage345Entry = findViewById(R.id.stage345_entry_simple)

        buttonsUseCase = buttonsView.useCase
        buttonsUseCase.registerListener(this)

        titleUseCase = titleView.useCase

        setup(binding.stage12List)
        setup(binding.stage345List)
        setup(binding.stage345List2)

        radioAdapter = RadioListAdapter(this)
        radioAdapter.listener = { _, text -> vm.onRadioSelect(text) }

        checkboxAdapter = CheckBoxListAdapter(this) { _, text, selected -> vm.onListSelect(text, selected) }
        checkboxAdapter2 = CheckBoxListAdapter(this) { _, text, selected -> vm.onList2Select(text, selected) }

        binding.stage12List.adapter = radioAdapter
        binding.stage345List.adapter = checkboxAdapter
        binding.stage345List2.adapter = checkboxAdapter2

        buttonsUseCase.softKeyboardDetect = SoftKeyboardDetect(binding.root)
        buttonsUseCase.centerVisible = false
        buttonsUseCase.reset()

        stage2Entry.control.afterTextChangedListener = { value -> vm.onEntryChanged(value) }
        stage2Entry.control.emsValue = resources.getInteger(R.integer.entry_simple_ems)
        stage2Entry.control.inputType = InputType.TYPE_CLASS_NUMBER
        stage2Entry.control.titleValue = getString(R.string.vehicle_mileage)
        stage2Entry.control.hintValue = getString(R.string.vehicle_required)
        stage2Entry.control.showCheckedValue = false

        stage345Entry.control.inputType = InputType.TYPE_CLASS_TEXT
        stage345Entry.control.emsValue = resources.getInteger(R.integer.entry_simple_ems_lights)
        stage345Entry.control.hintValue = getString(R.string.vehicle_comments)
        stage345Entry.control.afterTextChangedListener = { value -> vm.onEntryChanged(value) }
        stage345Entry.control.showCheckedValue = true
        stage345Entry.control.dispatchActionEvent = { action -> vm.dispatchActionEvent(action) }
        titleUseCase.mainTitleText = null

        vm.repo.entered.clear()
        vm.repo.stage.observe(this, Observer { stage -> onStageChanged(stage) })
        vm.actionUseCase.registerListener(this)
        vm.mileageTextValue = { stage2Entry.control.entryTextValue ?: "" }
        vm.entryTextValue = { stage345Entry.control.entryTextValue ?: "" }
        vm.entryHasCheckedValue = { stage345Entry.control.hasCheckedValue }
        vm.btnNextVisible = { flag -> buttonsUseCase.nextVisible = flag }
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.actionUseCase.unregisterListener(this)
        buttonsUseCase.unregisterListener(this)
    }

    private fun setup(list: RecyclerView) {
        val linearLayoutManager = LinearLayoutManager(list.context)
        list.layoutManager = linearLayoutManager
        val divider = DividerItemDecoration(list.context, linearLayoutManager.orientation)
        list.addItemDecoration(divider)
    }

    override fun onActionChanged(action: Action) {
        when (action) {
            Action.SUBMIT -> onSubmit()
            is Action.RETURN_PRESSED -> vm.doSimpleEntryReturn(action.text)
            is Action.BUTTON_DIALOG -> {
                vm.onEntryPressAction(action.button)
            }
        }
    }

    private fun onSubmit() {
        finish()
    }

    private fun onStageChanged(stage: VehicleStage) {

        titleUseCase.mainTitleText = null
        titleUseCase.subTitleText = null
        vm.showFrame12Value = false
        vm.showFrame345Value = false
        vm.stage3ListTitleValue = null
        vm.stage3List2TitleValue = null
        buttonsUseCase.prevVisible = stage != VehicleStage.STAGE_1
        buttonsUseCase.nextVisible = false
        buttonsUseCase.nextText = getString(R.string.btn_next)
        stage2Entry.control.showing = false
        stage345Entry.control.titleValue = null
        stage345Entry.control.showEditTextValue = vm.show345EditText
        checkboxAdapter.items = emptyList()

        vm.onStageChanged(stage)

        when (stage) {
            VehicleStage.STAGE_1 -> {
                vm.showFrame12Value = true
                radioAdapter.list = vm.repo.inspectingList
                radioAdapter.selectedText = vm.repo.entered.vehicle.inspectingValue
            }
            VehicleStage.STAGE_2 -> {
                vm.showFrame12Value = true
                stage2Entry.control.showing = true
                stage2Entry.control.entryTextValue = vm.repo.entered.mileageValue
                radioAdapter.list = vm.repo.typeOfInspection.toList()
                radioAdapter.selectedText = vm.repo.entered.vehicle.typeOfInspectionValue
            }
            VehicleStage.STAGE_3 -> {
                vm.showFrame345Value = true
                titleUseCase.mainTitleText = getString(R.string.vehicle_lights)
                titleUseCase.subTitleText = getString(R.string.vehicle_lights_description)
                vm.stage3ListTitleValue = getString(R.string.vehicle_lights_head)
                vm.stage3List2TitleValue = getString(R.string.vehicle_lights_tail)
                checkboxAdapter.items = vm.repo.headLights.toList()
                checkboxAdapter.selectedItems = vm.repo.entered.vehicle.headLightsValue
                checkboxAdapter2.items = vm.repo.tailLights.toList()
                checkboxAdapter2.selectedItems = vm.repo.entered.vehicle.tailLightsValue
                stage345Entry.control.titleValue = getString(R.string.vehicle_lights_exterior)
                stage345Entry.control.entryTextValue = vm.repo.entered.vehicle.exteriorLightIssues
                stage345Entry.control.checkedButtonBooleanValue = vm.repo.entered.hasIssuesExteriorLights
//                stage345Entry.invalidateAll()
            }
            VehicleStage.STAGE_4 -> {
                vm.showFrame345Value = true
                titleUseCase.mainTitleText = getString(R.string.vehicle_fluids)
                titleUseCase.subTitleText = getString(R.string.vehicle_fluids_description)
                vm.stage3ListTitleValue = getString(R.string.vehicle_fluids_checks)
                checkboxAdapter.items = vm.repo.fluidChecks.toList()
                checkboxAdapter.selectedItems = vm.repo.entered.vehicle.fluidChecksValue
                stage345Entry.control.titleValue = getString(R.string.vehicle_fluids_detected)
                stage345Entry.control.entryTextValue = vm.repo.entered.vehicle.fluidProblemsDetected
                stage345Entry.control.checkedButtonBooleanValue = vm.repo.entered.hasIssuesFluids
//                stage345Entry.invalidateAll()
            }
            VehicleStage.STAGE_5 -> {
                vm.showFrame345Value = true
                titleUseCase.mainTitleText = getString(R.string.vehicle_exterior)
                titleUseCase.subTitleText = getString(R.string.vehicle_exterior_description)
                vm.stage3ListTitleValue = getString(R.string.vehicle_tire_inspection)
                checkboxAdapter.items = vm.repo.tireInspection.toList()
                checkboxAdapter.selectedItems = vm.repo.entered.vehicle.tireInspectionValue
                stage345Entry.control.titleValue = getString(R.string.vehicle_tire_damage)
                stage345Entry.control.entryTextValue = vm.repo.entered.vehicle.exteriorDamage
                stage345Entry.control.checkedButtonBooleanValue = vm.repo.entered.hasIssuesDamage
//                stage345Entry.invalidateAll()
            }
            VehicleStage.STAGE_6 -> {
                vm.showFrame345Value = true
                titleUseCase.mainTitleText = getString(R.string.vehicle_other_title)
                titleUseCase.subTitleText = getString(R.string.vehicle_other_description)
                buttonsUseCase.nextText = getString(R.string.vehicle_submit)
                stage345Entry.control.entryTextValue = vm.repo.entered.vehicle.other
                stage345Entry.control.checkedButtonBooleanValue = vm.repo.entered.hasIssuesOther
//                stage345Entry.invalidateAll()
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

    override fun onButtonConfirm(action: Button): Boolean {
        return true
    }

    override fun onButtonEvent(action: Button) {
        vm.onButtonDispatch(action)
    }
}
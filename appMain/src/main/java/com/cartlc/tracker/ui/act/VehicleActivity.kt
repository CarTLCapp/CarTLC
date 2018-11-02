package com.cartlc.tracker.ui.act

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.cartlc.tracker.R
import com.cartlc.tracker.model.flow.Action
import com.cartlc.tracker.model.flow.VehicleStage
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.ui.frag.ButtonsFragment
import com.cartlc.tracker.ui.frag.EntrySimpleFragment
import com.cartlc.tracker.ui.frag.TitleFragment
import com.cartlc.tracker.ui.frag.VehicleListFragment
import com.cartlc.tracker.viewmodel.VehicleViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_vehicles.*
import javax.inject.Inject

class VehicleActivity : BaseActivity() {

    private lateinit var app: TBApplication

    val titleFragment: TitleFragment
        get() = frame_title as TitleFragment
    val buttonsFragment: ButtonsFragment
        get() = frame_buttons as ButtonsFragment
    val entrySimpleAboveFragment: EntrySimpleFragment
        get() = frame_entry_simple_above as EntrySimpleFragment
    val entrySimpleBelowFragment: EntrySimpleFragment
        get() = frame_entry_simple_below as EntrySimpleFragment
    val vehicleListFragment: VehicleListFragment
        get() = frame_vehicle_list as VehicleListFragment

    @Inject
    lateinit var vm: VehicleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = applicationContext as TBApplication

        setContentView(R.layout.activity_vehicle)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_vehicle)
        setSupportActionBar(toolbar)

        app.vehicleComponent.inject(this)

        title = getString(R.string.vehicle_title)

        buttonsFragment.root = root
        buttonsFragment.vm.handleActionEvent().observe(this, Observer { event ->
            event.executeIfNotHandled { onActionDispatch(event.peekContent()) }
        })
        buttonsFragment.vm.showCenterButtonValue = false

        entrySimpleAboveFragment.vm.handleGenericEvent().observe(this, Observer { event ->
            vm.doSimpleEntryAboveReturn(event.peekContent())
        })
        entrySimpleBelowFragment.vm.handleGenericEvent().observe(this, Observer { event ->
            vm.doSimpleEntryBelowReturn(event.peekContent())
        })
        vm.repo.stage.observe(this, Observer { stage -> onStageChanged(stage) })

    }

    private fun onActionDispatch(action: Action) {
        when (action) {
            Action.BTN_NEXT -> {
            }
            Action.BTN_PREV -> {
            }
        }
    }

    private fun onStageChanged(stage: VehicleStage) {
        entrySimpleAboveFragment.vm.showingValue = false
        entrySimpleBelowFragment.vm.showingValue = false
        vehicleListFragment.vm.showListValue = false
        vehicleListFragment.vm.showList2Value = false
        buttonsFragment.vm.showPrevButtonValue = false
        buttonsFragment.vm.showNextButtonValue = false

        when (stage) {
            VehicleStage.STAGE_1 -> {
                titleFragment.vm.titleValue = getString(R.string.vehicle_email_address)
                entrySimpleAboveFragment.vm.showingValue = true
                vehicleListFragment.vm.titleValue = getString(R.string.vehicle_inspecting)
                vehicleListFragment.vm.showListValue = true
                buttonsFragment.vm.showNextButtonValue = true
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
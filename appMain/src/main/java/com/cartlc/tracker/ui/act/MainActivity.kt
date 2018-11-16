/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.act

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.os.*
import android.provider.MediaStore
import android.text.InputType
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.cartlc.tracker.BuildConfig

import com.cartlc.tracker.R
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.model.data.DataEntry
import com.cartlc.tracker.model.data.DataNote
import com.cartlc.tracker.ui.util.CheckError
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.event.EventError
import com.cartlc.tracker.model.event.EventRefreshProjects
import com.cartlc.tracker.model.flow.Action
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.PictureFlow
import com.cartlc.tracker.model.misc.StringMessage
import com.cartlc.tracker.ui.bits.AutoLinearLayoutManager
import com.cartlc.tracker.ui.list.*
import com.cartlc.tracker.ui.util.DialogHelper
import com.cartlc.tracker.ui.util.LocationHelper
import com.cartlc.tracker.ui.util.PermissionHelper
import com.cartlc.tracker.ui.frag.*
import com.cartlc.tracker.viewmodel.MainViewModel

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class MainActivity : BaseActivity() {

    companion object {

        private val REQUEST_IMAGE_CAPTURE = 1
        private val REQUEST_EDIT_ENTRY = 2

        val RESULT_EDIT_ENTRY = 2
        val RESULT_EDIT_PROJECT = 3
        val RESULT_DELETE_PROJECT = 4

        private val KEY_TAKING_PICTURE = "picture"
    }

    private lateinit var mApp: TBApplication
    private lateinit var mPictureAdapter: PictureListAdapter
    private lateinit var mInputMM: InputMethodManager
    private var showServerError = true

    @Inject
    lateinit var vm: MainViewModel

    val loginFragment: LoginFragment
        get() = frame_login as LoginFragment
    val mainListFragment: MainListFragment
        get() = frame_main_list as MainListFragment
    val confirmationFragment: ConfirmationFragment
        get() = frame_confirmation_fragment as ConfirmationFragment
    val titleFragment: TitleFragment
        get() = frame_title as TitleFragment
    val buttonsFragment: ButtonsFragment
        get() = frame_buttons as ButtonsFragment
    val entrySimpleFragment: EntrySimpleFragment
        get() = frame_entry_simple as EntrySimpleFragment

    private inner class RotatePictureTask : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg voids: Void): Boolean? {
            vm.autoRotatePictureResult()
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            if (result!! && !isDestroyed && !isFinishing && vm.isPictureStage) {
                mPictureAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mApp = applicationContext as TBApplication
        mApp.mainViewModelComponent.inject(this)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar_main))

        buttonsFragment.root = root
        buttonsFragment.vm.handleActionEvent().observe(this, Observer { event ->
            event.executeIfNotHandled { onActionDispatch(event.peekContent()) }
        })
        mInputMM = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        fab_add.setOnClickListener { _: View -> vm.btnPlus() }

        mPictureAdapter = PictureListAdapter(this) { newCount: Int -> setPhotoTitleCount(newCount) }
        val linearLayoutManager = AutoLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        list_pictures.layoutManager = linearLayoutManager
        list_pictures.adapter = mPictureAdapter

        vm.onCreate()
        vm.curFlow.observe(this, Observer { stage -> onStageChanged(stage) })
        vm.error.observe(this, Observer { message -> showError(message) })
        vm.handleActionEvent().observe(this, Observer { event ->
            event.executeIfNotHandled { onActionDispatch(event.peekContent()) }
        })
        vm.detectNoteError = this::detectNoteError
        vm.entryTextValue = { entrySimpleFragment.entryTextValue }
        vm.detectLoginError = this::detectLoginError
        vm.getString = { msg -> getStringMessage(msg) }

        entrySimpleFragment.vm.handleGenericEvent().observe(this, Observer { event ->
            vm.doSimpleEntryReturn(event.peekContent())
        })

        EventBus.getDefault().register(this)
        title = mApp.versionedTitle
        getLocation()
    }

    private fun getStringMessage(msg: StringMessage): String =
            when (msg) {
                StringMessage.entry_hint_edit_project -> getString(R.string.entry_hint_edit_project)
                is StringMessage.status_installed_equipments -> getString(R.string.status_installed_equipments, msg.checkedEquipment, msg.maxEquip)
                is StringMessage.status_installed_pictures -> getString(R.string.status_installed_pictures, msg.countPictures)
            }

    private fun getLocation() {
        LocationHelper.instance.requestLocation(this, object : LocationHelper.OnLocationCallback {
            override fun onLocationUpdate(address: Address) {
                vm.fab_address = address
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile -> onBtnProfile()
            R.id.upload -> {
                mApp.reloadFromServer()
            }
            R.id.fleet_vehicles -> {
                vm.onVehiclesPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onResume() {
        super.onResume()
        mApp.ping()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        CheckError.instance.cleanup()
        LocationHelper.instance.onDestroy()
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EventError) {
        if (showServerError) {
            showServerError(event.toString())
        }
    }

    private fun doViewProject() {
        val intent = Intent(this, ListEntryActivity::class.java)
        startActivityForResult(intent, REQUEST_EDIT_ENTRY)
    }

    private fun doVehicles() {
        val intent = Intent(this, VehicleActivity::class.java)
        startActivity(intent)
    }

    private fun doVehiclesPendingDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.vehicle_pending_dialog_title)
        builder.setMessage(R.string.vehicle_pending_dialog_message)
        builder.create().show()
    }

    private fun onBtnProfile() {
        vm.btnProfile()
    }

    private fun onStageChanged(flow: Flow) {
        loginFragment.showing = false
        mainListFragment.showing = false
        mainListFragment.showEmpty = false
        buttonsFragment.reset(flow)
        confirmationFragment.showing = false
        titleFragment.vm.showSeparatorValue = false
        titleFragment.vm.subTitleValue = null
        entrySimpleFragment.reset()

        fab_add.hide()
        frame_pictures.visibility = View.GONE
        list_pictures.visibility = View.GONE
        list_entry_hint.visibility = View.GONE

        when (flow.stage) {
            Stage.LOGIN -> {
                loginFragment.showing = true
                buttonsFragment.vm.showCenterButtonValue = true
                buttonsFragment.vm.centerTextValue = getString(R.string.title_login)
            }
            Stage.PROJECT -> {
                mainListFragment.showing = true
                titleFragment.vm.subTitleValue = vm.curProjectHint
                buttonsFragment.vm.showNextButtonValue = vm.hasProjectName
                vm.processProject { projects -> setList(R.string.title_project, PrefHelper.KEY_PROJECT, projects) }
                getLocation()
            }
            Stage.COMPANY -> {
                titleFragment.vm.subTitleValue = vm.editProjectHint
                mainListFragment.showing = true
                buttonsFragment.vm.showCenterButtonValue = true
                vm.processCompanies { companyNames ->
                    setList(R.string.title_company, PrefHelper.KEY_COMPANY, companyNames)
                    checkCenterButtonIsEdit()
                }
            }
            Stage.ADD_COMPANY -> {
                titleFragment.vm.titleValue = getString(R.string.title_company)
                entrySimpleFragment.vm.showingValue = true
                entrySimpleFragment.vm.simpleHintValue = getString(R.string.title_company)
                vm.processEditCompany { companyName ->
                    entrySimpleFragment.vm.simpleTextValue = companyName
                }
            }
            Stage.STATE, Stage.ADD_STATE -> {
                val editing = flow.stage == Stage.ADD_STATE
                titleFragment.vm.subTitleValue = if (editing) vm.editProjectHint else vm.curProjectHint
                mainListFragment.showing = true
                buttonsFragment.vm.showNextButtonValue = false
                vm.processStates(editing) { states, hint, edit ->
                    if (edit) {
                        setList(R.string.title_state, PrefHelper.KEY_STATE, states)
                    } else {
                        entrySimpleFragment.vm.helpTextValue = hint
                        entrySimpleFragment.vm.showingValue = true
                        if (setList(R.string.title_state, PrefHelper.KEY_STATE, states)) {
                            buttonsFragment.vm.showNextButtonValue = true
                        }
                        buttonsFragment.vm.showCenterButtonValue = true
                        checkChangeCompanyButtonVisible()
                    }
                }
            }
            Stage.CITY, Stage.ADD_CITY -> {
                val editing = flow.stage == Stage.ADD_CITY
                titleFragment.vm.subTitleValue = if (editing) vm.editProjectHint else vm.curProjectHint
                buttonsFragment.vm.showNextButtonValue = false
                vm.processCities(editing) { cities, hint, edit ->
                    if (edit) {
                        entrySimpleFragment.vm.showingValue = true
                        titleFragment.vm.titleValue = getString(R.string.title_city)
                        entrySimpleFragment.vm.simpleTextValue = ""
                        entrySimpleFragment.vm.simpleHintValue = getString(R.string.title_city)
                    } else {
                        entrySimpleFragment.vm.helpTextValue = hint
                        mainListFragment.showing = true
                        if (setList(R.string.title_city, PrefHelper.KEY_CITY, cities)) {
                            buttonsFragment.vm.showNextButtonValue = true
                        }
                        buttonsFragment.vm.showCenterButtonValue = true
                        checkChangeCompanyButtonVisible()
                    }
                }
            }
            Stage.STREET, Stage.ADD_STREET -> {
                var editing = flow.stage == Stage.ADD_STREET
                buttonsFragment.vm.showNextButtonValue = false
                titleFragment.vm.subTitleValue = if (editing) vm.editProjectHint else vm.curProjectHint

                vm.processStreets(editing) { streets, hint, edit ->
                    if (edit) {
                        titleFragment.vm.titleValue = getString(R.string.title_street)
                        entrySimpleFragment.vm.showingValue = true
                        entrySimpleFragment.vm.simpleTextValue = ""
                        entrySimpleFragment.vm.simpleHintValue = getString(R.string.title_street)
                        entrySimpleFragment.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                    } else {
                        entrySimpleFragment.vm.helpTextValue = hint
                        buttonsFragment.vm.showCenterButtonValue = true
                        mainListFragment.showing = true
                        if (setList(R.string.title_street, PrefHelper.KEY_STREET, streets)) {
                            buttonsFragment.vm.showNextButtonValue = true
                        }
                        checkChangeCompanyButtonVisible()
                    }
                }
            }
            Stage.CONFIRM_ADDRESS -> {
                if (vm.fab_addressConfirmOkay) {
                    vm.fab_addressConfirmOkay = false
                    titleFragment.vm.subTitleValue = vm.curProjectHint
                    checkChangeCompanyButtonVisible()
                } else {
                    vm.skip()
                }
            }
            Stage.CURRENT_PROJECT -> {
                mApp.ping()
                vm.processCurrentProject { fab_add.show() }
                mainListFragment.showing = true
                titleFragment.vm.showSeparatorValue = true
                buttonsFragment.vm.showCenterButtonValue = true
                buttonsFragment.vm.prevTextValue = getString(R.string.btn_edit)
                buttonsFragment.vm.centerTextValue = getString(R.string.btn_new_project)
                titleFragment.vm.titleValue = getString(R.string.title_current_project)
                mainListFragment.setAdapter(flow.stage)
            }
            Stage.TRUCK -> {
                entrySimpleFragment.vm.showingValue = true
                entrySimpleFragment.vm.simpleHintValue = getString(R.string.title_truck)
                entrySimpleFragment.vm.helpTextValue = getString(R.string.entry_hint_truck)
                entrySimpleFragment.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                mainListFragment.showing = true

                vm.processTrucks { trucks, truckValue, hint ->
                    entrySimpleFragment.vm.simpleTextValue = truckValue
                    setList(R.string.title_truck, PrefHelper.KEY_TRUCK, trucks)
                    titleFragment.vm.subTitleValue = hint
                }
            }
            Stage.EQUIPMENT -> {
                titleFragment.vm.titleValue = getString(R.string.title_equipment_installed)
                mainListFragment.showing = true
                mainListFragment.setAdapter(flow.stage)
                buttonsFragment.vm.showCenterButtonValue = true
            }
            Stage.ADD_EQUIPMENT -> {
                titleFragment.vm.titleValue = getString(R.string.title_equipment)
                entrySimpleFragment.vm.showingValue = true
                entrySimpleFragment.vm.simpleHintValue = getString(R.string.title_equipment)
                entrySimpleFragment.vm.simpleTextValue = ""
            }
            Stage.NOTES -> {
                titleFragment.vm.titleValue = getString(R.string.title_notes)
                mainListFragment.showing = true
                mainListFragment.setAdapter(flow.stage)
            }
            Stage.PICTURE_1,
            Stage.PICTURE_2,
            Stage.PICTURE_3 -> {
                vm.processPictures { pictures, pictureCount, showToast ->
                    if (showToast) {
                        showPictureToast(pictureCount)
                    }
                    setPhotoTitleCount(pictureCount)
                    buttonsFragment.vm.showNextButtonValue = false
                    buttonsFragment.vm.showCenterButtonValue = true
                    buttonsFragment.vm.centerTextValue = getString(R.string.btn_another)
                    frame_pictures.visibility = View.VISIBLE
                    list_pictures.visibility = View.VISIBLE
                    val pictureFlow = flow as PictureFlow
                    if (pictureCount < pictureFlow.expected) {
                        vm.dispatchPictureRequest()
                    } else {
                        buttonsFragment.vm.showNextButtonValue = true
                        mPictureAdapter.setList(pictures)
                    }
                }
            }
            Stage.STATUS -> {
                buttonsFragment.vm.nextTextValue = getString(R.string.btn_done)
                mainListFragment.showing = true
                mainListFragment.setAdapter(flow.stage)
                titleFragment.vm.titleValue = getString(R.string.title_status)
                titleFragment.vm.subTitleValue = vm.statusHint
                vm.processStatus()
            }
            Stage.CONFIRM -> {
                buttonsFragment.vm.nextTextValue = getString(R.string.btn_confirm)
                confirmationFragment.showing = true
                titleFragment.vm.titleValue = getString(R.string.title_confirmation)
                vm.processConfirm { entry -> confirmationFragment.fill(entry) }
                storeCommonRotation()
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EventRefreshProjects) {
        Timber.d("onEvent(EventRefreshProjects)")
        vm.curFlow.value?.let { onStageChanged(it) }
    }

    private fun onActionDispatch(action: Action) {
        when (action) {
            Action.NEW_PROJECT -> {
                vm.onNewProject()
            }
            Action.VIEW_PROJECT -> {
                doViewProject()
            }
            is Action.PICTURE_REQUEST -> dispatchPictureRequest(action.file)
            Action.CONFIRM_DIALOG -> showConfirmDialog()
            Action.VEHICLES -> doVehicles()
            Action.VEHICLES_PENDING -> doVehiclesPendingDialog()
            Action.BTN_PREV -> vm.btnPrev()
            Action.BTN_CENTER -> vm.btnCenter()
            Action.BTN_NEXT -> vm.btnNext()
            Action.BTN_CHANGE -> vm.btnChangeCompany()
            Action.EDIT_ENTRY -> doEditEntry()
            is Action.SHOW_TRUCK_ERROR -> showTruckError(action.entry, action.callback)
        }
    }

    private fun showTruckError(entry: DataEntry, callback: CheckError.CheckErrorResult) {
        CheckError.instance.showTruckError(this, entry, callback)
    }

    // Return false if NoneSelected situation has occurred.
    private fun setList(titleId: Int, key: String, list: List<String>): Boolean {
        val title = getString(titleId)
        titleFragment.vm.titleValue = title
        if (list.isEmpty()) {
            mainListFragment.vm.curKey = key
            mainListFragment.showing = false
            vm.onEmptyList()
        } else {
            return mainListFragment.setList(key, list)
        }
        return true
    }

    private fun dispatchPictureRequest(pictureFile: File) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) == null) {
            vm.dispatchPictureRequestFailure()
            return
        }
        val pictureUri = TBApplication.getUri(this, pictureFile)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)
        // Grant permissions
        val resInfoList = packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            grantUriPermission(packageName, pictureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        // Start Camera activity
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_EDIT_ENTRY ->
                when (resultCode) {
                    RESULT_EDIT_ENTRY -> doEditEntry()
                    RESULT_EDIT_PROJECT -> doEditProject()
                    RESULT_DELETE_PROJECT -> vm.onDeletedProject()
                    else -> vm.onAbort()
                }
            REQUEST_IMAGE_CAPTURE ->
                if (resultCode == Activity.RESULT_OK) {
                    RotatePictureTask().execute()
                    vm.onPictureRequestComplete()
                }
            else -> vm.onAbort()
        }
    }

    private fun storeCommonRotation() {
        val commonRotation = mPictureAdapter.commonRotation
        if (commonRotation != 0) {
            vm.incAutoRotatePicture(commonRotation)
        } else {
            if (mPictureAdapter.hadSomeRotations()) {
                vm.clearAutoRotatePicture()
            }
        }
    }

    private fun doEditEntry() {
        vm.onEditEntry()
    }

    private fun doEditProject() {
        vm.onEditProject()
    }

    private fun detectNoteError(): Boolean {
        if (!mainListFragment.isNotesComplete()) {
            showNoteError(mainListFragment.notes())
            return true
        }
        return false
    }

    private fun detectLoginError(): Boolean = loginFragment.detectLoginError()

    private fun showNoteError(notes: List<DataNote>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.title_notes)
        val sbuf = StringBuilder()
        for (note in notes) {
            if (note.num_digits > 0 && note.valueLength() > 0 && note.valueLength() != note.num_digits.toInt()) {
                sbuf.append("    ")
                sbuf.append(note.name)
                sbuf.append(": ")
                sbuf.append(getString(R.string.error_incorrect_note_count, note.valueLength(), note.num_digits))
                sbuf.append("\n")
            }
        }
        val msg = getString(R.string.error_incorrect_digit_count, sbuf.toString())
        builder.setMessage(msg)
        builder.setPositiveButton(android.R.string.yes) { dialog, _ -> showNoteErrorOk(dialog) }
        builder.setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showNoteErrorOk(dialog: DialogInterface) {
        if (BuildConfig.DEBUG) {
            vm.advance()
        } else {
            vm.btnNext()
        }
        dialog.dismiss()
    }

    fun checkCenterButtonIsEdit() {
        if (vm.isCenterButtonEdit) {
            buttonsFragment.vm.centerTextValue = getString(R.string.btn_edit)
        } else {
            buttonsFragment.vm.centerTextValue = getString(R.string.btn_add)
        }
    }

    private fun checkChangeCompanyButtonVisible() {
        if (vm.didAutoSkip) {
            buttonsFragment.vm.showCenterButtonValue = true
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        vm.onRestoreInstanceState(savedInstanceState.getString(KEY_TAKING_PICTURE, null))
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        PermissionHelper.instance.handlePermissionResult(requestCode, permissions, grantResults)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_TAKING_PICTURE, vm.onSaveInstanceState())
        super.onSaveInstanceState(outState)
    }

//    private fun isZipCode(tableZipCode: String?): Boolean {
//        return tableZipCode != null && tableZipCode.length == 5 && tableZipCode.matches("^[0-9]*$".toRegex())
//    }

    private fun showPictureToast(pictureCount: Int) {
        val msgId: Int
        if (pictureCount <= 0) {
            msgId = R.string.picture_help_1
        } else if (pictureCount <= 1) {
            msgId = R.string.picture_help_2
        } else if (pictureCount <= 2) {
            msgId = R.string.picture_help_3
        } else {
            return
        }
        val toast = Toast.makeText(this, msgId, Toast.LENGTH_LONG)
        val top = toast.view as ViewGroup
        val view = top.getChildAt(0)
        if (view is TextView) {
            view.textSize = resources.getDimension(R.dimen.picture_toast_size)
        }
        toast.show()
    }

    private fun setPhotoTitleCount(count: Int) {
        if (count == 1) {
            titleFragment.vm.titleValue = getString(R.string.title_photo)
        } else {
            titleFragment.vm.titleValue = getString(R.string.title_photos, count)
        }
    }

    override fun onErrorDialogOkay() {
        vm.onErrorDialogOkay()
    }

    fun showConfirmDialog() {
        dialogHelper.showConfirmDialog(object : DialogHelper.DialogListener {
            override fun onOkay() {
                vm.onConfirmOkay()
                mApp.ping()
            }

            override fun onCancel() {}
        })
    }

    private fun showServerError(message: String?) {
        dialogHelper.showServerError(message
                ?: "server error", object : DialogHelper.DialogListener {
            override fun onOkay() {}

            override fun onCancel() {
                showServerError = false
            }
        })
    }

}

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
import com.cartlc.tracker.model.data.DataAddress
import com.cartlc.tracker.model.data.DataEntry
import com.cartlc.tracker.model.data.DataNote
import com.cartlc.tracker.model.data.DataStates
import com.cartlc.tracker.ui.util.CheckError
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.event.EventError
import com.cartlc.tracker.model.event.EventRefreshProjects
import com.cartlc.tracker.model.flow.Action
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.PictureFlow
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.bits.AutoLinearLayoutManager
import com.cartlc.tracker.ui.list.*
import com.cartlc.tracker.ui.util.BitmapHelper
import com.cartlc.tracker.ui.util.DialogHelper
import com.cartlc.tracker.ui.util.LocationHelper
import com.cartlc.tracker.ui.util.PermissionHelper
import com.cartlc.tracker.ui.frag.*
import com.cartlc.tracker.viewmodel.MainViewModel

import java.io.File
import java.util.ArrayList
import java.util.Collections

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
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

    lateinit private var mApp: TBApplication
    lateinit private var mPictureAdapter: PictureListAdapter
    lateinit private var mInputMM: InputMethodManager
    private var fab_address: Address? = null
    private var mTakingPictureFile: File? = null
    private var fab_addressConfirmOkay = false
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
    val prefHelper: PrefHelper
        get() = vm.prefHelper
    val db: DatabaseTable
        get() = vm.db

//    private val isNewEquipmentOkay: Boolean
//        get() {
//            val name = prefHelper.projectName
//            return name == TBApplication.OTHER
//        }

    private val isAutoNarrowOkay: Boolean
        get() = vm.wasNext && vm.autoNarrowOkay

    private val statusHint: String
        get() {
            val sbuf = StringBuilder()
            val countPictures = prefHelper.numPicturesTaken
            val maxEquip = prefHelper.numEquipPossible
            val checkedEquipment = db.tableEquipment.queryChecked().size
            sbuf.append(getString(R.string.status_installed_equipments, checkedEquipment, maxEquip))
            sbuf.append("\n")
            sbuf.append(getString(R.string.status_installed_pictures, countPictures))
            return sbuf.toString()
        }

    private val editProjectHint: String
        get() {
            val sbuf = StringBuilder()
            sbuf.append(getString(R.string.entry_hint_edit_project))
            sbuf.append("\n")
            val name = prefHelper.projectName
            if (name != null) {
                sbuf.append(name)
                sbuf.append("\n")
            }
            sbuf.append(prefHelper.address)
            return sbuf.toString()
        }

    private val curProjectHint: String
        get() {
            val sbuf = StringBuilder()
            val name = prefHelper.projectName
            if (name != null) {
                sbuf.append(name)
                sbuf.append("\n")
            }
            sbuf.append(prefHelper.address)
            return sbuf.toString()
        }

    private inner class RotatePictureTask : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg voids: Void): Boolean? {
            autoRotatePictureResult()
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

        mApp.setUncaughtExceptionHandler(this)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        buttonsFragment.root = root
        buttonsFragment.vm.handleActionEvent().observe(this, Observer { event ->
            event.executeIfNotHandled { onActionDispatch(event.peekContent()) }
        })
        mInputMM = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        fab_add.setOnClickListener({ _: View -> doBtnPlus() })
        mPictureAdapter = PictureListAdapter(this, { newCount: Int -> setPhotoTitleCount(newCount) })
        val linearLayoutManager = AutoLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        list_pictures.layoutManager = linearLayoutManager
        list_pictures.adapter = mPictureAdapter

        prefHelper.setFromCurrentProjectId()

        vm.curFlow.observe(this, Observer { stage -> onStageChanged(stage) })

//        vm.onRestoreInstanceState(savedInstanceState)

        vm.error.observe(this, Observer { message -> showError(message) })

        vm.handleActionEvent().observe(this, Observer { event ->
            event.executeIfNotHandled { onActionDispatch(event.peekContent()) }
        })
        vm.detectNoteError = this::detectNoteError
        vm.entryTextValue = { entrySimpleFragment.entryTextValue }
        vm.detectLoginError = this::detectLoginError

        entrySimpleFragment.vm.handleGenericEvent().observe(this, Observer { event ->
            vm.doSimpleEntryReturn(event.peekContent())
        })

        EventBus.getDefault().register(this)
        title = mApp.versionedTitle
        getLocation()
    }

    private fun getLocation() {
        LocationHelper.instance.requestLocation(this, object : LocationHelper.OnLocationCallback {
            override fun onLocationUpdate(address: Address) {
                fab_address = address
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
                prefHelper.reloadFromServer()
                mApp.ping()
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
        // TODO: what safety check should I do now?
//        mDoingCenter = false // Safety
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

    private fun doBtnPlus() {
        if (prefHelper.currentEditEntryId != 0L) {
            prefHelper.clearLastEntry()
        }
        vm.btnPlus()
    }


    private fun doViewProject() {
        val intent = Intent(this, ListEntryActivity::class.java)
        startActivityForResult(intent, REQUEST_EDIT_ENTRY)
    }

    private fun doVehicles() {
        val intent = Intent(this, VehicleActivity::class.java)
        startActivity(intent)
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
                titleFragment.vm.subTitleValue = curProjectHint
                if (prefHelper.projectName == null) {
                    buttonsFragment.vm.showNextButtonValue = false
                }
                setList(R.string.title_project, PrefHelper.KEY_PROJECT, db.tableProjects.query(true))
                getLocation()
            }
            Stage.COMPANY -> {
                titleFragment.vm.subTitleValue = editProjectHint
                mainListFragment.showing = true
                buttonsFragment.vm.showCenterButtonValue = true
                val companies = db.tableAddress.query()
                autoNarrowCompanies(companies.toMutableList())
                val companyNames = getNames(companies)
                if (companyNames.size == 1 && vm.autoNarrowOkay) {
                    prefHelper.company = companyNames[0]
                    vm.skip()
                } else {
                    setList(R.string.title_company, PrefHelper.KEY_COMPANY, companyNames)
                    checkCenterButtonIsEdit()
                }
            }
            Stage.ADD_COMPANY -> {
                titleFragment.vm.titleValue = getString(R.string.title_company)
                entrySimpleFragment.vm.showingValue = true
                entrySimpleFragment.vm.simpleHintValue = getString(R.string.title_company)
                if (vm.isLocalCompany) {
                    vm.companyEditing = prefHelper.company
                    entrySimpleFragment.vm.simpleTextValue = vm.companyEditing ?: ""
                } else {
                    entrySimpleFragment.vm.simpleTextValue = ""
                }
            }
            Stage.STATE, Stage.ADD_STATE -> {
                var editing = flow.stage == Stage.ADD_STATE
                titleFragment.vm.subTitleValue = if (editing) editProjectHint else curProjectHint
                if (!editing) {
                    entrySimpleFragment.vm.helpTextValue = prefHelper.address
                    entrySimpleFragment.vm.showingValue = true
                }
                mainListFragment.showing = true
                buttonsFragment.vm.showNextButtonValue = false
                val company = prefHelper.company
                val zipcode = prefHelper.zipCode
                var states: MutableList<String> = db.tableAddress.queryStates(company!!, zipcode).toMutableList()
                if (states.size == 0) {
                    val state = zipcode?.let { db.tableZipCode.queryState(it) }
                    if (state != null) {
                        states = ArrayList()
                        states.add(state)
                    } else {
                        editing = true
                    }
                }
                if (editing) {
                    states = DataStates.getUnusedStates(states).toMutableList()
                    prefHelper.state = null
                    setList(R.string.title_state, PrefHelper.KEY_STATE, states)
                } else {
                    autoNarrowStates(states)
                    if (states.size == 1 && vm.autoNarrowOkay) {
                        prefHelper.state = states[0]
                        vm.skip()
                    } else {
                        if (setList(R.string.title_state, PrefHelper.KEY_STATE, states)) {
                            buttonsFragment.vm.showNextButtonValue = true
                        }
                        buttonsFragment.vm.showCenterButtonValue = true
                        checkChangeCompanyButtonVisible()
                    }
                }
            }
            Stage.CITY, Stage.ADD_CITY -> {
                var editing = flow.stage == Stage.ADD_CITY
                titleFragment.vm.subTitleValue = if (editing) editProjectHint else curProjectHint
                buttonsFragment.vm.showNextButtonValue = false
                if (!editing) {
                    entrySimpleFragment.vm.helpTextValue = prefHelper.address
                }
                val company = prefHelper.company
                val zipcode = prefHelper.zipCode
                val state = prefHelper.state
                var cities: MutableList<String> = db.tableAddress.queryCities(company!!, zipcode, state!!).toMutableList()
                if (cities.isEmpty()) {
                    val city = zipcode?.let { db.tableZipCode.queryCity(zipcode) }
                    if (city != null) {
                        cities = ArrayList()
                        cities.add(city)
                    } else {
                        editing = true
                    }
                }
                if (editing) {
                    entrySimpleFragment.vm.showingValue = true
                    titleFragment.vm.titleValue = getString(R.string.title_city)
                    entrySimpleFragment.vm.simpleTextValue = ""
                    entrySimpleFragment.vm.simpleHintValue = getString(R.string.title_city)
                } else {
                    autoNarrowCities(cities)
                    if (cities.size == 1 && vm.autoNarrowOkay) {
                        prefHelper.city = cities[0]
                        vm.skip()
                    } else {
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
                if (!editing) {
                    entrySimpleFragment.vm.helpTextValue = prefHelper.address
                }
                titleFragment.vm.subTitleValue = if (editing) editProjectHint else curProjectHint
                val streets = db.tableAddress.queryStreets(
                        prefHelper.company!!,
                        prefHelper.city!!,
                        prefHelper.state!!,
                        prefHelper.zipCode)
                if (streets.isEmpty()) {
                    editing = true
                }
                if (editing) {
                    titleFragment.vm.titleValue = getString(R.string.title_street)
                    entrySimpleFragment.vm.showingValue = true
                    entrySimpleFragment.vm.simpleTextValue = ""
                    entrySimpleFragment.vm.simpleHintValue = getString(R.string.title_street)
                    entrySimpleFragment.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                } else {
                    autoNarrowStreets(streets)
                    buttonsFragment.vm.showCenterButtonValue = true
                    mainListFragment.showing = true
                    if (streets.size == 1 && vm.autoNarrowOkay) {
                        prefHelper.street = streets[0]
                        fab_addressConfirmOkay = true
                        vm.skip()
                    } else {
                        if (setList(R.string.title_street, PrefHelper.KEY_STREET, streets)) {
                            buttonsFragment.vm.showNextButtonValue = true
                        }
                        checkChangeCompanyButtonVisible()
                    }
                }
            }
            Stage.CONFIRM_ADDRESS -> {
                if (fab_addressConfirmOkay) {
                    fab_addressConfirmOkay = false
                    titleFragment.vm.subTitleValue = curProjectHint
                    checkChangeCompanyButtonVisible()
                } else {
                    vm.skip()
                }
            }
            Stage.CURRENT_PROJECT -> {
                mApp.ping()
                prefHelper.saveProjectAndAddressCombo(vm.editProject)
                vm.editProject = false
                mainListFragment.showing = true
                titleFragment.vm.showSeparatorValue = true
                buttonsFragment.vm.showCenterButtonValue = true
                buttonsFragment.vm.prevTextValue = getString(R.string.btn_edit)
                if (db.tableProjectAddressCombo.count() > 0) {
                    fab_add.show()
                }
                buttonsFragment.vm.centerTextValue = getString(R.string.btn_new_project)
                titleFragment.vm.titleValue = getString(R.string.title_current_project)
                mainListFragment.setAdapter(flow.stage)
                checkErrors()
            }

            Stage.TRUCK -> {
                entrySimpleFragment.vm.showingValue = true
                entrySimpleFragment.vm.simpleHintValue = getString(R.string.title_truck)
                entrySimpleFragment.vm.simpleTextValue = prefHelper.truckValue
                entrySimpleFragment.vm.helpTextValue = getString(R.string.entry_hint_truck)
                entrySimpleFragment.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS

                mainListFragment.showing = true
                setList(R.string.title_truck, PrefHelper.KEY_TRUCK,
                        db.tableTruck.queryStrings(prefHelper.currentProjectGroup))
                if (prefHelper.currentProjectGroup != null) {
                    titleFragment.vm.subTitleValue = prefHelper.currentProjectGroup!!.hintLine
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
                val pictureCount = prefHelper.numPicturesTaken
                if (vm.wasNext) {
                    showPictureToast(pictureCount)
                    vm.wasNext = false
                }
                setPhotoTitleCount(pictureCount)
                buttonsFragment.vm.showNextButtonValue = false
                buttonsFragment.vm.showCenterButtonValue = true
                buttonsFragment.vm.centerTextValue = getString(R.string.btn_another)
                frame_pictures.visibility = View.VISIBLE
                list_pictures.visibility = View.VISIBLE
                val pictureFlow = flow as PictureFlow
                if (pictureCount < pictureFlow.expected) {
                    if (!dispatchPictureRequest()) {
                        showError(getString(R.string.error_cannot_take_picture))
                    }
                } else {
                    buttonsFragment.vm.showNextButtonValue = true
                    mPictureAdapter.setList(
                            db.tablePictureCollection.removeNonExistant(
                                    db.tablePictureCollection.queryPictures(prefHelper.currentPictureCollectionId
                                    )).toMutableList()
                    )
                }
            }
            Stage.STATUS -> {
                buttonsFragment.vm.nextTextValue = getString(R.string.btn_done)
                mainListFragment.showing = true
                mainListFragment.setAdapter(flow.stage)
                titleFragment.vm.titleValue = getString(R.string.title_status)
                titleFragment.vm.subTitleValue = statusHint
                vm.curEntry = null
            }
            Stage.CONFIRM -> {
                buttonsFragment.vm.nextTextValue = getString(R.string.btn_confirm)
                confirmationFragment.showing = true
                vm.curEntry = prefHelper.saveEntry()
                vm.curEntry?.let {
                    confirmationFragment.fill(it)
                }
                titleFragment.vm.titleValue = getString(R.string.title_confirmation)
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
            Action.NEW_PROJECT -> { vm.onNewProject() }
            Action.VIEW_PROJECT -> { doViewProject() }
            Action.PICTURE_REQUEST -> dispatchPictureRequest()
            Action.CONFIRM_DIALOG -> showConfirmDialog()
            Action.VEHICLES -> doVehicles()
            Action.BTN_PREV -> vm.btnPrev()
            Action.BTN_CENTER -> vm.btnCenter()
            Action.BTN_NEXT -> vm.btnNext()
            Action.BTN_CHANGE -> vm.btnChangeCompany()
        }
    }

    private fun getNames(companies: List<DataAddress>): List<String> {
        val list = ArrayList<String>()
        for (address in companies) {
            if (!list.contains(address.company)) {
                list.add(address.company)
            }
        }
        Collections.sort(list)
        return list
    }

    private fun autoNarrowCompanies(companies: MutableList<DataAddress>) {
        if (!isAutoNarrowOkay) {
            return
        }
        val companyNames = getNames(companies)
        if (companyNames.size == 1) {
            return
        }
        val address = fab_address
        if (address == null) {
            return
        }
        val reduced = ArrayList<DataAddress>()
        for (company in companies) {
            if (LocationHelper.instance.matchCompany(address, company)) {
                reduced.add(company)
            }
        }
        if (reduced.size == 0) {
            return
        }
        companies.clear()
        companies.addAll(reduced)
    }

    private fun autoNarrowStates(states: MutableList<String>) {
        if (!isAutoNarrowOkay) {
            return
        }
        if (states.size == 1) {
            return
        }
        if (fab_address == null) {
            return
        }
        val state = LocationHelper.instance.matchState(fab_address!!, states)
        if (state != null) {
            states.clear()
            states.add(state)
        }
    }

    private fun autoNarrowCities(cities: MutableList<String>) {
        if (!isAutoNarrowOkay) {
            return
        }
        if (cities.size == 1) {
            return
        }
        if (fab_address == null) {
            return
        }
        val city = LocationHelper.instance.matchCity(fab_address!!, cities)
        if (city != null) {
            cities.clear()
            cities.add(city)
        }
    }

    private fun autoNarrowStreets(streets: List<String>) {
        if (!isAutoNarrowOkay) {
            return
        }
        if (streets.size == 1) {
            return
        }
        if (fab_address == null) {
            return
        }
        LocationHelper.instance.reduceStreets(fab_address!!, streets.toMutableList())
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

    private fun dispatchPictureRequest(): Boolean {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val pictureFile = prefHelper.genFullPictureFile()
            db.tablePictureCollection.add(pictureFile, prefHelper.currentPictureCollectionId)
            val pictureUri = TBApplication.getUri(this, pictureFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)
            mTakingPictureFile = pictureFile
            // Grant permissions
            val resInfoList = packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(packageName, pictureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            // Start Camera activity
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            return true
        }
        return false
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

    private fun autoRotatePictureResult() {
        if (mTakingPictureFile != null && mTakingPictureFile!!.exists()) {
            val degrees = prefHelper.autoRotatePicture
            if (degrees != 0) {
                BitmapHelper.rotate(mTakingPictureFile!!, degrees)
            }
        }
    }

    private fun storeCommonRotation() {
        val commonRotation = mPictureAdapter.commonRotation
        if (commonRotation != 0) {
            prefHelper.incAutoRotatePicture(commonRotation)
        } else {
            if (mPictureAdapter.hadSomeRotations()) {
                prefHelper.clearAutoRotatePicture()
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
//        vm.onRestoreInstanceState(savedInstanceState)
        val path = savedInstanceState.getString(KEY_TAKING_PICTURE, null)
        if (path != null) {
            mTakingPictureFile = File(path)
        }
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        PermissionHelper.instance.handlePermissionResult(requestCode, permissions, grantResults)
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        vm.onSaveInstanceState(outState)
        if (mTakingPictureFile != null) {
            outState.putString(KEY_TAKING_PICTURE, mTakingPictureFile!!.absolutePath)
        }
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

    private fun checkErrors() {
        if (prefHelper.doErrorCheck) {
            val entry = vm.checkEntryErrors()
            if (entry != null) {
                CheckError.instance.showTruckError(this, prefHelper, entry,
                        object : CheckError.CheckErrorResult {
                            override fun doEdit() {
                                this@MainActivity.doEditEntry()
                            }

                            override fun doDelete(entry: DataEntry) {
                                db.tableEntry.remove(entry)
                            }
                        }
                )
            } else {
                prefHelper.doErrorCheck = false
            }
        }
        vm.checkProjectErrors()
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

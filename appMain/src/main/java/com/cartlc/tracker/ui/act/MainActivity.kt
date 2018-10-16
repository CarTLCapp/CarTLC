/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.act

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.location.Address
import android.os.*
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.cartlc.tracker.model.misc.TruckStatus
import com.cartlc.tracker.model.event.EventError
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.PictureFlow
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.bits.AutoLinearLayoutManager
import com.cartlc.tracker.ui.frag.LoginFragment
import com.cartlc.tracker.ui.frag.MainListFragment
import com.cartlc.tracker.ui.list.*
import com.cartlc.tracker.ui.util.BitmapHelper
import com.cartlc.tracker.ui.util.DialogHelper
import com.cartlc.tracker.ui.util.LocationHelper
import com.cartlc.tracker.ui.util.PermissionHelper
import com.cartlc.tracker.model.misc.EntryHint
import com.cartlc.tracker.model.misc.ErrorMessage
import com.cartlc.tracker.ui.frag.ConfirmationFragment
import com.cartlc.tracker.viewmodel.MainViewModel

import java.io.File
import java.util.ArrayList
import java.util.Collections

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    companion object {

        private val REQUEST_IMAGE_CAPTURE = 1
        private val REQUEST_EDIT_ENTRY = 2

        private val MSG_SET_HINT = 2
        private val NUM_MSGS = 4

        private val KEY_HINT = "hint"

        val RESULT_EDIT_ENTRY = 2
        val RESULT_EDIT_PROJECT = 3
        val RESULT_DELETE_PROJECT = 4

        private val KEY_TAKING_PICTURE = "picture"
    }

    private var mHandler = MyHandler()
    private var mSoftKeyboardDetect = SoftKeyboardDetect()
    lateinit private var mApp: TBApplication
    lateinit private var mPictureAdapter: PictureListAdapter
    lateinit private var mInputMM: InputMethodManager
    lateinit private var mZipCodeWatcher: ZipCodeWatcher
    lateinit private var mDialogHelper: DialogHelper
    private var fab_address: Address? = null
    private var mTakingPictureFile: File? = null
    private var mShowServerError = true
    private var fab_addressConfirmOkay = false
    private var mAutoNarrowOkay = true

    @Inject
    lateinit var vm: MainViewModel

    val loginFragment: LoginFragment
        get() = login_fragment as LoginFragment
    val mainListFragment: MainListFragment
        get() = main_list_fragment as MainListFragment
    val confirmationFragment: ConfirmationFragment
        get() = frame_confirmation_fragment as ConfirmationFragment
    val prefHelper: PrefHelper
        get() = vm.prefHelper
    val db: DatabaseTable
        get() = vm.db

    private val isNewEquipmentOkay: Boolean
        get() {
            val name = prefHelper.projectName
            return name == TBApplication.OTHER
        }

    private val versionedTitle: String
        get() {
            val sbuf = StringBuilder()
            sbuf.append(getString(R.string.app_name))
            sbuf.append(" - ")
            try {
                sbuf.append(mApp.version)
            } catch (ex: Exception) {
                TBApplication.ReportError(ex, MainActivity::class.java, "getVersionedTitle()", "main")
            }

            return sbuf.toString()
        }

    private val isAutoNarrowOkay: Boolean
        get() = vm.wasNext && mAutoNarrowOkay

    private val statusHint: String
        get() {
            val sbuf = StringBuilder()
            val countPictures = prefHelper.numPicturesTaken
            val maxEquip = prefHelper.numEquipPossible
            val checkedEquipment = db.equipment.queryChecked().size
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

    inner class MyHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_SET_HINT -> entry_hint.text = msg.data.getString(KEY_HINT)
            }
        }
    }

    private inner class SoftKeyboardDetect : ViewTreeObserver.OnGlobalLayoutListener {

        val ratio = 0.15

        fun clear() {
            buttons.visibility = View.VISIBLE
        }

        override fun onGlobalLayout() {
            val rect = Rect()
            root.rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = root.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            if (keypadHeight > screenHeight * ratio) {
                hideButtons()
            } else {
                restoreButtons()
            }
        }

        fun hideButtons() {
            if (buttons.getVisibility() == View.VISIBLE) {
                buttons.visibility = View.GONE
            }
        }

        fun restoreButtons() {
            if (buttons.getVisibility() != View.VISIBLE) {
                buttons.visibility = View.VISIBLE
            }
        }
    }

    private inner class ZipCodeWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable) {
            val value = s.toString().trim { it <= ' ' }
            if (isZipCode(value)) {
                mApp.requestZipCode(value)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mApp = applicationContext as TBApplication

        mApp.appComponent.inject(this)

        setContentView(R.layout.activity_main)

        mApp.setUncaughtExceptionHandler(this)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        root.viewTreeObserver.addOnGlobalLayoutListener(mSoftKeyboardDetect)
        mInputMM = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        next.setOnClickListener({ this.doBtnNextFromUser(it) })
        prev.setOnClickListener({ this.doBtnPrevFromUser(it) })
        change.setOnClickListener({ _ -> doBtnChangeCompany() })
        mDialogHelper = DialogHelper(this)
        btn_center.setOnClickListener({ _ -> doBtnCenter() })
        fab_add.setOnClickListener({ _: View -> doBtnPlus() })
        mPictureAdapter = PictureListAdapter(this, { newCount: Int -> setPhotoTitleCount(newCount) })
        val linearLayoutManager = AutoLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        list_pictures.layoutManager = linearLayoutManager
        list_pictures.adapter = mPictureAdapter
        val autoNext = object : OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                vm.doSimpleEntryReturn(getEditText(entry_simple))
                return false
            }
        }
        entry_simple.setOnEditorActionListener(autoNext)
        mZipCodeWatcher = ZipCodeWatcher()

        prefHelper.setFromCurrentProjectId()

        vm.curFlow.observe(this, Observer { stage -> onStageChanged(stage) })

        vm.computeCurStage()
        vm.onRestoreInstanceState(savedInstanceState)

        vm.error.observe(this, Observer { message -> showError(message) })

        vm.handleConfirmDialogEvent().observe(this, Observer {
            it.executeIfNotHandled { showConfirmDialog() }
        })
        vm.handleDispatchPictureRequestEvent().observe(this, Observer {
            it.executeIfNotHandled { dispatchPictureRequest() }
        })
        vm.detectNoteError = this::detectNoteError
        vm.entryTextValue = { getEditText(entry_simple) }
        vm.detectLoginError = this::detectLoginError

        EventBus.getDefault().register(this)
        title = versionedTitle
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
        mDialogHelper.clearDialog()
        LocationHelper.instance.onDestroy()
        for (what in 0 until NUM_MSGS) {
            mHandler.removeMessages(what)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EventError) {
        if (mShowServerError) {
            showServerError(event.toString())
        }
    }

    private fun getEditText(text: EditText): String {
        return text.text.toString().trim { it <= ' ' }
    }

    private fun doBtnNextFromUser(v: View) {
        vm.btnNext()
        mSoftKeyboardDetect.clear()
        TBApplication.hideKeyboard(this@MainActivity, v)
    }

    private fun doBtnPlus() {
        if (prefHelper.currentEditEntryId != 0L) {
            prefHelper.clearLastEntry()
        }
        vm.btnPlus()
    }

    private fun doBtnPrevFromUser(v: View) {
        vm.btnPrev()
        mSoftKeyboardDetect.clear()
        TBApplication.hideKeyboard(this@MainActivity, v)
    }

    private fun doBtnCenter() {
        vm.btnCenter()
    }

    private fun doBtnChangeCompany() {
        mAutoNarrowOkay = false
        vm.btnChangeCompany()
    }

    private fun doViewProject() {
        val intent = Intent(this, ListEntryActivity::class.java)
        startActivityForResult(intent, REQUEST_EDIT_ENTRY)
    }

    private fun onBtnProfile() {
        vm.btnProfile()
    }

    private fun onStageChanged(flow: Flow) {
        loginFragment.showing = false
        mainListFragment.showing = false
        mainListFragment.showEmpty = false
        frame_entry.visibility = View.GONE
        frame_status.visibility = View.GONE
        fab_add.hide()
        change.visibility = View.GONE
        btn_center.visibility = View.INVISIBLE
        btn_center.setText(R.string.btn_add)
        entry_simple.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS)
        entry_simple.removeTextChangedListener(mZipCodeWatcher)
        confirmationFragment.showing = false
        frame_pictures.visibility = View.GONE
        list_pictures.visibility = View.GONE
        list_entry_hint.visibility = View.GONE
        entry_hint.visibility = View.GONE
        sub_title.visibility = View.GONE
        main_title_separator.visibility = View.GONE

        next.visibility = if (flow.next == null) View.INVISIBLE else View.VISIBLE
        next.setText(R.string.btn_next)
        prev.visibility = if (flow.prev == null) View.INVISIBLE else View.VISIBLE
        prev.setText(R.string.btn_prev)

        when (flow.stage) {
            Stage.LOGIN -> {
                loginFragment.showing = true
                btn_center.visibility = View.VISIBLE
            }
            Stage.PROJECT -> {
                showMainListFrame()
                showSubTitleHint(flow.stage)
                if (prefHelper.projectName == null) {
                    next.visibility = View.INVISIBLE
                }
                setList(R.string.title_project, PrefHelper.KEY_PROJECT, db.projects.query(true))
                getLocation()
            }
            Stage.COMPANY -> {
                showSubTitleHint(flow.stage)
                showMainListFrame()
                btn_center.visibility = View.VISIBLE
                val companies = db.address.query()
                autoNarrowCompanies(companies.toMutableList())
                val companyNames = getNames(companies)
                if (companyNames.size == 1 && mAutoNarrowOkay) {
                    prefHelper.company = companyNames[0]
                    vm.skip()
                } else {
                    setList(R.string.title_company, PrefHelper.KEY_COMPANY, companyNames)
                    checkCenterButtonIsEdit()
                }
            }
            Stage.ADD_COMPANY -> {
                main_title_text.setText(R.string.title_company)
                frame_entry.visibility = View.VISIBLE
                entry_simple.setHint(R.string.title_company)
                if (vm.isLocalCompany) {
                    vm.companyEditing = prefHelper.company
                    entry_simple.setText(vm.companyEditing)
                } else {
                    entry_simple.setText("")
                }
            }
            Stage.STATE, Stage.ADD_STATE -> {
                showEntryHint(flow.stage)
                showSubTitleHint(flow.stage)
                showMainListFrame()
                next.visibility = View.INVISIBLE
                val company = prefHelper.company
                val zipcode = prefHelper.zipCode
                var states: MutableList<String> = db.address.queryStates(company!!, zipcode).toMutableList()
                var editing = flow.stage == Stage.ADD_STATE
                if (states.size == 0) {
                    val state = zipcode?.let { db.zipCode.queryState(it) }
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
                    if (states.size == 1 && mAutoNarrowOkay) {
                        prefHelper.state = states[0]
                        vm.skip()
                    } else {
                        if (setList(R.string.title_state, PrefHelper.KEY_STATE, states)) {
                            next.visibility = View.VISIBLE
                        }
                        btn_center.visibility = View.VISIBLE
                        checkChangeCompanyButtonVisible()
                    }
                }
            }
            Stage.CITY, Stage.ADD_CITY -> {
                showEntryHint(flow.stage)
                showSubTitleHint(flow.stage)
                next.visibility = View.INVISIBLE
                val company = prefHelper.company
                val zipcode = prefHelper.zipCode
                val state = prefHelper.state
                var cities: MutableList<String> = db.address.queryCities(company!!, zipcode, state!!).toMutableList()
                var editing = flow.stage == Stage.ADD_CITY
                if (cities.isEmpty()) {
                    val city = zipcode?.let { db.zipCode.queryCity(zipcode) }
                    if (city != null) {
                        cities = ArrayList()
                        cities.add(city)
                    } else {
                        editing = true
                    }
                }
                if (editing) {
                    frame_entry.visibility = View.VISIBLE
                    main_title_text.setText(R.string.title_city)
                    entry_simple.setHint(R.string.title_city)
                    entry_simple.setText("")
                } else {
                    autoNarrowCities(cities)
                    if (cities.size == 1 && mAutoNarrowOkay) {
                        prefHelper.city = cities[0]
                        vm.skip()
                    } else {
                        showMainListFrame()
                        btn_center.visibility = View.VISIBLE
                        if (setList(R.string.title_city, PrefHelper.KEY_CITY, cities)) {
                            next.visibility = View.VISIBLE
                        }
                        checkChangeCompanyButtonVisible()
                    }
                }
            }
            Stage.STREET, Stage.ADD_STREET -> {
                next.visibility = View.INVISIBLE
                showEntryHint(flow.stage)
                showSubTitleHint(flow.stage)
                val streets = db.address.queryStreets(
                        prefHelper.company!!,
                        prefHelper.city!!,
                        prefHelper.state!!,
                        prefHelper.zipCode)
                var editing = flow.stage == Stage.ADD_STREET
                if (streets.isEmpty()) {
                    editing = true
                }
                if (editing) {
                    main_title_text.setText(R.string.title_street)
                    frame_entry.visibility = View.VISIBLE
                    entry_simple.setHint(R.string.title_street)
                    entry_simple.setText("")
                    entry_simple.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS or InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                } else {
                    autoNarrowStreets(streets)
                    btn_center.visibility = View.VISIBLE
                    showMainListFrame()
                    if (streets.size == 1 && mAutoNarrowOkay) {
                        prefHelper.street = streets[0]
                        fab_addressConfirmOkay = true
                        vm.skip()
                    } else {
                        if (setList(R.string.title_street, PrefHelper.KEY_STREET, streets)) {
                            next.visibility = View.VISIBLE
                        }
                        checkChangeCompanyButtonVisible()
                    }
                }
            }
            Stage.CONFIRM_ADDRESS -> {
                if (fab_addressConfirmOkay) {
                    fab_addressConfirmOkay = false
                    showSubTitleHint(flow.stage)
                    checkChangeCompanyButtonVisible()
                } else {
                    vm.skip()
                }
            }
            Stage.CURRENT_PROJECT -> {
                mApp.ping()
                prefHelper.saveProjectAndAddressCombo(vm.editProject)
                vm.editProject = false
                showMainListFrame()
                main_title_separator.visibility = View.VISIBLE
                btn_center.visibility = View.VISIBLE
                prev.setText(R.string.btn_edit)
                if (db.projectAddressCombo.count() > 0) {
                    fab_add.show()
                }
                btn_center.setText(R.string.btn_new_project)
                main_title_text.setText(R.string.title_current_project)
                mainListFragment.setAdapter(flow.stage)
                checkErrors()
            }
            Stage.NEW_PROJECT -> {
                mAutoNarrowOkay = true
                vm.onNewProject()
            }
            Stage.VIEW_PROJECT -> {
                doViewProject()
            }
            Stage.TRUCK -> {
                frame_entry.visibility = View.VISIBLE
                entry_simple.setHint(R.string.title_truck)
                entry_simple.setText(prefHelper.truckValue)
                entry_simple.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                showMainListFrame()
                setList(R.string.title_truck, PrefHelper.KEY_TRUCK,
                        db.truck.queryStrings(prefHelper.currentProjectGroup))
                showEntryHint(flow.stage)
                showSubTitleHint(flow.stage)
            }
            Stage.EQUIPMENT -> {
                if (isNewEquipmentOkay) {
                    btn_center.visibility = View.VISIBLE
                }
                main_title_text.setText(R.string.title_equipment_installed)
                showMainListFrame()
                mainListFragment.setAdapter(flow.stage)
                btn_center.visibility = View.VISIBLE
            }
            Stage.ADD_EQUIPMENT -> {
                frame_entry.visibility = View.VISIBLE
                main_title_text.setText(R.string.title_equipment)
                entry_simple.setHint(R.string.title_equipment)
                entry_simple.setText("")
            }
            Stage.NOTES -> {
                main_title_text.setText(R.string.title_notes)
                showMainListFrame()
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
                next.visibility = View.INVISIBLE
                btn_center.visibility = View.VISIBLE
                btn_center.setText(R.string.btn_another)
                frame_pictures.visibility = View.VISIBLE
                list_pictures.visibility = View.VISIBLE
                val pictureFlow = flow as PictureFlow
                if (pictureCount < pictureFlow.expected) {
                    if (!dispatchPictureRequest()) {
                        showError(getString(R.string.error_cannot_take_picture))
                    }
                } else {
                    next.visibility = View.VISIBLE
                    mPictureAdapter.setList(
                            db.pictureCollection.removeNonExistant(
                                    db.pictureCollection.queryPictures(prefHelper.currentPictureCollectionId
                                    )).toMutableList()
                    )
                }
            }
            Stage.STATUS -> {
                next.setText(R.string.btn_done)
                frame_status.visibility = View.VISIBLE
                main_title_text.setText(R.string.title_status)
                setStatusButton()
                vm.curEntry = null
            }
            Stage.CONFIRM -> {
                next.setText(R.string.btn_confirm)
                confirmationFragment.showing = true
                vm.curEntry = prefHelper.saveEntry()
                vm.curEntry?.let {
                    confirmationFragment.fill(it)
                }
                main_title_text.setText(R.string.title_confirmation)
                storeCommonRotation()
            }
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
        main_title_text.setText(title)
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
            db.pictureCollection.add(pictureFile, prefHelper.currentPictureCollectionId)
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
        if (resultCode == RESULT_EDIT_ENTRY) {
            doEditEntry()
        } else if (resultCode == RESULT_EDIT_PROJECT) {
            doEditProject()
        } else if (resultCode == RESULT_DELETE_PROJECT) {
            vm.onDeletedProject()
        } else if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                RotatePictureTask().execute()
                vm.onPictureRequestComplete()
            }
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

    fun showError(error: ErrorMessage) {
        return showError(getErrorMessage(error))
    }

    private fun showError(error: String) {
        mDialogHelper.showError(error, object : DialogHelper.DialogListener {
            override fun onOkay() {
                vm.onErrorDialogOkay()
            }

            override fun onCancel() {}
        })
    }

    private fun getErrorMessage(error: ErrorMessage): String =
            getString(when (error) {
                ErrorMessage.ENTER_YOUR_NAME -> R.string.error_enter_your_name
                ErrorMessage.NEED_A_TRUCK -> R.string.error_need_a_truck_number
                ErrorMessage.NEED_NEW_COMPANY -> R.string.error_need_new_company
                ErrorMessage.NEED_EQUIPMENT -> R.string.error_need_equipment
                ErrorMessage.NEED_COMPANY -> R.string.error_need_company
                ErrorMessage.NEED_STATUS -> R.string.error_need_status
            })

    fun showEntryHint(entryHint: EntryHint) {
        if (entryHint.message.isNotEmpty()) {
            list_entry_hint.setText(entryHint.message)
            if (entryHint.isError) {
                list_entry_hint.setTextColor(ContextCompat.getColor(this, R.color.entry_error_color))
            } else {
                list_entry_hint.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
            list_entry_hint.visibility = View.VISIBLE
        } else {
            list_entry_hint.visibility = View.GONE
        }
    }

    fun showServerError(message: String?) {
        mDialogHelper.showServerError(message ?: "server error", object : DialogHelper.DialogListener {
            override fun onOkay() {}

            override fun onCancel() {
                mShowServerError = false
            }
        })
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
            btn_center.setText(R.string.btn_edit)
        } else {
            btn_center.setText(R.string.btn_add)
        }
    }

    private fun checkChangeCompanyButtonVisible() {
        if (vm.didAutoSkip) {
            change.visibility = View.VISIBLE
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        vm.onRestoreInstanceState(savedInstanceState)
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
        vm.onSaveInstanceState(outState)
        if (mTakingPictureFile != null) {
            outState.putString(KEY_TAKING_PICTURE, mTakingPictureFile!!.absolutePath)
        }
        super.onSaveInstanceState(outState)
    }

    private fun isZipCode(zipCode: String?): Boolean {
        return zipCode != null && zipCode.length == 5 && zipCode.matches("^[0-9]*$".toRegex())
    }

    private fun showEntryHint(stage: Stage) {
        var hint: String? = null
        when (stage) {
            Stage.STATE, Stage.CITY, Stage.STREET, Stage.CONFIRM_ADDRESS -> hint = prefHelper.address
            Stage.STATUS -> hint = statusHint
            Stage.TRUCK -> hint = getString(R.string.entry_hint_truck)
            else -> {
            }
        }
        if (hint != null && hint.length > 0) {
            entry_hint.setText(hint)
            entry_hint.visibility = View.VISIBLE
        }
    }

    private fun showSubTitleHint(stage: Stage) {
        var hint: String? = null
        when (stage) {
            Stage.PROJECT,
            Stage.COMPANY,
            Stage.STATE,
            Stage.CITY,
            Stage.STREET,
            Stage.CONFIRM_ADDRESS ->
                hint = curProjectHint
            Stage.ADD_COMPANY,
            Stage.ADD_STATE,
            Stage.ADD_CITY,
            Stage.ADD_STREET ->
                hint = editProjectHint
            Stage.TRUCK ->
                if (prefHelper.currentProjectGroup != null) {
                    hint = prefHelper.currentProjectGroup!!.hintLine
                }
            else -> {
            }
        }
        if (hint != null && hint.length > 0) {
            sub_title.setText(hint)
            sub_title.visibility = View.VISIBLE
        }
    }

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

    private fun showMainListFrame() {
        mainListFragment.showing = true
    }

    fun onStatusButtonClicked(view: View) {
        val checked = (view as RadioButton).isChecked
        if (checked) {
            when (view.getId()) {
                R.id.status_complete -> prefHelper.status = TruckStatus.COMPLETE
                R.id.status_partial -> prefHelper.status = TruckStatus.PARTIAL
                R.id.status_needs_repair -> prefHelper.status = TruckStatus.NEEDS_REPAIR
            }
        }
    }

    private fun setStatusButton() {
        val status = prefHelper.status
        if (status != null) {
            if (status === TruckStatus.NEEDS_REPAIR) {
                status_needs_repair.setChecked(true)
            } else if (status === TruckStatus.COMPLETE) {
                status_complete.setChecked(true)
            } else if (status === TruckStatus.PARTIAL) {
                status_partial.setChecked(true)
            } else {
                status_needs_repair.setChecked(false)
                status_complete.setChecked(false)
                status_partial.setChecked(false)
                status_select.clearCheck()
            }
        } else {
            status_needs_repair.setChecked(false)
            status_complete.setChecked(false)
            status_partial.setChecked(false)
            status_select.clearCheck()
        }
    }

    private fun setPhotoTitleCount(count: Int) {
        if (count == 1) {
            main_title_text.setText(getString(R.string.title_photo))
        } else {
            main_title_text.setText(getString(R.string.title_photos, count))
        }
    }

    private fun checkErrors() {
        if (prefHelper.doErrorCheck) {
            val entry = vm.checkEntryErrors()
            if (entry != null) {
                CheckError.instance.showTruckError(this, entry,
                        object : CheckError.CheckErrorResult {
                            override fun doEdit() {
                                this@MainActivity.doEditEntry()
                            }

                            override fun doDelete(entry: DataEntry) {
                                db.entry.remove(entry)
                            }
                        }
                )
            } else {
                prefHelper.doErrorCheck = false
            }
        }
        vm.checkProjectErrors()
    }

    fun showConfirmDialog() {
        mDialogHelper.showConfirmDialog(object : DialogHelper.DialogListener {
            override fun onOkay() {
                vm.onConfirmOkay()
                mApp.ping()
            }

            override fun onCancel() {}
        })
    }

}

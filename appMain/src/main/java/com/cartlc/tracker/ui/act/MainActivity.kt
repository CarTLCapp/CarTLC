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
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.cartlc.tracker.R
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.model.data.DataEntry
import com.cartlc.tracker.model.data.DataNote
import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.ui.util.CheckError
import com.cartlc.tracker.model.event.EventError
import com.cartlc.tracker.model.event.EventRefreshProjects
import com.cartlc.tracker.model.flow.ActionUseCase
import com.cartlc.tracker.model.msg.ErrorMessage
import com.cartlc.tracker.ui.base.BaseActivity
import com.cartlc.tracker.ui.bits.AutoLinearLayoutManager
import com.cartlc.tracker.ui.bits.SoftKeyboardDetect
import com.cartlc.tracker.ui.list.*
import com.cartlc.tracker.ui.util.helper.DialogHelper
import com.cartlc.tracker.ui.util.helper.LocationHelper
import com.cartlc.tracker.ui.util.helper.PermissionHelper
import com.cartlc.tracker.ui.frag.*
import com.cartlc.tracker.ui.bits.entrysimple.EntrySimpleView
import com.cartlc.tracker.ui.stage.StageNavigator
import com.cartlc.tracker.ui.stage.buttons.ButtonsView
import com.cartlc.tracker.ui.stage.newproject.NewProjectVMHolder
import com.cartlc.tracker.viewmodel.main.*
import com.crashlytics.android.Crashlytics // CRASHLYTICS
import io.fabric.sdk.android.Fabric // CRASHLYTICS

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.File
import java.lang.ref.WeakReference

class MainActivity : BaseActivity(), ActionUseCase.Listener {

    companion object {

        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_EDIT_ENTRY = 2

        const val RESULT_EDIT_ENTRY = 2
        const val RESULT_EDIT_PROJECT = 3
        const val RESULT_DELETE_PROJECT = 4

        private const val PRIVACY_POLICY_URL = "https://www.iubenda.com/privacy-policy/10260978"
        private const val KEY_TAKING_PICTURE = "picture"
    }

    private lateinit var app: TBApplication
    private lateinit var mPictureAdapter: PictureListAdapter
    private lateinit var mInputMM: InputMethodManager
    private var showServerError = true

    private val repo: CarRepository
        get() = app.repo

    lateinit var vm: MainVMHolder
    private lateinit var stageNavigator: StageNavigator

    private val mainListFragment: MainListFragment
        get() = frame_main_list as MainListFragment
    val confirmationFragment: ConfirmationFragment
        get() = frame_confirmation_fragment as ConfirmationFragment
    private val titleFragment: TitleFragment
        get() = frame_title as TitleFragment
    private val buttonsView: ButtonsView
        get() = frame_buttons as ButtonsView
    val entrySimpleView: EntrySimpleView
        get() = frame_entry_simple as EntrySimpleView

    private class RotatePictureTask(act: MainActivity) : AsyncTask<Void, Void, Boolean>() {

        private val ref = WeakReference<MainActivity>(act)
        private val main: MainActivity?
            get() = ref.get()

        override fun doInBackground(vararg voids: Void): Boolean? {
            main?.vm?.autoRotatePictureResult()
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            main?.let {
                if (!it.isDestroyed && !it.isFinishing && it.vm.isPictureStage) {
                    it.mPictureAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fabric.with(this, Crashlytics()) // CRASHLYTICS

        app = applicationContext as TBApplication

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        val softKeyboardDetect = SoftKeyboardDetect(root)
        buttonsView.softKeyboardDetect = softKeyboardDetect
        val buttonsUseCase = buttonsView.controller

        stageNavigator = StageNavigator(boundAct, buttonsUseCase)

        val confirmationViewModel = confirmationFragment.vm
        val mainListViewModel = mainListFragment.vm
        val titleViewModel = titleFragment.vm
        val entrySimpleControl = entrySimpleView.control

        val newProjectHolder = NewProjectVMHolder(
                boundAct,
                buttonsUseCase,
                mainListViewModel,
                titleViewModel,
                entrySimpleControl
        )
        vm = MainVMHolder(
                boundAct,
                newProjectHolder,
                buttonsUseCase,
                mainListViewModel,
                confirmationViewModel,
                titleViewModel,
                entrySimpleControl
        )
        mInputMM = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        fab_add.setOnClickListener { vm.btnPlus() }

        mPictureAdapter = PictureListAdapter(this) { newCount: Int -> titleFragment.vm.setPhotoTitleCount(newCount) }
        val linearLayoutManager = AutoLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        list_pictures.layoutManager = linearLayoutManager
        list_pictures.adapter = mPictureAdapter

        repo.actionUseCase.registerListener(this)
        vm.error.observe(this, Observer { message -> showError(message) })
        vm.addButtonVisible.observe(this, Observer { visible -> onAddButtonVisibleChanged(visible) })
        vm.framePictureVisible.observe(this, Observer { visible -> onFramePictureVisibleChanged(visible) })
        vm.error.observe(this, Observer<ErrorMessage> { message -> showError(message) })
        vm.notes = { mainListFragment.notes }

        entrySimpleControl.emsValue = resources.getInteger(R.integer.entry_simple_ems)

        componentRoot.eventController.register(this)
        title = app.versionedTitle

        getLocation()
    }

    override fun onStart() {
        super.onStart()
        repo.flowUseCase.notifyListeners()
    }

    private fun getLocation() {
        LocationHelper.instance.requestLocation(this, object : LocationHelper.OnLocationCallback {
            override fun onLocationUpdate(address: Address) {
                vm.fabAddress = address
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile -> vm.btnProfile()
            R.id.upload -> {
                app.reloadFromServer()
            }
            R.id.fleet_vehicles -> {
                vm.onVehiclesPressed()
            }
            R.id.privacy -> {
                onPrivacyPolicy()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onPrivacyPolicy() {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(PRIVACY_POLICY_URL)
        startActivity(i)
    }

    override fun onDestroy() {
        super.onDestroy()
        componentRoot.eventController.unregister(this)
        CheckError.instance.cleanup()
        LocationHelper.instance.onDestroy()
        repo.actionUseCase.unregisterListener(this)
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

    private fun onAddButtonVisibleChanged(visible: Boolean) {
        if (visible) {
            fab_add.show()
        } else {
            fab_add.hide()
        }
    }

    private fun onFramePictureVisibleChanged(visible: Boolean) {
        if (visible) {
            frame_pictures.visibility = View.VISIBLE
        } else {
            frame_pictures.visibility = View.GONE
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EventRefreshProjects) {
        Timber.d("onEvent(EventRefreshProjects)")
        repo.flowUseCase.notifyListeners()
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EventError) {
        if (showServerError) {
            showServerError(event.toString())
        }
    }

    override fun onActionChanged(action: Action) {
        when (action) {
            Action.PING -> app.ping()
            Action.VIEW_PROJECT -> doViewProject()
            is Action.PICTURE_REQUEST -> dispatchPictureRequest(action.file)
            Action.CONFIRM_DIALOG -> showConfirmDialog()
            Action.VEHICLES -> doVehicles()
            Action.VEHICLES_PENDING -> doVehiclesPendingDialog()
            Action.GET_LOCATION -> getLocation()
            Action.STORE_ROTATION -> storeCommonRotation()
            Action.SHOW_NOTE_ERROR -> showNoteError(mainListFragment.notes)
            is Action.SHOW_TRUCK_ERROR -> showTruckError(action.entry, action.callback)
            is Action.SET_MAIN_LIST -> mainListFragment.setList(action.list)
            is Action.SET_PICTURE_LIST -> mPictureAdapter.setList(action.list)
            is Action.SHOW_PICTURE_TOAST -> showPictureToast(action.count)
            is Action.CONFIRMATION_FILL -> confirmationFragment.fill(action.entry)
//            else -> stageNavigator.onActionDispatch(action)
        }
    }

    private fun showTruckError(entry: DataEntry, callback: CheckError.CheckErrorResult) {
        CheckError.instance.showTruckError(this, entry, callback)
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
                    RESULT_EDIT_ENTRY -> vm.onEditEntry()
                    RESULT_EDIT_PROJECT -> vm.onEditProject()
                    RESULT_DELETE_PROJECT -> vm.onDeletedProject()
                    else -> vm.onAbort()
                }
            REQUEST_IMAGE_CAPTURE ->
                if (resultCode == Activity.RESULT_OK) {
                    RotatePictureTask(this).execute()
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
        vm.showNoteErrorOk()
        dialog.dismiss()
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

    private fun showPictureToast(pictureCount: Int) {
        val msgId = when {
            pictureCount <= 0 -> R.string.picture_help_1
            pictureCount <= 1 -> R.string.picture_help_2
            pictureCount <= 2 -> R.string.picture_help_3
            else -> return
        }
        val toast = Toast.makeText(this, msgId, Toast.LENGTH_LONG)
        val top = toast.view as ViewGroup
        val view = top.getChildAt(0)
        if (view is TextView) {
            view.textSize = resources.getDimension(R.dimen.picture_toast_size)
        }
        toast.show()
    }

    override fun onErrorDialogOkay() {
        vm.onErrorDialogOkay()
    }

    private fun showConfirmDialog() {
        dialogHelper.showConfirmDialog(object : DialogHelper.DialogListener {
            override fun onOkay() {
                confirmationFragment.vm.onConfirmOkay()
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

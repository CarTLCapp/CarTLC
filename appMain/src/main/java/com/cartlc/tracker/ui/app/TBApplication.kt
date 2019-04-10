/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.app

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.VisibleForTesting
import androidx.multidex.MultiDex

import com.cartlc.tracker.BuildConfig
import com.cartlc.tracker.R
import com.cartlc.tracker.model.*
import com.cartlc.tracker.ui.util.CheckError
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.event.EventError
import com.cartlc.tracker.model.server.AmazonHelper
import com.cartlc.tracker.model.server.DCService
import com.cartlc.tracker.model.server.ServerHelper
import com.cartlc.tracker.model.sql.DatabaseManager
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.app.dependencyinjection.ComponentRoot
import com.cartlc.tracker.ui.util.helper.LocationHelper
import com.cartlc.tracker.ui.util.helper.PermissionHelper.PermissionRequest
import com.cartlc.tracker.ui.util.helper.PermissionHelper.PermissionListener

import com.cartlc.tracker.ui.util.helper.PermissionHelper
import com.cartlc.tracker.viewmodel.vehicle.DaggerVehicleViewModelComponent
import com.cartlc.tracker.viewmodel.vehicle.VehicleViewModel
import com.cartlc.tracker.viewmodel.vehicle.VehicleViewModelComponent
import com.cartlc.tracker.viewmodel.vehicle.VehicleViewModelModule
import com.squareup.leakcanary.LeakCanary

import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.io.File

class TBApplication : Application() {

    companion object {

        private val OVERRIDE_IS_DEVELOPMENT_SERVER: Boolean? = null

        @VisibleForTesting
        var DEBUG_TREE = false

        private const val LEAK_CANARY = false

        const val REPORT_LOCATION = false
        const val OTHER = "Other"

        private val PERMISSIONS = arrayOf(
                PermissionRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        R.string.perm_read_external_storage),
                PermissionRequest(Manifest.permission.READ_EXTERNAL_STORAGE,
                        R.string.perm_write_external_storage),
                PermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION,
                        R.string.perm_location))

        fun IsDevelopmentServer(): Boolean {
            return OVERRIDE_IS_DEVELOPMENT_SERVER ?: BuildConfig.DEBUG
        }

        fun getUri(ctx: Context, file: File): Uri {
            return FileProvider.getUriForFile(ctx, "com.cartcl.tracker.fileprovider", file)
        }

        fun hideKeyboard(ctx: Context, v: View) {
            val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }

        fun ReportError(ex: Exception, claz: Class<*>, function: String, type: String): String {
            return ReportError(ex.message
                    ?: "unknown", claz, function, type)
        }

        fun ReportError(msg: String, claz: Class<*>, function: String, type: String): String {
            val sbuf = StringBuilder()
            sbuf.append("Class:")
            sbuf.append(claz.simpleName)
            sbuf.append(".")
            sbuf.append(function)
            sbuf.append(" ")
            sbuf.append(type)
            sbuf.append(": ")
            sbuf.append(msg)
            Timber.e(sbuf.toString())
            return sbuf.toString()
        }

        fun ReportServerError(ex: Exception, claz: Class<*>, function: String, type: String) {
            val msg = ReportError(ex, claz, function, type)
            ShowError(msg)
        }

        fun ShowError(msg: String) {
            EventBus.getDefault().post(EventError(msg))
        }
    }

    val versionedTitle: String
        get() {
            val sbuf = StringBuilder()
            sbuf.append(getString(R.string.app_name))
            sbuf.append(" - ")
            try {
                sbuf.append(version)
            } catch (ex: Exception) {
                ReportError(ex, TBApplication::class.java, "versionedTitle", "main")
            }
            return sbuf.toString()
        }

//    lateinit var carRepoComponent: CarRepositoryComponent

    private lateinit var carRepo: CarRepository
    private lateinit var prefHelper: PrefHelper
    private lateinit var dm: DatabaseManager

    val repo: CarRepository
        get() = carRepo

    lateinit var componentRoot: ComponentRoot
    lateinit var vehicleComponent: VehicleViewModelComponent
    lateinit var amazonHelper: AmazonHelper

    private lateinit var vehicleViewModel: VehicleViewModel
    private lateinit var vehicleRepository: VehicleRepository

    val db: DatabaseTable
        get() = dm

    val version: String
        @Throws(PackageManager.NameNotFoundException::class)
        get() {
            val sbuf = StringBuilder()
            val version = packageManager.getPackageInfo(packageName, 0).versionName
            sbuf.append("v")
            sbuf.append(version)
            if (prefHelper.isDevelopment) {
                sbuf.append("d")
            }
            return sbuf.toString()
        }

    override fun onCreate() {
        super.onCreate()

        componentRoot = ComponentRoot(this)
        dm = DatabaseManager(this)
        prefHelper = PrefHelper(this, dm)
        carRepo = CarRepository(
                dm,
                prefHelper,
                componentRoot.flowUseCase
        )
        carRepo.computeCurStage()

        vehicleRepository = VehicleRepository(this, dm, prefHelper)
        vehicleViewModel = VehicleViewModel(vehicleRepository)
        vehicleComponent = DaggerVehicleViewModelComponent.builder()
                .vehicleViewModelModule(VehicleViewModelModule(vehicleViewModel))
                .build()

        if (LEAK_CANARY) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return
            }
            LeakCanary.install(this)
        }
        if (IsDevelopmentServer() && DEBUG_TREE) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree(db))
        }
        ServerHelper.Init(this)
        amazonHelper = AmazonHelper(db, prefHelper, componentRoot.eventController)
        PermissionHelper.Init()
        CheckError.Init()
        LocationHelper.Init(this, db)

        if (prefHelper.detectOneTimeReloadFromServerCheck()) {
            reloadFromServer()
        }
    }

    fun reloadFromServer() {
        prefHelper.reloadFromServer()
        ping()
    }

    fun ping() {
        if (ServerHelper.instance.hasConnection(this)) {
            startService(Intent(this, DCService::class.java))
        }
    }

//    fun requestZipCode(tableZipCode: String) {
//        val data = db.tableZipCode.query(tableZipCode)
//        if (data != null) {
//            data.check()
//            EventBus.getDefault().post(data)
//        } else if (ServerHelper.instance.hasConnection(this)) {
//            val intent = Intent(this, DCService::class.java)
//            intent.action = DCService.ACTION_ZIP_CODE
//            intent.putExtra(DCService.DATA_ZIP_CODE, tableZipCode)
//            startService(intent)
//        }
//    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun checkPermissions(act: Activity, listener: PermissionListener) {
        PermissionHelper.instance.checkPermissions(act, PERMISSIONS, listener)
    }

}

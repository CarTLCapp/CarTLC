/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.app.factory.FactoryController
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.base.BaseActivity
import com.cartlc.tracker.fresh.ui.bits.SoftKeyboardDetect
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    companion object {
        private const val KEY_TAKING_PICTURE = "picture"
    }

    private val factoryViewMvc: FactoryViewMvc
        get() = componentRoot.factoryViewMvc
    private val factoryController: FactoryController
        get() = componentRoot.factoryController

    private lateinit var controller: MainController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fabric.with(this, Crashlytics()) // CRASHLYTICS

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        val viewMvc = factoryViewMvc.allocMainViewMvc(content, boundAct.factoryViewHelper)
        (viewMvc as MainViewMvcImpl).fabAdd = fab_add // Give access to top layout button
        controller = factoryController.allocMainController(boundAct, viewMvc)
        controller.softKeyboardDetect = SoftKeyboardDetect(root)
        content.addView(viewMvc.rootView)

        title = controller.versionedTitle
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        controller.handlePermissionResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        controller.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (controller.onOptionsItemSelected(item.itemId)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // TODO: Do for real?
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_TAKING_PICTURE, controller.onSaveInstanceState())
        super.onSaveInstanceState(outState)
    }

    // TODO: Do for real?
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        controller.onRestoreInstanceState(savedInstanceState.getString(KEY_TAKING_PICTURE, null))
        super.onRestoreInstanceState(savedInstanceState)
    }


}

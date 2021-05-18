/**
 * Copyright 2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.daily.hours

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.app.factory.FactoryController
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.base.BaseActivity
import com.cartlc.tracker.fresh.ui.bits.HideOnSoftKeyboard
import kotlinx.android.synthetic.main.activity_main.*

class HoursActivity : BaseActivity() {

    private val factoryViewMvc: FactoryViewMvc
        get() = componentRoot.factoryViewMvc
    private val factoryController: FactoryController
        get() = componentRoot.factoryController

    private lateinit var controller: HoursController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val content = findViewById<ViewGroup>(R.id.content)
        val hoursViewMvc = factoryViewMvc.allocHoursViewMvc(content)
        val titleViewMvc = hoursViewMvc.titleViewMvc
        val buttonsViewMvc = hoursViewMvc.buttonsViewMvc

        controller = factoryController.allocHoursController(boundAct, hoursViewMvc, titleViewMvc, buttonsViewMvc)
        controller.hideOnSoftKeyboard = HideOnSoftKeyboard(root)

        content.addView(hoursViewMvc.rootView)

        title = getString(R.string.hours_title)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
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

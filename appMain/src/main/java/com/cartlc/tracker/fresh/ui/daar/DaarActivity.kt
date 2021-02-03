/**
 * Copyright 2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.daar

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

class DaarActivity : BaseActivity() {

    private val factoryViewMvc: FactoryViewMvc
        get() = componentRoot.factoryViewMvc
    private val factoryController: FactoryController
        get() = componentRoot.factoryController

    private lateinit var daarController: DaarController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val content = findViewById<ViewGroup>(R.id.content)
        val daarViewMvc = factoryViewMvc.allocDaarViewMvc(content)
        val titleViewMvc = daarViewMvc.titleViewMvc
        val buttonsViewMvc = daarViewMvc.buttonsViewMvc

        daarController = factoryController.allocDaarController(boundAct, daarViewMvc, titleViewMvc, buttonsViewMvc)
        daarController.hideOnSoftKeyboard = HideOnSoftKeyboard(root)

        content.addView(daarViewMvc.rootView)

        title = getString(R.string.daar_title)
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

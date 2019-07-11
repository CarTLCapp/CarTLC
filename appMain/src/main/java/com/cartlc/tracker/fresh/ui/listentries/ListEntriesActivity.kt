/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.ui.listentries

import android.os.Bundle
import android.view.MenuItem
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.app.factory.FactoryController
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_list_entries.*

class ListEntriesActivity: BaseActivity(), ListEntriesController.Listener {

    private val factoryViewMvc: FactoryViewMvc
        get() = componentRoot.factoryViewMvc
    private val factoryController: FactoryController
        get() = componentRoot.factoryController

    private lateinit var controller: ListEntriesController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_entries)
        setSupportActionBar(toolbar_list)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val viewMvc = factoryViewMvc.allocListEntriesViewMvc(content)
        controller = factoryController.allocListEntriesController(boundAct, viewMvc)
        controller.listener = this
        content.addView(viewMvc.rootView)
    }

    override var titleString: String?
        get() = title.toString()
        set(value) { title = value }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
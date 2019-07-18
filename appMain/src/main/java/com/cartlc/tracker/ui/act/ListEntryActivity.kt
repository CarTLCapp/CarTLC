/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.act

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.view.View

import com.cartlc.tracker.R
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.ui.bits.AutoLinearLayoutManager
import com.cartlc.tracker.ui.list.ListEntryAdapter
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_list_entries.*

class ListEntryActivity : BaseActivity(), ListEntryAdapter.OnItemSelectedListener {

    lateinit var app: TBApplication
    private lateinit var mEntryListAdapter: ListEntryAdapter

    val repo: CarRepository
        get() = app.repo
    val db: DatabaseTable
        get() = repo.db
    val prefHelper: PrefHelper
        get() = repo.prefHelper

    private val titleString: String
        get() {
            val sbuf = StringBuilder()
            sbuf.append(prefHelper.projectDashName)
            sbuf.append(" - ")
            val count = mEntryListAdapter.itemCount
            if (count == 1) {
                sbuf.append(getString(R.string.title_element))
            } else {
                sbuf.append(getString(R.string.title_elements, count))
            }
            return sbuf.toString()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_entries)
        app = applicationContext as TBApplication
        mEntryListAdapter = ListEntryAdapter(this, this)
        edit_address!!.setOnClickListener {
            prefHelper.setFromCurrentProjectId()
            setResult(MainController.RESULT_EDIT_PROJECT)
            finish()
        }
        delete!!.setOnClickListener {
            val projectGroup = prefHelper.currentProjectGroup
            if (projectGroup != null) {
                db.tableProjectAddressCombo.remove(projectGroup.id)
            }
            setResult(MainController.RESULT_DELETE_PROJECT)
            finish()
        }
        val linearLayoutManager = AutoLinearLayoutManager(this)
        list_entries!!.layoutManager = linearLayoutManager
        list_entries!!.adapter = mEntryListAdapter

        setSupportActionBar(toolbar_list)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        mEntryListAdapter.onDataChanged()
        setProjectDisplay()
        title = titleString
        setResult(Activity.RESULT_CANCELED)
        if (mEntryListAdapter.itemCount == 0) {
            delete!!.visibility = View.VISIBLE
        } else {
            delete!!.visibility = View.GONE
        }
    }

    private fun setProjectDisplay() {
        val combo = prefHelper.currentProjectGroup
        if (combo == null) {
            project_name!!.text = ""
            project_address!!.text = ""
        } else {
            project_name!!.text = combo.projectDashName
            project_address!!.text = combo.addressLine
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onEdit(entry: DataEntry) {
        prefHelper.setFromEntry(entry)
        setResult(MainController.RESULT_EDIT_ENTRY)
        finish()
    }
}

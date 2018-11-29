/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.viewmodel.frag

import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.data.*
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.LoginFlow
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.misc.EntryHint
import com.cartlc.tracker.model.misc.TruckStatus
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.sql.SqlTableEntry
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.viewmodel.BaseViewModel
import javax.inject.Inject

class MainListViewModel(private val repo: CarRepository) : BaseViewModel() {

    private val prefHelper: PrefHelper
        get() = repo.prefHelper

    var showing = ObservableBoolean(false)

    var showingValue: Boolean
        get() = showing.get()
        set(value) {
            showing.set(value)
        }

    var showEmpty = ObservableBoolean(false)

    var showEmptyValue: Boolean
        get() = showEmpty.get()
        set(value) {
            showEmpty.set(value)
        }

    var curKey: String? = null

    val curFlow: MutableLiveData<Flow>
        get() = repo.curFlow

    var curFlowValue: Flow
        get() = curFlow.value ?: LoginFlow()
        set(value) {
            curFlow.value = value
        }

    val isInNotes: Boolean
        get() = curFlowValue.stage == Stage.NOTES

    val status: TruckStatus?
        get() = prefHelper.status

    val key: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    var currentProjectGroup: DataProjectAddressCombo?
        get() = prefHelper.currentProjectGroup
        set(value) {
            prefHelper.currentProjectGroup = value
            onCurrentProjectGroupChanged()
        }

    var onCurrentProjectGroupChanged: () -> Unit = {}

    val currentProjectGroupId: Long
        get() = prefHelper.currentProjectGroupId

    val currentEditEntry: DataEntry?
        get() = prefHelper.currentEditEntry

    val keyValue: String?
        get() = curKey?.let { prefHelper.getKeyValue(it) }

    val entryHint: MutableLiveData<EntryHint> by lazy {
        MutableLiveData<EntryHint>()
    }

    val projectGroups: List<DataProjectAddressCombo>
        get() = repo.db.tableProjectAddressCombo.query()

    var entryHintValue: EntryHint
        get() = entryHint.value ?: EntryHint("", false)
        set(value) {
            entryHint.value = value
        }

    fun onStatusButtonClicked(status: TruckStatus) {
        prefHelper.status = status
    }

    fun setItemChecked(item: DataEquipment, isChecked: Boolean) {
        repo.db.tableEquipment.setChecked(item, isChecked)
    }

    fun queryForProject(currentProjectGroup: DataProjectAddressCombo): DataCollectionEquipmentProject =
            repo.db.tableCollectionEquipmentProject.queryForProject(currentProjectGroup.projectNameId)


    fun updateNoteValue(note: DataNote) {
        repo.db.tableNote.updateValue(note)
    }

    fun queryNotes(): List<DataNote> =
            currentProjectGroup?.let { repo.db.tableCollectionNoteProject.getNotes(it.projectNameId) }
                    ?: emptyList()

    fun countProjectAddressCombo(projectGroupId: Long): SqlTableEntry.Count =
            repo.db.tableEntry.countProjectAddressCombo(projectGroupId)

    init {
        key.observeForever { value ->
            curKey?.let {
                prefHelper.setKeyValue(it, value)
            }
        }
    }

}
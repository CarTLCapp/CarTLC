/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.viewmodel.frag

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.model.data.*
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.FlowUseCase
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.misc.EntryHint
import com.cartlc.tracker.model.misc.TruckStatus
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.sql.SqlTableEntry
import com.cartlc.tracker.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.viewmodel.BaseViewModel

class MainListViewModel(
        boundFrag: BoundFrag
) : BaseViewModel(), LifecycleObserver, FlowUseCase.Listener {

    private val repo = boundFrag.repo

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

    var curFlowValue: Flow
        get() = repo.curFlowValue
        set(value) {
            repo.curFlowValue = value
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

    init {
        boundFrag.bindObserver(this)
        repo.flowUseCase.registerListener(this)
    }

    // region lifecycle

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        repo.flowUseCase.unregisterListener(this)
    }

    // endregion lifecycle

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
        entryHintValue = EntryHint("", false)
        showingValue = false
        showEmptyValue = false
    }

    override fun onStageChanged(flow: Flow) {
    }

    /// endregion FlowUseCase.Listener

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

    fun countProjectAddressCombo(projectGroup: DataProjectAddressCombo): SqlTableEntry.Count =
        repo.db.tableEntry.countProjectAddressCombo(projectGroup.id)

    init {
        key.observeForever { value ->
            curKey?.let {
                prefHelper.setKeyValue(it, value)
            }
        }
    }

}
package com.cartlc.tracker.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.model.data.DataEntry
import com.cartlc.tracker.model.event.ActionEvent
import com.cartlc.tracker.model.flow.Action
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.LoginFlow
import com.cartlc.tracker.model.misc.ErrorMessage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.sql.DatabaseManager
import com.cartlc.tracker.model.table.DatabaseTable

class CarRepository(
        val context: Context,
        private val dm: DatabaseManager,
        val prefHelper: PrefHelper
) {
    val db: DatabaseTable
        get() = dm


    /** Current Flow **/

    val curFlow: MutableLiveData<Flow> by lazy {
        MutableLiveData<Flow>()
    }

    /** ErrorEvent **/

    val error: MutableLiveData<ErrorMessage> by lazy {
        MutableLiveData<ErrorMessage>()
    }

    var errorValue: ErrorMessage
        get() = error.value!!
        set(value) { error.value = value }

    /** ActionEvent **/

    private val handleAction: MutableLiveData<ActionEvent> by lazy {
        MutableLiveData<ActionEvent>()
    }

    fun handleActionEvent(): LiveData<ActionEvent> = handleAction

    fun dispatchActionEvent(action: Action) {
        handleAction.value = ActionEvent(action)
    }

    init {
        curFlow.value = LoginFlow()
    }

    val hasInsectingList: Boolean
        get() {
            return dm.tableVehicleName.vehicleNames.isNotEmpty()
        }

    fun checkProjectErrors(): Boolean {
        val entries = db.tableProjectAddressCombo.query()
        for (combo in entries) {
            if (!combo.hasValidState) {
                val address = combo.fix()
                if (address != null) {
                    db.tableAddress.update(address)
                }
            }
        }
        return false
    }

    fun checkEntryErrors(): DataEntry? {
        val entries = db.tableEntry.query()
        for (entry in entries) {
            if (entry.hasError) {
                return entry
            }
        }
        return null
    }

    fun clearUploaded() {
        dm.clearUploaded()
        prefHelper.clearUploaded()
    }

    fun add(entry: DataEntry) {
        if (db.tableEntry.add(entry)) {
            prefHelper.incNextEquipmentCollectionID()
            prefHelper.incNextPictureCollectionID()
            prefHelper.incNextNoteCollectionID()
        }
    }
}
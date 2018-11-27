package com.cartlc.tracker.viewmodel

import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.LoginFlow
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.misc.EntryHint
import com.cartlc.tracker.model.misc.TruckStatus
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.app.TBApplication
import javax.inject.Inject

class MainListViewModel(ctx: Context) : BaseViewModel() {

    @Inject
    lateinit var repo: CarRepository

    init {
        (ctx.applicationContext as TBApplication).carRepoComponent.inject(this)
    }

    private val prefHelper: PrefHelper
        get() = repo.prefHelper
    val tmpPrefHelper: PrefHelper
        get() = repo.prefHelper
    val tmpDb: DatabaseTable
        get() = repo.db

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

    val keyValue: String?
        get() = curKey?.let { prefHelper.getKeyValue(it) }

    val entryHint: MutableLiveData<EntryHint> by lazy {
        MutableLiveData<EntryHint>()
    }

    var entryHintValue: EntryHint
        get() = entryHint.value ?: EntryHint("", false)
        set(value) { entryHint.value = value }

    fun onStatusButtonClicked(status: TruckStatus) {
        prefHelper.status = status
    }

    init {
        key.observeForever { value ->
            curKey?.let {
                prefHelper.setKeyValue(it, value)
            }
        }
    }

}
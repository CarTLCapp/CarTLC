package com.cartlc.tracker.fresh.ui.mainlist

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.common.observable.BaseObservableImpl
import com.cartlc.tracker.fresh.model.flow.Flow
import com.cartlc.tracker.fresh.model.flow.FlowUseCase
import com.cartlc.tracker.fresh.model.flow.Stage
import com.cartlc.tracker.fresh.model.misc.EntryHint
import com.cartlc.tracker.fresh.model.misc.TruckStatus

class MainListController(
        boundAct: BoundAct,
        private val viewMvc: MainListViewMvc
) : BaseObservableImpl<MainListUseCase.Listener>(), LifecycleObserver,
        MainListUseCase,
        MainListViewMvc.Listener,
        FlowUseCase.Listener {

    private val ctx = boundAct.act
    private val repo = boundAct.repo
    private val prefHelper = repo.prefHelper
    private val status: TruckStatus?
        get() = prefHelper.status

    init {
        boundAct.bindObserver(this)
        repo.flowUseCase.registerListener(this)
    }

    // region lifecycle

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        viewMvc.registerListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        viewMvc.unregisterListener(this)
        repo.flowUseCase.unregisterListener(this)
    }

    // endregion lifecycle

    // region MainListUseCase

    override var visible: Boolean
        get() = viewMvc.visible
        set(value) {
            viewMvc.visible = value
        }

    override var key: String? = null

    override var keyValue: String?
        get() = key?.let { prefHelper.getKeyValue(it) }
        set(value) {
            key?.let {
                prefHelper.setKeyValue(it, value)
                for (listener in listeners) {
                    listener.onKeyValueChanged(it, value)
                }
            }
        }

    override var simpleItems: List<String>
        get() = viewMvc.simpleItems
        set(value) {
            viewMvc.adapter =  MainListViewMvc.Adapter.SIMPLE
            viewMvc.simpleItems = value
            setSimpleSelected()
        }

    override val areNotesComplete: Boolean
        get() = repo.isNotesComplete(viewMvc.notes)

    override val notes: List<DataNote>
        get() = viewMvc.notes

    // endregion MainListUseCase

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
        viewMvc.emptyVisible = false
        viewMvc.visible = false
    }

    override fun onStageChanged(flow: Flow) {
        when (flow.stage) {
            Stage.CURRENT_PROJECT -> {
                key = null
                viewMvc.adapter = MainListViewMvc.Adapter.PROJECT
            }
            Stage.EQUIPMENT -> {
                viewMvc.adapter = MainListViewMvc.Adapter.EQUIPMENT
            }
            Stage.NOTES -> {
                viewMvc.adapter = MainListViewMvc.Adapter.NOTE_ENTRY
                if (viewMvc.numNotes == 0) {
                    viewMvc.visible = false
                    viewMvc.emptyVisible = true
                } else {
                    viewMvc.visible = true
                }
            }
            Stage.STATUS -> {
                viewMvc.radioItems = listOf(
                        TruckStatus.COMPLETE.getString(ctx),
                        TruckStatus.PARTIAL.getString(ctx),
                        TruckStatus.NEEDS_REPAIR.getString(ctx)
                )
                viewMvc.radioSelectedText = status?.getStringNull(ctx)
            }
        }
    }

    // endregion FlowUseCase.Listener

    // region MainListViewMvc.Listener

    override fun onEntryHintChanged(entryHint: EntryHint) {
        for (listener in listeners) {
            listener.onEntryHintChanged(entryHint)
        }
    }

    override fun onSimpleItemClicked(position: Int, value: String) {
        keyValue = value
    }

    override fun onProjectGroupSelected(projectGroup: DataProjectAddressCombo) {
        for (listener in listeners) {
            listener.onProjectGroupSelected(projectGroup)
        }
    }

    override fun onRadioItemSelected(text: String) {
        prefHelper.status = TruckStatus.from(ctx, text)
    }

    // endregion MainListViewMvc.Listener

    private fun setSimpleSelected() {
        keyValue?.let {
            val position = viewMvc.setSimpleSelected(it)
            if (position >= 0) {
                viewMvc.scrollToPosition = position
            }
        } ?: run {
            viewMvc.setSimpleNoneSelected()
        }
    }

}

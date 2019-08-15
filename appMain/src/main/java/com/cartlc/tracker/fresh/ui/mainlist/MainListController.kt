package com.cartlc.tracker.fresh.ui.mainlist

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.fresh.model.core.data.DataFlowElement
import com.cartlc.tracker.fresh.model.core.data.DataFlowElement.Type
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.model.core.table.TableFlowElement
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
    private val checkBoxItemEnabled = mutableListOf<Boolean>()

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
            viewMvc.adapter = MainListViewMvc.Adapter.SIMPLE
            viewMvc.simpleItems = value
            setSimpleSelected()
        }

    override val areNotesComplete: Boolean
        get() = repo.isNotesComplete(viewMvc.notes)

    override val notes: List<DataNote>
        get() = viewMvc.notes

    override val isConfirmReady: Boolean
        get() = isAllChecked

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
            is Stage.CUSTOM_FLOW -> {
                repo.currentFlowElement?.let { element ->
                    when (element.type) {
                        Type.NONE ->
                            if (repo.db.tableFlowElementNote.hasNotes(element.id)) {
                                viewMvc.adapter = MainListViewMvc.Adapter.NOTE_ENTRY
                                viewMvc.visible = true
                            } else {
                                viewMvc.visible = false
                                viewMvc.emptyVisible = true
                            }
                        Type.CONFIRM_NEW,
                        Type.CONFIRM -> {
                            viewMvc.checkBoxItems = convert(repo.db.tableFlowElement.queryConfirmBatch(element.flowId, element.id))
                            buildCheckBoxMemory()
                            notifyConfirmListeners()
                        }
                        else -> {
                            viewMvc.visible = false
                            viewMvc.emptyVisible = false
                        }
                    }
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

    private fun convert(items: List<DataFlowElement>): List<String> {
        return TableFlowElement.convertToStrings(items)
    }

    private fun buildCheckBoxMemory() {
        checkBoxItemEnabled.clear()
        for (item in viewMvc.checkBoxItems) {
            checkBoxItemEnabled.add(prefHelper.getConfirmValue(item))
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

    override fun onCheckBoxItemChanged(position: Int, item: String, isChecked: Boolean) {
        checkBoxItemEnabled[position] = isChecked
        prefHelper.setConfirmValue(item, isChecked)
        notifyConfirmListeners()
    }

    private fun notifyConfirmListeners() {
        for (listener in listeners) {
            listener.onConfirmItemChecked(isAllChecked)
        }
    }

    private val isAllChecked: Boolean
        get() {
            for (i in 0 until checkBoxItemEnabled.size) {
                if (!checkBoxItemEnabled[i]) {
                    return false
                }
            }
            return true
        }

    override fun isCheckBoxItemSelected(position: Int): Boolean {
        return if (position < checkBoxItemEnabled.size) {
            checkBoxItemEnabled[position]
        } else {
            false
        }
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

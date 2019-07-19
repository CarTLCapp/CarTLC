package com.cartlc.tracker.fresh.ui.mainlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataEquipment
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.ui.app.factory.FactoryAdapterController
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvcImpl
import com.cartlc.tracker.fresh.ui.mainlist.adapter.*
import com.cartlc.tracker.fresh.model.misc.EntryHint

class MainListViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?,
        factoryViewMvc: FactoryViewMvc,
        factoryAdapterController: FactoryAdapterController
) : ObservableViewMvcImpl<MainListViewMvc.Listener>(),
        MainListViewMvc,
        SimpleListController.Listener,
        ProjectGroupListController.Listener,
        EquipmentSelectController.Listener,
        RadioListController.Listener,
        NoteListEntryController.Listener {

    override val rootView: View = inflater.inflate(R.layout.frame_main_list, container, false) as ViewGroup

    private val mainList = findViewById<RecyclerView>(R.id.main_list)
    private val emptyView = findViewById<TextView>(R.id.empty)
    private val simpleListController = factoryAdapterController.allocSimpleListController(this)
    private val simpleListAdapter = SimpleListAdapter(factoryViewMvc, R.layout.entry_item_simple, simpleListController)
    private val projectGroupController = factoryAdapterController.allocProjectGroupController(this)
    private val projectGroupAdapter = ProjectGroupListAdapter(factoryViewMvc, projectGroupController)
    private val equipmentSelectController = factoryAdapterController.allocEquipmentSelectController(this)
    private val equipmentSelectAdapter = EquipmentSelectListAdapter(factoryViewMvc, equipmentSelectController)
    private val radioController = factoryAdapterController.allocRadioListController(this)
    private val radioAdapter = RadioListAdapter(factoryViewMvc, radioController)
    private val noteListEntryController = factoryAdapterController.allocNoteListEntryController(this)
    private val noteListEntryAdapter = NoteListEntryAdapter(factoryViewMvc, noteListEntryController)

    init {
        val linearLayoutManager = LinearLayoutManager(mainList.context)
        mainList.layoutManager = linearLayoutManager
        val divider = DividerItemDecoration(mainList.context, linearLayoutManager.orientation)
        mainList.addItemDecoration(divider)
    }

    // region MainListViewMvc

    override var visible: Boolean
        get() = mainList.visibility == View.VISIBLE
        set(value) {
            mainList.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var emptyVisible: Boolean
        get() = emptyView.visibility == View.VISIBLE
        set(value) {
            emptyView.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var simpleItems: List<String>
        get() = simpleListAdapter.items
        set(value) {
            mainList.adapter = simpleListAdapter
            simpleListAdapter.items = value
        }

    override var radioItems: List<String>
        get() = radioController.list
        set(value) {
            mainList.adapter = radioAdapter
            radioController.list = value
        }

    override var radioSelectedText: String?
        get() = radioController.selectedText
        set(value) {
            radioController.selectedText = value
        }

    override var adapter: MainListViewMvc.Adapter
        get() = TODO("not implemented")
        set(value) {
            visible = true
            mainList.adapter = when (value) {
                MainListViewMvc.Adapter.SIMPLE -> simpleListAdapter
                MainListViewMvc.Adapter.PROJECT -> projectGroupAdapter
                MainListViewMvc.Adapter.EQUIPMENT -> equipmentSelectAdapter
                MainListViewMvc.Adapter.RADIO -> radioAdapter
                MainListViewMvc.Adapter.NOTE_ENTRY -> noteListEntryAdapter
            }
            when (value) {
                MainListViewMvc.Adapter.PROJECT -> projectGroupController.onProjectDataChanged()
                MainListViewMvc.Adapter.EQUIPMENT -> equipmentSelectController.onEquipmentDataChanged()
                MainListViewMvc.Adapter.NOTE_ENTRY -> noteListEntryController.onNoteDataChanged()
                else -> { }
            }
        }

    override val numNotes: Int
        get() = noteListEntryController.numNotes

    override val notes: List<DataNote>
        get() = noteListEntryController.notes

    override fun setSimpleNoneSelected() {
        simpleListAdapter.setNoneSelected()
    }

    override fun setSimpleSelected(value: String): Int {
        return simpleListAdapter.setSelected(value)
    }

    override var scrollToPosition: Int
        get() = TODO("not implemented")
        set(value) { mainList.scrollToPosition(value) }

    // endregion MainListViewMvc

    // region SimpleListController.Listener

    override var simpleSelectedPostion: Int
        get() = simpleListAdapter.selectedPos
        set(value) { simpleListAdapter.selectedPos = value }

    override fun onSimpleItemClicked(position: Int, text: String) {
        for (listener in listeners) {
            listener.onSimpleItemClicked(position, text)
        }
    }

    // endregion SimpleListController.Listener

    // region ProjectGroupController.Listener

    override fun onProjectRefreshNeeded() {
        projectGroupAdapter.notifyDataSetChanged()
    }

    override fun onProjectGroupSelected(projectGroup: DataProjectAddressCombo) {
        for (listener in listeners) {
            listener.onProjectGroupSelected(projectGroup)
        }
    }

    // endregion ProjectGroupController.Listener

    // region EquipmentSelectController.Listener

    override fun onEquipmentDataChanged(items: List<DataEquipment>) {
        equipmentSelectAdapter.items = items
    }

    // endregion EquipmentSelectController.Listener

    // region RadioListController.Listener

    override fun onRadioItemSelected(text: String) {
        for (listener in listeners) {
            listener.onRadioItemSelected(text)
        }
    }

    override fun onRadioRefreshNeeded() {
        radioAdapter.notifyDataSetChanged()
    }

    // endregion RadioListController.Listener

    // region NoteListEntryController.Listener

    override fun onEntryHintChanged(entryHint: EntryHint) {
        for (listener in listeners) {
            listener.onEntryHintChanged(entryHint)
        }
    }

    override fun onNotesChanged(items: List<DataNote>) {
        noteListEntryAdapter.items = items
    }

    // endregion NoteListEntryController.Listener
}
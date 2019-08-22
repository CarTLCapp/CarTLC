package com.cartlc.tracker.fresh.ui.confirm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvcImpl
import com.cartlc.tracker.fresh.ui.mainlist.adapter.SimpleListAdapter
import com.cartlc.tracker.fresh.ui.picture.PictureListUseCase
import com.cartlc.tracker.fresh.ui.picture.PictureListView
import com.cartlc.tracker.ui.list.NoteListAdapter

class ConfirmFinalViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?,
        factoryViewMvc: FactoryViewMvc
) : ObservableViewMvcImpl<ConfirmFinalViewMvc.Listener>(), ConfirmFinalViewMvc
{

    override val rootView: View = inflater.inflate(R.layout.frame_confirm, container, false) as ViewGroup

    private val ctx = rootView.context

    private val equipmentGrid = findViewById<RecyclerView>(R.id.equipment_grid)
    private val notesList = findViewById<RecyclerView>(R.id.notes_list)
    private val notesLabel = findViewById<TextView>(R.id.confirm_notes_label)
    private val confirmPicturesLabel = findViewById<TextView>(R.id.confirm_pictures_label)
    private val confirmPictureList = findViewById<PictureListView>(R.id.confirm_pictures_list)
    private val equipmentListAdapter: SimpleListAdapter = SimpleListAdapter(factoryViewMvc, R.layout.confirm_item, null)
    private val noteAdapter: NoteListAdapter
    private val projectNameValue = findViewById<TextView>(R.id.project_name_value)
    private val projectAddressValue = findViewById<TextView>(R.id.project_address_value)
    private val truckNumberValue = findViewById<TextView>(R.id.truck_number_value)
    private val statusValue = findViewById<TextView>(R.id.status_value)

    private val pictureUseCase = confirmPictureList.control as PictureListUseCase

    init {
        equipmentGrid.adapter = equipmentListAdapter
        val gridLayout = GridLayoutManager(ctx, 2)
        equipmentGrid.layoutManager = gridLayout
        noteAdapter = NoteListAdapter(ctx)
        notesList.adapter = noteAdapter
        notesList.layoutManager = LinearLayoutManager(ctx)
        pictureUseCase.isThumbnail = true
    }

    override var projectName: String
        get() = projectNameValue.text.toString()
        set(value) {
            projectNameValue.text = value
        }

    override var projectAddress: String?
        get() = projectAddressValue.text.toString()
        set(value) {
            projectAddressValue.text = value
            projectAddressValue.visibility = if (value == null) View.GONE else View.VISIBLE
        }

    override var truckNumber: String?
        get() = truckNumberValue.text.toString()
        set(value) {
            truckNumberValue.text = value
            truckNumberValue.visibility = if (value == null) View.GONE else View.VISIBLE
        }

    override var status: String
        get() = statusValue.text.toString()
        set(value) {
            statusValue.text = value
        }

    override var pictureLabel: String
        get() = confirmPicturesLabel.text.toString()
        set(value) {
            confirmPicturesLabel.text = value
        }

    override var notes: List<DataNote>
        get() = TODO("not implemented")
        set(value) {
            noteAdapter.setItems(value)
            notesLabel.visibility = if (value.isEmpty()) View.GONE else View.VISIBLE
        }

    override var equipmentNames: List<String>
        get() = TODO("not implemented")
        set(value) {
            equipmentListAdapter.items = value
        }

    override var pictures: List<DataPicture>
        get() = pictureUseCase.pictureItems
        set(value) {
            pictureUseCase.pictureItems = value
        }

}
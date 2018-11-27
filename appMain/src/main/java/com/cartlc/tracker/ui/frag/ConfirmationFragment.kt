package com.cartlc.tracker.ui.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.cartlc.tracker.R
import com.cartlc.tracker.databinding.FragConfirmationBinding
import com.cartlc.tracker.model.data.DataEntry
import com.cartlc.tracker.ui.bits.AutoLinearLayoutManager
import com.cartlc.tracker.ui.list.NoteListAdapter
import com.cartlc.tracker.ui.list.PictureThumbnailListAdapter
import com.cartlc.tracker.ui.list.SimpleListAdapter
import com.cartlc.tracker.viewmodel.ConfirmationViewModel

class ConfirmationFragment : BaseFragment() {

    private lateinit var binding: FragConfirmationBinding
    private lateinit var simpleAdapter: SimpleListAdapter
    private lateinit var noteAdapter: NoteListAdapter
    private lateinit var pictureAdapter: PictureThumbnailListAdapter

    val vm: ConfirmationViewModel
        get() = baseVM as ConfirmationViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragConfirmationBinding.inflate(layoutInflater, container, false)
        baseVM = ConfirmationViewModel(activity!!)
        binding.viewModel = vm
        super.onCreateView(inflater, container, savedInstanceState)

        val equipmentGrid = binding.equipmentGrid
        val notesList = binding.notesList
        val confirmPicturesList = binding.confirmPicturesList
        val ctx = context!!

        simpleAdapter = SimpleListAdapter(ctx, R.layout.entry_item_confirm)
        equipmentGrid.adapter = simpleAdapter
        val gridLayout = GridLayoutManager(ctx, 2)
        equipmentGrid.layoutManager = gridLayout
        noteAdapter = NoteListAdapter(ctx)
        notesList.adapter = noteAdapter
        notesList.layoutManager = LinearLayoutManager(ctx)
        pictureAdapter = PictureThumbnailListAdapter(ctx)
        val layoutManager = AutoLinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
        confirmPicturesList.layoutManager = layoutManager
        confirmPicturesList.adapter = pictureAdapter
        return binding.root
    }

    fun fill(entry: DataEntry) {
        val ctx = context!!
        val projectNameValue = binding.projectNameValue
        val projectAddressValue = binding.projectAddressValue
        val truckNumberValue = binding.truckNumberValue
        val statusValue = binding.statusValue
        val picturesLabel = binding.confirmPicturesLabel
        projectNameValue.text = entry.projectName
        val address = entry.addressBlock
        if (address.isNullOrBlank()) {
            projectAddressValue.visibility = View.GONE
        } else {
            projectAddressValue.visibility = View.VISIBLE
            projectAddressValue.text = address
        }
        noteAdapter.setItems(entry.notesWithValuesOnly)
        val truck = entry.truck
        if (truck == null) {
            truckNumberValue.visibility = View.GONE
        } else {
            truckNumberValue.text = truck.toString()
            truckNumberValue.visibility = View.VISIBLE
        }
        simpleAdapter.items = entry.equipmentNames!!
        pictureAdapter.setList(entry.pictures.toMutableList())
        statusValue.text = entry.getStatus(ctx)
        picturesLabel.text = ctx.getString(R.string.title_pictures_, entry.pictures.size)
    }
}
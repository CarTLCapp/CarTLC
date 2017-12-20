package com.cartlc.tracker.act;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataTruck;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dug on 5/15/17.
 */

public class ConfirmationFrame {

    @BindView(R.id.project_name_value)    TextView                    mProjectNameValue;
    @BindView(R.id.truck_number_value)    TextView                    mTruckNumberValue;
    @BindView(R.id.project_address_value) TextView                    mAddressValue;
    @BindView(R.id.status_value)          TextView                    mStatusValue;
    @BindView(R.id.equipment_grid)        RecyclerView                mEquipmentGrid;
    @BindView(R.id.notes_list)            RecyclerView                mNoteList;
    @BindView(R.id.confirm_pictures_list) RecyclerView                mPictureList;
    final                                 FrameLayout                 mTop;
    final                                 SimpleListAdapter           mSimpleAdapter;
    final                                 NoteListAdapter             mNoteAdapter;
    final                                 PictureThumbnailListAdapter mPictureAdapter;
    final                                 Context                     mCtx;

    public ConfirmationFrame(FrameLayout top) {
        mCtx = top.getContext();
        mTop = top;
        ButterKnife.bind(this, top);
        mSimpleAdapter = new SimpleListAdapter(mCtx, R.layout.entry_item_confirm);
        mEquipmentGrid.setAdapter(mSimpleAdapter);
        GridLayoutManager gridLayout = new GridLayoutManager(mCtx, 2);
        mEquipmentGrid.setLayoutManager(gridLayout);
        mNoteAdapter = new NoteListAdapter(mCtx);
        mNoteList.setAdapter(mNoteAdapter);
        mNoteList.setLayoutManager(new LinearLayoutManager(mCtx));
        mPictureAdapter = new PictureThumbnailListAdapter(mCtx);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mCtx, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setAutoMeasureEnabled(true);
        mPictureList.setLayoutManager(layoutManager);
        mPictureList.setAdapter(mPictureAdapter);
    }

    public void setVisibility(int code) {
        mTop.setVisibility(code);
    }

    public void fill(DataEntry entry) {
        mProjectNameValue.setText(entry.getProjectName());
        String address = entry.getAddressBlock();
        if (TextUtils.isEmpty(address)) {
            mAddressValue.setVisibility(View.GONE);
        } else {
            mAddressValue.setVisibility(View.VISIBLE);
            mAddressValue.setText(address);
        }
        mNoteAdapter.setItems(entry.getNotesWithValuesOnly());
        DataTruck truck = entry.getTruck();
        if (truck == null) {
            mTruckNumberValue.setVisibility(View.GONE);
        } else {
            mTruckNumberValue.setText(truck.toString());
            mTruckNumberValue.setVisibility(View.VISIBLE);
        }
        mSimpleAdapter.setList(entry.getEquipmentNames());
        mPictureAdapter.setList(entry.getPictures());
        mStatusValue.setText(entry.getStatus(mCtx));
    }
}

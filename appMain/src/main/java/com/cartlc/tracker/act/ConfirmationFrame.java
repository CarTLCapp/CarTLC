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
import com.cartlc.tracker.data.DataPicture;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dug on 5/15/17.
 */

public class ConfirmationFrame {

    @BindView(R.id.project_name_value)    TextView                    mProjectNameValue;
    @BindView(R.id.truck_number_value)    TextView                    mTruckNumberValue;
    @BindView(R.id.project_address_value) TextView                    mAddressValue;
    @BindView(R.id.project_notes_value)   TextView                    mNotesValue;
    @BindView(R.id.equipment_grid)        RecyclerView                mEquipmentGrid;
    @BindView(R.id.project_notes_label)   TextView                    mNotesLabel;
    @BindView(R.id.confirm_pictures_list) RecyclerView                mPictureList;
    final                                 FrameLayout                 mTop;
    final                                 SimpleListAdapter           mSimpleAdapter;
    final                                 GridLayoutManager           mGridLayout;
    final                                 PictureThumbnailListAdapter mPictureAdapter;

    public ConfirmationFrame(FrameLayout top) {
        final Context ctx = top.getContext();
        mTop = top;
        ButterKnife.bind(this, top);
        mSimpleAdapter = new SimpleListAdapter(ctx, R.layout.entry_item_confirm);
        mEquipmentGrid.setAdapter(mSimpleAdapter);
        mGridLayout = new GridLayoutManager(ctx, 2);
        mEquipmentGrid.setLayoutManager(mGridLayout);
        mPictureAdapter = new PictureThumbnailListAdapter(ctx);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setAutoMeasureEnabled(true);
        mPictureList.setLayoutManager(layoutManager);
        mPictureList.setAdapter(mPictureAdapter);
    }

    public void setVisibility(int code) {
        mTop.setVisibility(code);
    }

    public void fill(DataEntry entry) {
        mProjectNameValue.setText(entry.getProjectName());
        String address = entry.getAddressText();
        if (TextUtils.isEmpty(address)) {
            mAddressValue.setVisibility(View.GONE);
        } else {
            mAddressValue.setVisibility(View.VISIBLE);
            mAddressValue.setText(address);
        }
        String notes = entry.getNotes();
        if (TextUtils.isEmpty(notes)) {
            mNotesValue.setVisibility(View.GONE);
            mNotesLabel.setVisibility(View.GONE);
        } else {
            mNotesValue.setVisibility(View.VISIBLE);
            mNotesLabel.setVisibility(View.VISIBLE);
            mNotesValue.setText(notes);
        }
        if (entry.truckNumber == 0) {
            mTruckNumberValue.setVisibility(View.GONE);
        } else {
            mTruckNumberValue.setVisibility(View.VISIBLE);
            mTruckNumberValue.setText(Long.toString(entry.truckNumber));
        }
        mSimpleAdapter.setList(entry.getEquipmentNames());
        mPictureAdapter.setList(entry.getPictures());
    }
}

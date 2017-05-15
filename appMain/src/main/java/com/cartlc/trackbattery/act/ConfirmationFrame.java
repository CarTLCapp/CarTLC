package com.cartlc.trackbattery.act;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cartlc.trackbattery.R;
import com.cartlc.trackbattery.data.DataEntry;

/**
 * Created by dug on 5/15/17.
 */

public class ConfirmationFrame {

    final FrameLayout mTop;
    final TextView mProjectName;
    final TextView mTruckNumber;
    final TextView mAddress;
    final TextView mNotes;
    final RecyclerView mEquipmentGrid;
    final SimpleListAdapter mSimpleAdapter;
    final GridLayoutManager mGridLayout;

    public ConfirmationFrame(FrameLayout top) {
        mTop = top;
        mProjectName = (TextView) top.findViewById(R.id.project_name);
        mTruckNumber = (TextView) top.findViewById(R.id.truck_number);
        mAddress = (TextView) top.findViewById(R.id.project_address);
        mNotes = (TextView) top.findViewById(R.id.project_notes);
        mEquipmentGrid = (RecyclerView) top.findViewById(R.id.equipment_grid);
        mSimpleAdapter = new SimpleListAdapter(mTop.getContext(), R.layout.entry_item_confirm);
        mEquipmentGrid.setAdapter(mSimpleAdapter);
        mGridLayout = new GridLayoutManager(mTop.getContext(), 2);
        mEquipmentGrid.setLayoutManager(mGridLayout);
    }

    public void setVisibility(int code) {
        mTop.setVisibility(code);
    }

    public void fill(DataEntry entry) {
        mProjectName.setText(entry.getProjectName());
        String address = entry.getAddressText();
        if (TextUtils.isEmpty(address)) {
            mAddress.setVisibility(View.GONE);
        } else {
            mAddress.setVisibility(View.VISIBLE);
            mAddress.setText(address);
        }
        String notes = entry.getNotes();
        if (TextUtils.isEmpty(notes)) {
            mNotes.setVisibility(View.GONE);
        } else {
            mNotes.setVisibility(View.VISIBLE);
            mNotes.setText(notes);
        }
        if (entry.truckNumber == 0) {
            mTruckNumber.setVisibility(View.GONE);
        } else {
            mTruckNumber.setVisibility(View.VISIBLE);
            mTruckNumber.setText(Long.toString(entry.truckNumber));
        }
        mSimpleAdapter.setList(entry.getEquipmentNames());
    }
}

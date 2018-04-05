package com.cartlc.tracker.act;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataProjectAddressCombo;
import com.cartlc.tracker.etc.PrefHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dug on 5/12/17.
 */

public class ListEntryAdapter extends RecyclerView.Adapter<ListEntryAdapter.CustomViewHolder> {

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.truck_value) TextView truckValue;
        @BindView(R.id.status)      TextView status;
        @BindView(R.id.notes)       TextView notes;
        @BindView(R.id.equipments)  TextView equipments;
        @BindView(R.id.edit)        TextView edit;

        public CustomViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface OnItemSelectedListener {
        void onEdit(DataEntry entry);
    }

    final Context                mContext;
    final OnItemSelectedListener mListener;
    final LayoutInflater         mLayoutInflater;
    List<DataEntry> mItems;

    public ListEntryAdapter(Context context, OnItemSelectedListener listener) {
        mContext = context;
        mListener = listener;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.entry_item_full, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        final DataEntry item = mItems.get(position);
        holder.truckValue.setText(item.getTruckLine(mContext));
        holder.status.setText(item.getStatus(mContext));
        holder.notes.setText(item.getNotesLine());
        holder.equipments.setText(item.getEquipmentLine(mContext));
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onEdit(item);
            }
        });
        if (TextUtils.isEmpty(holder.notes.getText().toString().trim())) {
            holder.notes.setVisibility(View.GONE);
        } else {
            holder.notes.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void onDataChanged() {
        DataProjectAddressCombo combo = PrefHelper.getInstance().getCurrentProjectGroup();
        if (combo == null) {
            mItems = new ArrayList();
        } else {
            mItems = combo.getEntries();
        }
    }
}

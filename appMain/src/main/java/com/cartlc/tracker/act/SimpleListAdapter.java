/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.act;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cartlc.tracker.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dug on 5/10/17.
 */

public class SimpleListAdapter extends RecyclerView.Adapter<SimpleListAdapter.CustomViewHolder> {

    public interface OnItemSelectedListener {
        void onSelectedItem(int position, String text);
    }

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item) TextView simpleText;

        public CustomViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    final Context        mContext;
    final LayoutInflater mLayoutInflater;
    final int            mEntryItemLayoutId;
    OnItemSelectedListener mListener;
    List<String>           mItems;
    int     mSelectedPos  = -1;
    boolean mSelectedOkay = false;

    public SimpleListAdapter(Context context, OnItemSelectedListener listener) {
        mContext = context;
        mListener = listener;
        mEntryItemLayoutId = R.layout.entry_item_simple;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public SimpleListAdapter(Context context, int entryItemLayoutId) {
        mContext = context;
        mEntryItemLayoutId = entryItemLayoutId;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(mEntryItemLayoutId, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, int position) {
        final String text = mItems.get(position);
        holder.simpleText.setText(text);

        if (mSelectedOkay) {
            if (position == mSelectedPos) {
                holder.itemView.setBackgroundResource(R.color.list_item_selected);
            } else {
                holder.itemView.setBackgroundResource(android.R.color.transparent);
            }
        }
        holder.simpleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                setSelected(position);
                if (mListener != null) {
                    mListener.onSelectedItem(position, text);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setList(List<String> list) {
        mItems = list;
        notifyDataSetChanged();
    }

    public void setSelected(int position) {
        mSelectedPos = position;
        mSelectedOkay = true;
        notifyDataSetChanged();
    }

    public int setSelected(String value) {
        int position = mItems.indexOf(value);
        setSelected(position);
        return position;
    }

    public void setNoneSelected() {
        setSelected(-1);
    }
}
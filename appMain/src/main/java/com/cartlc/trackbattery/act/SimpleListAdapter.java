package com.cartlc.trackbattery.act;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cartlc.trackbattery.R;

import java.util.List;

/**
 * Created by dug on 5/10/17.
 */

public class SimpleListAdapter extends RecyclerView.Adapter<SimpleListAdapter.CustomViewHolder> {

    public interface OnItemSelectedListener {
        void onSelectedItem(int position, String text);
    }

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView simpleText;

        public CustomViewHolder(View view) {
            super(view);
            simpleText = (TextView) view.findViewById(R.id.item);
        }
    }

    final Context mContext;
    final int mEntryItemLayoutId;
    OnItemSelectedListener mListener;
    List<String> mItems;
    int mSelectedPos = -1;
    boolean mSelectedOkay = false;

    public SimpleListAdapter(Context context, OnItemSelectedListener listener) {
        mContext = context;
        mListener = listener;
        mEntryItemLayoutId = R.layout.entry_item_simple;
    }

    public SimpleListAdapter(Context context, int entryItemLayoutId) {
        mContext = context;
        mEntryItemLayoutId = entryItemLayoutId;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mEntryItemLayoutId, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, final int position) {
        final String text = mItems.get(position);
        holder.simpleText.setText(text);

        if (mSelectedOkay) {
            if (position == mSelectedPos) {
                holder.itemView.setBackgroundResource(R.color.colorSelected);
            } else {
                holder.itemView.setBackgroundResource(android.R.color.transparent);
            }
        }
        holder.simpleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
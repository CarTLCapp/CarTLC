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

    public interface OnSelectedItemListener {
        void onSelectedItem(int position, String text);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView simpleText;

        public CustomViewHolder(View view) {
            super(view);
            simpleText = (TextView) view.findViewById(R.id.simple_text);
        }
    }

    final Context mContext;
    OnSelectedItemListener mListener;
    List<String> mItems;
    int mSelectedPos;

    public SimpleListAdapter(Context context, OnSelectedItemListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_item_simple, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, final int position) {
        final String text = mItems.get(position);
        holder.simpleText.setText(text);

        if (position == mSelectedPos) {
            holder.simpleText.setBackgroundResource(R.color.colorSelected);
        } else {
            holder.simpleText.setBackgroundResource(android.R.color.transparent);
        }
        holder.simpleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    setSelected(position);
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
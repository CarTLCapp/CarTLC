package com.cartlc.tracker.act;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DataNote;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dug on 5/12/17.
 */

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.CustomViewHolder> {

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.label) TextView label;
        @BindView(R.id.value) TextView value;

        public CustomViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    final protected Context        mContext;
    final protected LayoutInflater mLayoutInflater;
    protected       List<DataNote> mItems;

    public NoteListAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.entry_item_note, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        final DataNote item = mItems.get(position);
        holder.label.setText(item.name);
        holder.value.setText(item.value);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItems(List<DataNote> items) {
        mItems = items;
        notifyDataSetChanged();
    }

}

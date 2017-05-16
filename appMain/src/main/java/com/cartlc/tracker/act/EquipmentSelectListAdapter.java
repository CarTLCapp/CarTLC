package com.cartlc.tracker.act;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DataEquipment;
import com.cartlc.tracker.data.DataEquipmentProjectCollection;
import com.cartlc.tracker.data.DataProjectGroup;
import com.cartlc.tracker.data.PrefHelper;
import com.cartlc.tracker.data.TableEquipment;
import com.cartlc.tracker.data.TableEquipmentProjectCollection;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dug on 5/12/17.
 */

public class EquipmentSelectListAdapter extends RecyclerView.Adapter<EquipmentSelectListAdapter.CustomViewHolder> {

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item) CheckBox checkBox;

        public CustomViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    final protected Context             mContext;
    protected       List<DataEquipment> mItems;

    public EquipmentSelectListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_item_equipment, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, final int position) {
        final DataEquipment item = mItems.get(position);
        holder.checkBox.setText(item.name);
        holder.checkBox.setChecked(item.isChecked);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TableEquipment.getInstance().setChecked(item, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void onDataChanged() {
        DataProjectGroup curGroup = PrefHelper.getInstance().getCurrentProjectGroup();
        DataEquipmentProjectCollection collection = TableEquipmentProjectCollection.getInstance().queryForProject(curGroup.projectNameId);
        mItems = collection.getEquipment();
        notifyDataSetChanged();
    }

}

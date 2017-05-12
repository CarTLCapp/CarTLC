package com.cartlc.trackbattery.act;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.cartlc.trackbattery.R;
import com.cartlc.trackbattery.data.DataEquipment;
import com.cartlc.trackbattery.data.DataProjectGroup;
import com.cartlc.trackbattery.data.PrefHelper;
import com.cartlc.trackbattery.data.TableEquipment;
import com.cartlc.trackbattery.data.TableProjects;

import java.util.List;

/**
 * Created by dug on 5/12/17.
 */

public class EquipmentListAdapter extends RecyclerView.Adapter<EquipmentListAdapter.CustomViewHolder> {

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public CustomViewHolder(View view) {
            super(view);
            checkBox = (CheckBox) view.findViewById(R.id.item);
        }
    }

    final protected Context mContext;
    protected List<DataEquipment> mItems;

    public EquipmentListAdapter(Context context) {
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
        mItems = TableEquipment.getInstance().query(curGroup.projectNameId);
        notifyDataSetChanged();
    }

}

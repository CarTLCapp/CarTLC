package com.cartlc.tracker.act;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DataEquipment;
import com.cartlc.tracker.data.DataEquipmentProjectCollection;
import com.cartlc.tracker.data.DataProjectAddressCombo;
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

    static final int MSG_CHANGED = 0;

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHANGED:
                    notifyDataSetChanged();
                    break;
            }
        }
    }

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item) RadioButton radioButton;

        public CustomViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    final protected Context             mContext;
    protected       List<DataEquipment> mItems;
    protected MyHandler mHandler = new MyHandler();
    protected DataEquipment mLastCheckedItem;
    protected RadioButton   mLastCheckedView;

    public EquipmentSelectListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_item_equipment, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        final DataEquipment item = mItems.get(position);
        holder.radioButton.setText(item.name);
        holder.radioButton.setOnCheckedChangeListener(null);

        if (item.isChecked) {
            if ((mLastCheckedItem == null) || (mLastCheckedItem == item)) {
                mLastCheckedItem = item;
                mLastCheckedView = holder.radioButton;
                holder.radioButton.setChecked(true);
            } else {
                // Ignore: uncheck .. can only have one.
                item.isChecked = false;
                holder.radioButton.setChecked(false);
                TableEquipment.getInstance().setChecked(item, false);
            }
        } else {
            holder.radioButton.setChecked(false);
        }
        holder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mLastCheckedItem != null) {
                        mLastCheckedItem.isChecked = false;
                        TableEquipment.getInstance().setChecked(mLastCheckedItem, false);
                        mLastCheckedView.setOnCheckedChangeListener(null);
                        mLastCheckedView.setChecked(false);
                    }
                    item.isChecked = true;
                    TableEquipment.getInstance().setChecked(item, true);
                    mLastCheckedView = holder.radioButton;
                    mLastCheckedItem = item;
                    mHandler.sendEmptyMessage(MSG_CHANGED);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void onDataChanged() {
        DataProjectAddressCombo curGroup = PrefHelper.getInstance().getCurrentProjectGroup();
        DataEquipmentProjectCollection collection = TableEquipmentProjectCollection.getInstance().queryForProject(curGroup.projectNameId);
        mItems = collection.getEquipment();
        mLastCheckedItem = null;
        mLastCheckedView = null;
        notifyDataSetChanged();
    }

    public boolean hasChecked() {
        DataProjectAddressCombo curGroup = PrefHelper.getInstance().getCurrentProjectGroup();
        DataEquipmentProjectCollection collection = TableEquipmentProjectCollection.getInstance().queryForProject(curGroup.projectNameId);
        for (DataEquipment item : collection.getEquipment()) {
            if (item.isChecked) {
                return true;
            }
        }
        return false;
    }
}

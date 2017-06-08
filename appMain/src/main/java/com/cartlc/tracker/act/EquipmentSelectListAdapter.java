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
import com.cartlc.tracker.data.DataCollectionEquipmentProject;
import com.cartlc.tracker.data.DataProjectAddressCombo;
import com.cartlc.tracker.data.PrefHelper;
import com.cartlc.tracker.data.TableEquipment;
import com.cartlc.tracker.data.TableCollectionEquipmentProject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by dug on 5/12/17.
 */

public class EquipmentSelectListAdapter extends RecyclerView.Adapter<EquipmentSelectListAdapter.CustomViewHolder> {

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item) CheckBox checkButton;

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
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        final DataEquipment item = mItems.get(position);
        holder.checkButton.setText(item.name);
        holder.checkButton.setOnCheckedChangeListener(null);
        holder.checkButton.setChecked(item.isChecked);
        holder.checkButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.isChecked = isChecked;
                TableEquipment.getInstance().setChecked(item, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void onDataChanged() {
        DataProjectAddressCombo curGroup = PrefHelper.getInstance().getCurrentProjectGroup();
        if (curGroup != null) {
            Timber.d("MYDEBUG: PROJECT-ID=" + curGroup.projectNameId);
            DataCollectionEquipmentProject collection = TableCollectionEquipmentProject.getInstance().queryForProject(curGroup.projectNameId);
            mItems = collection.getEquipment();
            notifyDataSetChanged();
        } else {
            Timber.d("MYDEBUG: NULL GROUP");
        }
    }

    public boolean hasChecked() {
        DataProjectAddressCombo curGroup = PrefHelper.getInstance().getCurrentProjectGroup();
        if (curGroup != null) {
            DataCollectionEquipmentProject collection = TableCollectionEquipmentProject.getInstance().queryForProject(curGroup.projectNameId);
            for (DataEquipment item : collection.getEquipment()) {
                if (item.isChecked) {
                    return true;
                }
            }
        }
        return false;
    }
}

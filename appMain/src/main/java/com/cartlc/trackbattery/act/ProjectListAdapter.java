package com.cartlc.trackbattery.act;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cartlc.trackbattery.R;
import com.cartlc.trackbattery.data.DataAddress;
import com.cartlc.trackbattery.data.DataProjectGroup;
import com.cartlc.trackbattery.data.PrefHelper;
import com.cartlc.trackbattery.data.TableEntries;
import com.cartlc.trackbattery.data.TableProjectGroups;

import java.util.List;

/**
 * Created by dug on 5/10/17.
 */

public class ProjectListAdapter extends RecyclerView.Adapter<ProjectListAdapter.CustomViewHolder> {

    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView mProjectName;
        TextView mProjectNotes;
        TextView mProjectAddress;

        public CustomViewHolder(View view) {
            super(view);
            mProjectName = (TextView) view.findViewById(R.id.project_name);
            mProjectNotes = (TextView) view.findViewById(R.id.project_notes);
            mProjectAddress = (TextView) view.findViewById(R.id.project_address);
        }
    }

    final Context mContext;
    List<DataProjectGroup> mProjectGroups;
    Long mCurProjectGroupId;

    public ProjectListAdapter(Context context) {
        mContext = context;
        onDataChanged();
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_item_project, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, final int position) {
        final DataProjectGroup projectGroup = mProjectGroups.get(position);
        holder.mProjectName.setText(projectGroup.getProjectName());
        int count = TableEntries.getInstance().count(projectGroup.projectNameId);
        if (count > 0) {
            holder.mProjectNotes.setText(Integer.toString(count));
            holder.mProjectNotes.setVisibility(View.VISIBLE);
        } else {
            holder.mProjectNotes.setText("");
            holder.mProjectNotes.setVisibility(View.GONE);
        }
        DataAddress address = projectGroup.getAddress();
        if (address == null) {
            holder.mProjectAddress.setText(null);
        } else {
            holder.mProjectAddress.setText(address.getBlock());
        }
        if (projectGroup.id == mCurProjectGroupId) {
            holder.mProjectName.setBackgroundResource(R.color.project_highlight);
            holder.mProjectAddress.setBackgroundResource(R.color.address_highlight);
        } else {
            holder.mProjectName.setBackgroundResource(R.color.project_normal);
            holder.mProjectAddress.setBackgroundResource(R.color.address_normal);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelected(projectGroup);
            }
        });
    }


    public void setSelected(DataProjectGroup group) {
        mCurProjectGroupId = group.id;
        PrefHelper.getInstance().setCurrentProjectGroup(group);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mProjectGroups.size();
    }

    void onDataChanged() {
        mProjectGroups = TableProjectGroups.getInstance().query();
        mCurProjectGroupId = PrefHelper.getInstance().getCurrentProjectGroupId();
        notifyDataSetChanged();
    }
}
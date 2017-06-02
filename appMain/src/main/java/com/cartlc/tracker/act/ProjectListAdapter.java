package com.cartlc.tracker.act;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DataAddress;
import com.cartlc.tracker.data.DataProjectAddressCombo;
import com.cartlc.tracker.data.PrefHelper;
import com.cartlc.tracker.data.TableEntry;
import com.cartlc.tracker.data.TableProjectAddressCombo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */

public class ProjectListAdapter extends RecyclerView.Adapter<ProjectListAdapter.CustomViewHolder> {

    class CustomViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.project_name)    TextView mProjectName;
        @BindView(R.id.project_notes)   TextView mProjectNotes;
        @BindView(R.id.project_address) TextView mProjectAddress;

        public CustomViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    final Context mContext;
    List<DataProjectAddressCombo> mProjectGroups;
    Long                          mCurProjectGroupId;

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
        final DataProjectAddressCombo projectGroup = mProjectGroups.get(position);
        holder.mProjectName.setText(projectGroup.getProjectName());
        int countTotal = TableEntry.getInstance().countProjectAddressCombo(projectGroup.id);
        if (countTotal > 0) {
            int           countUploaded = TableEntry.getInstance().countFullyUploaded(projectGroup.id);
            StringBuilder sbuf          = new StringBuilder();
            sbuf.append(mContext.getString(R.string.title_entries_));
            sbuf.append(" ");
            sbuf.append(Integer.toString(countTotal));
            sbuf.append("   ");
            sbuf.append(mContext.getString(R.string.title_uploaded_));
            sbuf.append(" ");
            sbuf.append(Integer.toString(countUploaded));
            holder.mProjectNotes.setText(sbuf.toString());
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


    public void setSelected(DataProjectAddressCombo group) {
        mCurProjectGroupId = group.id;
        PrefHelper.getInstance().setCurrentProjectGroup(group);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mProjectGroups.size();
    }

    void onDataChanged() {
        mProjectGroups = TableProjectAddressCombo.getInstance().query();
        mCurProjectGroupId = PrefHelper.getInstance().getCurrentProjectGroupId();
        notifyDataSetChanged();
    }
}
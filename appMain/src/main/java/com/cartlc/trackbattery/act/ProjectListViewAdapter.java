package com.cartlc.trackbattery.act;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cartlc.trackbattery.R;
import com.cartlc.trackbattery.data.TableEntries;
import com.cartlc.trackbattery.data.TableProjects;

import java.util.List;

/**
 * Created by dug on 5/10/17.
 */

public class ProjectListViewAdapter extends RecyclerView.Adapter<ProjectListViewAdapter.CustomViewHolder> {

    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView mProjectName;
        TextView mProjectNotes;

        public CustomViewHolder(View view) {
            super(view);
            mProjectName = (TextView) view.findViewById(R.id.project_name);
            mProjectNotes = (TextView) view.findViewById(R.id.project_notes);
        }
    }

    final Context mContext;
    List<Long> mProjects;

    public ProjectListViewAdapter(Context context) {
        mContext = context;
        onDataChanged();
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_item, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        long projectId = mProjects.get(position);
        String projectName = TableProjects.getInstance().query(projectId);
        int count = TableEntries.getInstance().count(projectId);
        holder.mProjectName.setText(projectName);
        if (count > 0) {
            holder.mProjectNotes.setText(Integer.toString(count));
        } else {
            holder.mProjectNotes.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return mProjects.size();
    }

    void onDataChanged() {
        mProjects = TableEntries.getInstance().queryProjects();
    }
}
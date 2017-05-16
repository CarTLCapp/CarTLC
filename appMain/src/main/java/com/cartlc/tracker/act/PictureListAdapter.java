package com.cartlc.tracker.act;

import java.util.ArrayList;
import java.util.List;

import com.cartlc.tracker.R;
import com.cartlc.tracker.data.TablePendingPictures;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by dug on 5/10/17.
 */

public class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.CustomViewHolder> {

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public CustomViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.picture);
            imageView.setAdjustViewBounds(true);
        }
    }

    final Context mContext;
    List<Uri> mItems = new ArrayList();

    public PictureListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_item_picture, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, final int position) {
        final Uri uri = mItems.get(position);
        Picasso.with(mContext).load(uri).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void onDataChanged() {
        mItems = TablePendingPictures.getInstance().queryPictures(mContext);
        notifyDataSetChanged();
    }
}
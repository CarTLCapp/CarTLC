package com.cartlc.tracker.act;

import java.util.ArrayList;
import java.util.List;

import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DataPicture;
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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dug on 5/10/17.
 */

public class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.CustomViewHolder> {

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.picture) ImageView imageView;
        @BindView(R.id.remove)  ImageView removeView;
        @BindView(R.id.loading) TextView  loading;

        public CustomViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            imageView.setAdjustViewBounds(true);
        }
    }

    final Context mContext;
    List<DataPicture> mItems = new ArrayList();

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
        final DataPicture item = mItems.get(position);
        Picasso.with(mContext).load(item.getUri(mContext)).into(holder.imageView);
        if (item.isRemoveOk()) {
            holder.removeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.remove();
                    mItems.remove(item);
                    notifyDataSetChanged();
                }
            });
            holder.removeView.setVisibility(View.VISIBLE);
            holder.loading.setVisibility(View.VISIBLE);
        } else {
            holder.removeView.setVisibility(View.GONE);
            holder.loading.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setList(List<DataPicture> list) {
        mItems = list;
        notifyDataSetChanged();
    }
}
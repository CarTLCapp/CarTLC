package com.cartlc.tracker.act;

import java.util.ArrayList;
import java.util.List;

import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DataPicture;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */

public class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.CustomViewHolder> {

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (mDecHeight == null) {
                mDecHeight = (int) mContext.getResources().getDimension(R.dimen.image_dec_size);
            }
            if (mMaxHeight != null) {
                mMaxHeight -= mDecHeight;
                if (mMaxHeight < mDecHeight) {
                    mMaxHeight = mDecHeight;
                }
                notifyDataSetChanged();
            }
        }
    }

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        protected @BindView(R.id.picture)          ImageView imageView;
        protected @Nullable @BindView(R.id.remove) ImageView removeView;
        protected @BindView(R.id.loading)          TextView  loading;

        public CustomViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    final static int MIN_HEIGHT = 100;
    final static int DEC_HEIGHT = 50;
    final protected Context mContext;
    protected List<DataPicture> mItems = new ArrayList();
    protected Integer mMaxHeight;
    protected Integer mDecHeight;
    protected MyHandler mHandler = new MyHandler();

    public PictureListAdapter(Context context) {
        mContext = context;
    }

    protected int getItemLayout() {
        return R.layout.entry_item_picture;
    }

    protected int getMaxDimension() {
        return R.dimen.image_full_max_height;
    }

    protected int getMaxHeight() {
        if (mMaxHeight == null) {
            mMaxHeight = (int) mContext.getResources().getDimension(getMaxDimension());
        }
        return mMaxHeight;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getItemLayout(), parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        final DataPicture item = mItems.get(position);
        Picasso.with(mContext).cancelRequest(holder.imageView);
        Picasso.Builder builder = new Picasso.Builder(mContext);
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                StringBuilder sbuf = new StringBuilder();
                sbuf.append("While processing: ");
                sbuf.append(uri.toString());
                sbuf.append("\n");
                sbuf.append(exception.getMessage());
                Timber.e(sbuf.toString());

                holder.loading.setText(R.string.error_while_loading_picture);

                mHandler.sendEmptyMessageDelayed(0, 100);
            }
        });
        if (!item.exists()) {
            item.remove();
            mItems.remove(item);
            notifyDataSetChanged();
        } else {
            builder.build()
                    .load(item.getUri(mContext))
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .resize(getMaxHeight(), getMaxHeight())
                    .centerInside()
                    .into(holder.imageView);

            if (holder.removeView != null) {
                holder.removeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.remove();
                        mItems.remove(item);
                        notifyDataSetChanged();
                    }
                });
                holder.loading.setVisibility(View.VISIBLE);
                holder.loading.setText(R.string.loading);
            } else {
                holder.loading.setVisibility(View.GONE);
            }
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
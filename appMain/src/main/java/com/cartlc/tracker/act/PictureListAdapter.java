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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */

public class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.CustomViewHolder> {

    static final int MSG_DECREASE_SIZE = 0;
    static final int MSG_REMOVE_ITEM   = 1;

    static final int DELAY_DECREASE_SIZE = 100;
    static final int DELAY_REMOVE_ITEM   = 100;

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DECREASE_SIZE:
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
                    break;
                case MSG_REMOVE_ITEM:
                    if (msg.obj instanceof DataPicture) {
                        DataPicture item = (DataPicture) msg.obj;
                        item.remove();
                        mItems.remove(item);
                        notifyDataSetChanged();
                    }
                    break;
            }
        }
    }

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        protected @BindView(R.id.picture)              ImageView imageView;
        protected @Nullable @BindView(R.id.remove)     ImageView removeView;
        protected @Nullable @BindView(R.id.rotate_cw)  ImageView rotateCWView;
        protected @Nullable @BindView(R.id.rotate_ccw) ImageView rotateCCWView;
        protected @BindView(R.id.loading) TextView loading;

        public CustomViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    final protected Context mContext;
    protected List<DataPicture> mItems = new ArrayList();
    protected Integer mDecHeight;
    protected MyHandler mHandler = new MyHandler();
    Integer mMaxHeight;

    public PictureListAdapter(Context context) {
        mContext = context;
    }

    protected int getItemLayout() {
        return R.layout.entry_item_picture;
    }

    protected int getMaxHeightResource() {
        return R.dimen.image_full_max_height;
    }

    protected int getMaxHeight() {
        if (mMaxHeight == null) {
            mMaxHeight = (int) mContext.getResources().getDimension(getMaxHeightResource());
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
                holder.imageView.setImageResource(android.R.color.transparent);

                mHandler.sendEmptyMessageDelayed(MSG_DECREASE_SIZE, DELAY_DECREASE_SIZE);
            }
        });
        if (!item.existsUnscaled()) {
            Message msg = new Message();
            msg.what = MSG_REMOVE_ITEM;
            msg.obj = item;
            mHandler.sendMessageDelayed(msg, DELAY_REMOVE_ITEM);
            holder.imageView.setImageResource(android.R.color.transparent);
            holder.loading.setText(R.string.error_picture_removed);
        } else {
            builder.build()
                    .load(item.getUnscaledUri(mContext))
                    .placeholder(R.drawable.loading)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .centerInside()
                    .resize(0, getMaxHeight())
                    .into(holder.imageView);
            holder.loading.setVisibility(View.GONE);

            if (holder.removeView != null) {
                holder.removeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.remove();
                        mItems.remove(item);
                        notifyDataSetChanged();
                    }
                });
            }
            if (holder.rotateCWView != null) {
                holder.rotateCWView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.rotateCW();
                        notifyDataSetChanged();
                    }
                });
            }
            if (holder.rotateCCWView != null) {
                holder.rotateCCWView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.rotateCCW();
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setList(List<DataPicture> list) {
        mItems = list;
        mMaxHeight = null;
        notifyDataSetChanged();
    }
}
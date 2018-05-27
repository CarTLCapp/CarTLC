/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.act;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.cartlc.tracker.R;
import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.data.DataPicture;
import com.cartlc.tracker.etc.PrefHelper;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

    interface RefreshCountListener {
        void refresh(int newCount);
    }

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        protected @BindView(R.id.picture)               ImageView imageView;
        protected @Nullable @BindView(R.id.remove)      ImageView removeView;
        protected @Nullable @BindView(R.id.rotate_cw)   ImageView rotateCWView;
        protected @Nullable @BindView(R.id.rotate_ccw)  ImageView rotateCCWView;
        protected @Nullable @BindView(R.id.note_dialog) ImageView noteDialogView;
        protected @BindView(R.id.note)                  TextView  noteView;
        protected @BindView(R.id.loading)               TextView  loading;

        public CustomViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    final protected Context              mContext;
    final protected LayoutInflater       mLayoutInflater;
    final protected RefreshCountListener mListener;
    HashMap<String, Integer> mRotation = new HashMap();
    protected List<DataPicture> mItems = new ArrayList();
    protected Integer mDecHeight;
    protected MyHandler mHandler = new MyHandler();
    Integer mMaxHeight;

    public PictureListAdapter(Context context, RefreshCountListener listener) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mListener = listener;
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
        View view = mLayoutInflater.inflate(getItemLayout(), parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        final DataPicture item = mItems.get(position);
        Picasso.get().cancelRequest(holder.imageView);
        Picasso.Builder builder = new Picasso.Builder(mContext);
        builder.listener((picasso, uri, exception) -> {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("While processing: ");
            sbuf.append(uri.toString());
            sbuf.append("\n");
            sbuf.append(exception.getMessage());
            Timber.e(sbuf.toString());

            holder.loading.setText(R.string.error_while_loading_picture);
            holder.imageView.setImageResource(android.R.color.transparent);

            mHandler.sendEmptyMessageDelayed(MSG_DECREASE_SIZE, DELAY_DECREASE_SIZE);
        });
        File pictureFile;
        if (item.existsUnscaled()) {
            pictureFile = item.getUnscaledFile();
        } else if (item.existsScaled()) {
            pictureFile = item.getScaledFile();
        } else {
            pictureFile = null;
        }
        if (pictureFile == null || !pictureFile.exists()) {
            Message msg = new Message();
            msg.what = MSG_REMOVE_ITEM;
            msg.obj = item;
            mHandler.sendMessageDelayed(msg, DELAY_REMOVE_ITEM);
            holder.imageView.setImageResource(android.R.color.transparent);
            holder.loading.setText(R.string.error_picture_removed);
        } else {
            builder.build()
                    .load(getUri(pictureFile))
                    .placeholder(R.drawable.loading)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .centerInside()
                    .resize(0, getMaxHeight())
                    .into(holder.imageView);
            holder.loading.setVisibility(View.GONE);

            if (holder.removeView != null) {
                holder.removeView.setOnClickListener(v -> {
                    item.remove();
                    mItems.remove(item);
                    notifyDataSetChanged();
                    if (mListener != null) {
                        mListener.refresh(mItems.size());
                    }
                });
            }
            if (holder.rotateCWView != null) {
                holder.rotateCWView.setOnClickListener(v -> {
                    incRotation(item, item.rotateCW());
                    notifyDataSetChanged();
                });
            }
            if (holder.rotateCCWView != null) {
                holder.rotateCCWView.setOnClickListener(v -> {
                    incRotation(item, item.rotateCCW());
                    notifyDataSetChanged();
                });
            }
            if (holder.noteDialogView != null) {
                holder.noteDialogView.setOnClickListener(v -> showPictureNoteDialog(item));
            }
            if (holder.noteView != null) {
                if (TextUtils.isEmpty(item.note)) {
                    holder.noteView.setVisibility(View.INVISIBLE);
                } else {
                    holder.noteView.setVisibility(View.VISIBLE);
                    holder.noteView.setText(item.note);
                }
            }
        }
    }

    public Uri getUri(File file) {
        return TBApplication.getUri(mContext, file);
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

    void showPictureNoteDialog(final DataPicture item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final View noteView = mLayoutInflater.inflate(R.layout.picture_note, null);
        builder.setView(noteView);

        final EditText edt = (EditText) noteView.findViewById(R.id.note);
        edt.setText(item.note);

        builder.setTitle(R.string.picture_note_title);
        builder.setPositiveButton("Done", (dialog, whichButton) -> {
            item.setNote(edt.getText().toString().trim());
            dialog.dismiss();
            notifyDataSetChanged();
        });
        builder.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss());
        AlertDialog b = builder.create();
        b.show();
    }

    void incRotation(DataPicture item, int degrees) {
        File file = item.getUnscaledFile();
        String path = file.getAbsolutePath();
        if (mRotation.containsKey(path)) {
            int value = (mRotation.get(path) + degrees) % 360;
            mRotation.put(path, value);
        } else {
            mRotation.put(path, degrees);
        }
    }

    public int getCommonRotation() {
        int commonRotation = 0;
        for (DataPicture picture : mItems) {
            String path = picture.getUnscaledFile().getAbsolutePath();
            if (!mRotation.containsKey(path)) {
                return 0;
            }
            int rotation = mRotation.get(path);
            if (commonRotation == 0) {
                commonRotation = rotation;
            } else if (commonRotation != rotation) {
                return 0;
            }
        }
        return commonRotation;
    }

    public boolean hadSomeRotations() {
        for (String key : mRotation.keySet()) {
            if (mRotation.get(key) != 0) {
                return true;
            }
        }
        return false;
    }
}
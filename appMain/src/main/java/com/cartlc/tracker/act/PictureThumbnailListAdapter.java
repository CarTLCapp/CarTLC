/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.act;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DataPicture;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dug on 5/10/17.
 */

public class PictureThumbnailListAdapter extends PictureListAdapter {

    public PictureThumbnailListAdapter(Context context) {
        super(context, null);
    }

    @Override
    protected int getItemLayout() {
        return R.layout.entry_item_picture_thumbnail;
    }

    @Override
    protected int getMaxHeightResource() {
        return R.dimen.image_thumbnail_max_height;
    }

}
package com.cartlc.tracker.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 * Created by dug on 5/16/17.
 */

public class DataPicture {

    public static Uri getUri(Context ctx, File file) {
        return FileProvider.getUriForFile(ctx, "com.cartcl.tracker.fileprovider", file);
    }

    public long id;
    public String pictureFilename;

    public DataPicture(long id, String filename) {
        this.id = id;
        pictureFilename = filename;
    }

    public DataPicture(String filename) {
        id = -1L;
        pictureFilename = filename;
    }

    public Uri getUri(Context ctx) {
        return getUri(ctx, new File(pictureFilename));
    }

    public void remove() {
        new File(pictureFilename).delete();
        if (id >= 0) {
            TablePendingPictures.getInstance().remove(id);
        }
    }

    public boolean isRemoveOk() {
        return id >= 0;
    }
}

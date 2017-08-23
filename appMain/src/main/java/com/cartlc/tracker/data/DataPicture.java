package com.cartlc.tracker.data;

import android.content.Context;
import android.net.Uri;

import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.util.BitmapHelper;

import java.io.File;

import timber.log.Timber;

/**
 * Created by dug on 5/16/17.
 */

public class DataPicture {

    public long    id;
    public String  unscaledFilename;
    public String  scaledFilename;
    public String  note;
    public boolean uploaded;

    File  unscaledFile;
    File  scaledFile;
    int[] imgSize;

    public DataPicture() {
    }

    public DataPicture(long id, String pictureFilename, String uploadingFilename, String note, boolean uploaded) {
        this.id = id;
        this.unscaledFilename = pictureFilename;
        this.scaledFilename = uploadingFilename;
        this.note = note;
        this.uploaded = uploaded;
    }

    public File getUnscaledFile() {
        if (unscaledFile == null) {
            unscaledFile = new File(unscaledFilename);
        }
        return unscaledFile;
    }

    public String getTailname() {
        int pos = unscaledFilename.lastIndexOf("/");
        if (pos >= 0) {
            return unscaledFilename.substring(pos + 1);
        }
        return unscaledFilename;
    }

    public boolean existsUnscaled() {
        return getUnscaledFile().exists();
    }

    public Uri getUnscaledUri(Context ctx) {
        if (!existsUnscaled()) {
            return null;
        }
        return TBApplication.getUri(ctx, getUnscaledFile());
    }

    public void remove() {
        getUnscaledFile().delete();
        if (getScaledFile() != null) {
            getScaledFile().delete();
        }
    }

    public File getScaledFile() {
        if (scaledFilename == null) {
            scaledFilename = BitmapHelper.createScaled(getUnscaledFile());
            if (scaledFilename != null) {
                TablePictureCollection.getInstance().update(this, null);
            } else {
                Timber.e("Failed to create uploading version of " + getUnscaledFile().getAbsolutePath());
            }
        }
        if (scaledFile == null && scaledFilename != null) {
            scaledFile = new File(scaledFilename);
        }
        return scaledFile;
    }

    public void setNote(String note) {
        this.note = note;
        TablePictureCollection.getInstance().update(this, null);
    }

    public void rotateCW() {
        BitmapHelper.rotate(getUnscaledFile(), 90);
    }

    public void rotateCCW() {
        BitmapHelper.rotate(getUnscaledFile(), -90);
    }

    public int[] getImageSize() {
        if (imgSize == null) {
            imgSize = BitmapHelper.getImageSize(getUnscaledFile().getAbsolutePath());
        }
        return imgSize;
    }
}

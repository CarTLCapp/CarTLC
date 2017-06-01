package com.cartlc.tracker.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.audiofx.EnvironmentalReverb;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.util.BitmapHelper;

import java.io.File;
import java.io.FileOutputStream;

import timber.log.Timber;

/**
 * Created by dug on 5/16/17.
 */

public class DataPicture {

    public long    id;
    public String  pictureFilename;
    public String  uploadingFilename;
    public boolean uploaded;

    File pictureFile;

    public DataPicture() {
    }

    public DataPicture(long id, String pictureFilename, String uploadingFilename, boolean uploaded) {
        this.id = id;
        this.pictureFilename = pictureFilename;
        this.uploadingFilename = uploadingFilename;
        this.uploaded = uploaded;
    }

    public File getPictureFile() {
        if (pictureFile == null) {
            pictureFile = new File(pictureFilename);
        }
        return pictureFile;
    }

    public String getTailname() {
        int pos = pictureFilename.lastIndexOf("/");
        if (pos >= 0) {
            return pictureFilename.substring(pos + 1);
        }
        return pictureFilename;
    }

    public boolean exists() {
        return getPictureFile().exists();
    }

    public Uri getUri(Context ctx) {
        if (!exists()) {
            return null;
        }
        return TBApplication.getUri(ctx, pictureFile);
    }

    public void remove() {
        getPictureFile().delete();
    }

    public String getUploadingFilename() {
        if (uploadingFilename == null) {
            uploadingFilename = BitmapHelper.createScaled(getPictureFile());
            if (uploadingFilename != null) {
                TablePictureCollection.getInstance().update(this, null);
            } else {
                Timber.e("Failed to create uploading version of " + getPictureFile().getAbsolutePath());
            }
        }
        return uploadingFilename;
    }
}

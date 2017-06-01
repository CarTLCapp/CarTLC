package com.cartlc.tracker.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.audiofx.EnvironmentalReverb;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import com.cartlc.tracker.app.TBApplication;

import java.io.File;
import java.io.FileOutputStream;

import timber.log.Timber;

/**
 * Created by dug on 5/16/17.
 */

public class DataPicture {

    static final String TMP_FILE_PATH = "cartlc";
    static final int    MAX_TMP_SIZE  = 500;

    class ScaledExternalFile {

        ScaledExternalFile() {
        }

        public boolean create() {
            try {
                String state = Environment.getExternalStorageState();
                if (!Environment.MEDIA_MOUNTED.equals(state)) {
                    Timber.e("No external media was mounted");
                    return false;
                }
                String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + TMP_FILE_PATH;
                File dirFile = new File(dir, TMP_FILE_PATH);
                dirFile.mkdir();

                File uploadingFile = new File(dirFile, genTmpName());

                uploadingFilename = uploadingFile.getAbsolutePath();

                Bitmap bitmap = loadScaledFile(pictureFilename, MAX_TMP_SIZE, MAX_TMP_SIZE);

                FileOutputStream fos = new FileOutputStream(uploadingFilename);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                fos.close();

                bitmap.recycle();
            } catch (Exception ex) {
                Timber.e(ex);
                return false;
            }
            return true;
        }

        String genTmpName() {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("p");
            sbuf.append(System.currentTimeMillis());
            return sbuf.toString();
        }

        Bitmap loadScaledFile(String pathname, int dstWidth, int dstHeight) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pathname, options);
            options.inJustDecodeBounds = false;
            options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, dstWidth, dstHeight);
            return BitmapFactory.decodeFile(pathname, options);
        }

        int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;
            if (srcAspect > dstAspect) {
                return srcWidth / dstWidth;
            } else {
                return srcHeight / dstHeight;
            }
        }
    }

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
            ScaledExternalFile scaled = new ScaledExternalFile();
            if (scaled.create()) {
                TablePictureCollection.getInstance().update(this, null);
            } else {
                uploadingFilename = null;
            }
        }
        return uploadingFilename;
    }
}

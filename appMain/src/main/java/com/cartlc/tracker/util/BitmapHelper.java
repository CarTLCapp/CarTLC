package com.cartlc.tracker.util;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import timber.log.Timber;

/**
 * Created by dug on 5/31/17.
 */

public class BitmapHelper {

    static final String FILE_PATH = "/cartlc/";

    static String genTmpName() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("p");
        sbuf.append(System.currentTimeMillis());
        return sbuf.toString();
    }

    public static Bitmap scaleFile(File path, int dstWidth, int dstHeight) {
        String pathname = path.getAbsolutePath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathname, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, dstWidth, dstHeight);
        return BitmapFactory.decodeFile(pathname, options);
    }

    static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        final float srcAspect = (float) srcWidth / (float) srcHeight;
        final float dstAspect = (float) dstWidth / (float) dstHeight;
        if (srcAspect > dstAspect) {
            return srcWidth / dstWidth;
        } else {
            return srcHeight / dstHeight;
        }
    }

    public static boolean saveToExternal(Bitmap bitmap, File file)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            fos.close();
            return true;
        }
        catch (Exception ex)
        {
           Timber.e("ProcessPicture, while saving result", ex);
        }
        return false;
    }

    public static File scaleIntoTempFile(File originalPictureFile)
    {
        try
        {
            String dir = Environment.getExternalStorageDirectory() + FILE_PATH;
            File dirFile = new File(dir);
            dirFile.mkdir();

            String externalFilename = dir + genTmpName();

            MediaFile file = new MediaFile(externalFilename, false, mIsCustom, true);

            if (mIsAsset)
            {
                AssetFileDescriptor afd = app.getApp().getAssets().openFd(mFilename);
                FileInputStream fs = afd.createInputStream();
                FileSupport.copyFile(fs, externalFilename);
                fs.close();
                afd.close();
            }
            else
            {
                FileInputStream fs = app.getApp().openFileInput(mFilename);
                FileSupport.copyFile(fs, externalFilename);
                fs.close();
            }
            return file;
        }
        catch (Exception ex)
        {
            GCCore.error(ex.getMessage());
        }
        return null;
    }

}

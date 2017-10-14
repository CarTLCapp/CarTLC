package com.cartlc.tracker.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;

import com.cartlc.tracker.app.TBApplication;

import java.io.File;
import java.io.FileOutputStream;

import timber.log.Timber;

/**
 * Created by dug on 6/1/17.
 */

public class BitmapHelper {

    static final String TMP_FILE_PATH = "cartlc";
    static final int    MAX_TMP_SIZE  = 500;

    public static String createScaled(File original) {
        String scaledFilename;
        try {
            String state = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                Timber.e("No external media was mounted");
                return null;
            }
            if (!original.exists()) {
                Timber.e("File does not exist: " + original.getAbsolutePath());
                return null;
            }
            String originalFilename = original.getAbsolutePath();
            int pos = originalFilename.lastIndexOf('.');
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(originalFilename.substring(0, pos));
            sbuf.append("_");
            sbuf.append(".jpg");

            scaledFilename = sbuf.toString();

            Bitmap bitmap = loadScaledFile(original.getAbsolutePath(), MAX_TMP_SIZE, MAX_TMP_SIZE);

            FileOutputStream fos = new FileOutputStream(scaledFilename);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            fos.close();

            bitmap.recycle();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, BitmapHelper.class, "createScaled()", original.toString());
            return null;
        }
        return scaledFilename;
    }

    static Bitmap loadScaledFile(String pathname, int dstWidth, int dstHeight) {
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

    public static void rotate(File picture, int degrees) {
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            Bitmap bitmap = BitmapFactory.decodeFile(picture.getAbsolutePath());
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            FileOutputStream fos = new FileOutputStream(picture.getAbsoluteFile());
            rotated.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, BitmapHelper.class, "rotate()", picture.toString());
        }
    }

    public static int [] getImageSize(String pathname) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathname, options);
        int [] result = new int[2];
        result[0] = options.outWidth;
        result[1] = options.outHeight;
        return result;
    }
}

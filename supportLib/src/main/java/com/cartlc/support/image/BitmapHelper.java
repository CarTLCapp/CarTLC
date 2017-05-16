package com.cartlc.support.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import timber.log.Timber;

import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by dug on 4/14/17.
 */
public class BitmapHelper {

    /**
     * Load the bitmap from the assets directory.
     *
     * @param filename : asset files
     */
    public static Bitmap loadBitmapFromAsset(Context context, final String filename) {
        Bitmap result = null;
        try {
            InputStream is = context.getAssets().open(filename);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            result = BitmapFactory.decodeStream(is, null, opt);
            is.close();
        } catch (Exception ex) {
            Timber.e(ex.getMessage());
        }
        return result;
    }

    /**
     * Load the bitmap from the assets directory.
     *
     * @param filename : asset files
     */
    public static Bitmap loadBitmapFromExternal(Context context, final String filename) {
        Bitmap result = null;
        try {
            InputStream is = context.getAssets().open(filename);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            result = BitmapFactory.decodeStream(is, null, opt);
            is.close();
        } catch (Exception ex) {
            Timber.e(ex.getMessage());
        }
        return result;
    }

    /**
     * Save bitmap to internal storage.
     *
     * @param ctx
     * @param bitmap
     * @param filename
     * @return
     */
    public static boolean saveBitmapToInternal(Context ctx, Bitmap bitmap, String filename) {
        try {
            FileOutputStream fos2 = ctx.openFileOutput(filename, 0);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos2);
            fos2.close();
            return true;
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return false;
    }
}

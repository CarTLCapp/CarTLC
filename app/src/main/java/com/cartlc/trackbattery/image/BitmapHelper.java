package com.cartlc.trackbattery.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import timber.log.Timber;

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
}

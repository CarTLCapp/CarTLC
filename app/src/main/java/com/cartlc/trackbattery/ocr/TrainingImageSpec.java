// TrainingImageSpec.java
// Copyright (c) 2010 William Whitney
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.
package com.cartlc.trackbattery.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import com.cartlc.trackbattery.image.BitmapHelper;
import timber.log.Timber;

/**
 * A data structure to hold the file name and character range of training data.
 *
 * @author William Whitney
 */
public class TrainingImageSpec {
    private String assetFilename;
    private CharacterRange charRange;
    private Bitmap bitmap;

    public TrainingImageSpec(String assetFilename, CharacterRange range) {
        this.assetFilename = assetFilename;
        this.charRange = range;
    }

    /**
     * @return the charRange
     */
    public CharacterRange getCharRange() {
        return charRange;
    }

    /**
     * @return the fileLocation
     */
    public Bitmap getImage(Context ctx) {
        if (bitmap == null) {
            bitmap = BitmapHelper.loadBitmapFromAsset(ctx, assetFilename);
            if (bitmap == null) {
                Timber.e("Could not load: " + assetFilename);
            }
        }
        return bitmap;
    }

    public String getAssetFilename() {
        return assetFilename;
    }
}

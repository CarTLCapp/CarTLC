// TrainingImageLoader.java
// Copyright (c) 2003-2010 Ronald B. Cemer
// Modified by William Whitney
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.
package com.cartlc.ocrlib;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import net.sourceforge.javaocr.scanner.DocumentScanner;
import net.sourceforge.javaocr.scanner.DocumentScannerListenerAdaptor;
import net.sourceforge.javaocr.scanner.PixelImage;
import timber.log.Timber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Utility class to load an image file, break it into individual characters,
 * and create and store multiple TrainingImage objects from the characters.
 * The source image must contain a range of characters specified in the
 * character range passed into the load() method.
 * @author Ronald B. Cemer
 */
public class TrainingImageLoader extends DocumentScannerListenerAdaptor
{
    static final boolean DEBUG = false;

    int charValue = 0;
    HashMap<Character, ArrayList<TrainingImage>> dest;
    DocumentScanner documentScanner = new DocumentScanner();
    /**
     * Load an image containing training characters, break it up into
     * characters, and build a training set.
     * Each entry in the training set (a <code>Map</code>) has a key which
     * is a <code>Character</code> object whose value is the character code.
     * Each corresponding value in the <code>Map</code> is an
     * <code>ArrayList</code> of one or more <code>TrainingImage</code>
     * objects which contain images of the character represented in the key.
     * @param imageFilename The filename of the image to load.
     * @param charRange A <code>CharacterRange</code> object representing the
     * range of characters which is contained in this image.
     * @param dest A <code>Map</code> which gets loaded with the training
     * data.  Multiple calls to this method may be made with the same
     * <code>Map</code> to populate it with the data from several training
     * images.
     * @throws IOException
     */
    public void load(
            @NonNull Bitmap image,
            CharacterRange charRange,
            HashMap<Character, ArrayList<TrainingImage>> dest,
            String imageFilename)
            throws IOException
    {
        PixelImage pixelImage = new PixelImage(image);
        pixelImage.toGrayScale(true);
        pixelImage.filter();
        charValue = charRange.min;
        this.dest = dest;
        documentScanner.scan(pixelImage, this, 0, 0, 0, 0);
        if (charValue != (charRange.max + 1))
        {
            throw new IOException(
                    "Expected to decode " + ((charRange.max + 1) - charRange.min)
                    + " characters but actually decoded " + (charValue - charRange.min)
                    + " characters in training: " + imageFilename);
        }
    }

    @Override
    public void processChar(PixelImage pixelImage, int x1, int y1, int x2, int y2, int rowY1, int rowY2)
    {
        if (DEBUG)
        {
            Timber.e(
                    "TrainingImageLoader.processChar: \'"
                    + (char) charValue + "\' " + x1 + "," + y1 + "-" + x2 + "," + y2);
        }
        int w = x2 - x1;
        int h = y2 - y1;
        int[] pixels = new int[w * h];
        for (int y = y1, destY = 0; y < y2; y++, destY++)
        {
            System.arraycopy(pixelImage.pixels, (y * pixelImage.width) + x1, pixels, destY * w, w);
        }
        if (DEBUG)
        {
            for (int y = 0, idx = 0; y < h; y++)
            {
                for (int x = 0; x < w; x++, idx++)
                {
                    System.out.print((pixels[idx] > 0) ? ' ' : '*');
                }
                System.out.println();
            }
            System.out.println();
        }
        Character chr = new Character((char) charValue);
        ArrayList<TrainingImage> al = dest.get(chr);
        if (al == null)
        {
            al = new ArrayList<>();
            dest.put(chr, al);
        }
        al.add(new TrainingImage(pixels, w, h, y1 - rowY1, rowY2 - y2));
        charValue++;
    }
}

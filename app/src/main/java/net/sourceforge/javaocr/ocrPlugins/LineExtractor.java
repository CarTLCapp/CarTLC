// LineExtractor.java
// Copyright (c) 2010 William Whitney
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.
package net.sourceforge.javaocr.ocrPlugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import net.sourceforge.javaocr.scanner.DocumentScanner;
import net.sourceforge.javaocr.scanner.DocumentScannerListenerAdaptor;
import net.sourceforge.javaocr.scanner.PixelImage;
import timber.log.Timber;

/**
 * Saves all the characters in an image to an output directory individually.
 * @author William Whitney
 */
public class LineExtractor extends DocumentScannerListenerAdaptor
{

    private int num = 0;
    private DocumentScanner documentScanner = new DocumentScanner();
    private File outputDir = null;
    private File inputImage = null;

    public void slice(File inputImage, File outputDir)
    {
        try
        {
            this.inputImage = inputImage;
            this.outputDir = outputDir;
            Bitmap img = BitmapFactory.decodeFile(inputImage.getPath());
            PixelImage pixelImage = new PixelImage(img);
            pixelImage.toGrayScale(true);
            pixelImage.filter();
            documentScanner.scan(pixelImage, this, 0, 0, pixelImage.width, pixelImage.height);
        }
        catch (Exception ex)
        {
            Timber.e(ex);
        }
    }

    @Override
    public void beginRow(PixelImage pixelImage, int y1, int y2)
    {
        try
        {
            int areaH = y2 - y1;
            Bitmap img = BitmapFactory.decodeFile(inputImage.getPath());
            int areaW = img.getWidth();
            Bitmap img2 = img.createBitmap(img, 0, y1, areaW, areaH);
            img.recycle();
            File outputfile = new File(outputDir + File.separator + "line_" + num + ".png");
            FileOutputStream out = new FileOutputStream(outputfile);
            img2.compress(Bitmap.CompressFormat.PNG, 100, out);
            num++;
        }
        catch (Exception ex)
        {
            Timber.e(ex);
        }
    }
}

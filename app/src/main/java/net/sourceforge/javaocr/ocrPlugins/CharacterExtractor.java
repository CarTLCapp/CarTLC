// CharacterExtractor.java
// Copyright (c) 2010 William Whitney
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.
package net.sourceforge.javaocr.ocrPlugins;


import java.awt.*;
import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import net.sourceforge.javaocr.scanner.DocumentScanner;
import net.sourceforge.javaocr.scanner.DocumentScannerListenerAdaptor;
import net.sourceforge.javaocr.scanner.PixelImage;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import timber.log.Timber;

/**
 * Saves all the characters in an image to an output directory individually. 
 * @author William Whitney
 */
public class CharacterExtractor extends DocumentScannerListenerAdaptor
{

    private int num = 0;
    private DocumentScanner documentScanner = new DocumentScanner();
    private File outputDir = null;
    private File inputImage = null;
    private int std_width;
    private int std_height;

    public void slice(File inputImage, File outputDir, int std_width, int std_height)
    {
        try
        {
            this.std_width = std_width;
            this.std_height = std_height;
            this.inputImage = inputImage;
            this.outputDir = outputDir;
            Bitmap bitmap = BitmapFactory.decodeFile(inputImage.getPath());
            PixelImage pixelImage = new PixelImage(bitmap);
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
    public void processChar(PixelImage pixelImage, int x1, int y1, int x2, int y2, int rowY1, int rowY2)
    {
        try
        {
            int areaW = x2 - x1;
            int areaH = y2 - y1;

            //Extract the character
            Bitmap characterImageFull = BitmapFactory.decodeFile(inputImage.getPath());
            Bitmap characterImage = Bitmap.createBitmap(characterImageFull, x1, y1, areaW, areaH);
            characterImageFull.recycle();

            //Scale image so that both the height and width are less than std size
            if (characterImage.getWidth() > std_width)
            {
                //Make image always std_width wide
                double scaleAmount = (double) std_width / (double) characterImage.getWidth();
                AffineTransform tx = new AffineTransform();
                tx.scale(scaleAmount, scaleAmount);
                AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
                characterImage = op.filter(characterImage, null);
            }

            if (characterImage.getHeight() > std_height)
            {
                //Make image always std_height tall
                double scaleAmount = (double) std_height / (double) characterImage.getHeight();
                AffineTransform tx = new AffineTransform();
                tx.scale(scaleAmount, scaleAmount);
                AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
                characterImage = op.filter(characterImage, null);
            }

            //Paint the scaled image on a white background
            Bitmap normalizedImage = Bitmap.createBitmap(std_width, std_height, Bitmap.Config.ARGB_8888);
            Graphics2D g = normalizedImage.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, std_width, std_height);

            //Center scaled image on new canvas
            int x_offset = (std_width - characterImage.getWidth()) / 2;
            int y_offset = (std_height - characterImage.getHeight()) / 2;

            g.drawImage(characterImage, x_offset, y_offset, null);
            g.dispose();

            //Save new image to file
            File outputfile = new File(outputDir + File.separator + "char_" + num + ".png");
            ImageIO.write(normalizedImage, "png", outputfile);
            num++;
        }
        catch (Exception ex)
        {
            Timber.e(ex);
        }
    }
}

package com.cartlc.trackbattery.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import com.cartlc.trackbattery.image.BitmapHelper;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dug on 4/14/17.
 */

public class OCRHelper {

    public static final String TRAINING_DIGITS = "images/training/digits.jpg";
    public static final String SAMPLE_DIGITS = "images/samples/shuffledDigits.jpg";

    public static void Init() {
        sInstance = new OCRHelper();
    }

    public static OCRHelper getInstance() {
        if (sInstance == null) {
            Init();
        }
        return sInstance;
    }

    static OCRHelper sInstance;

    ArrayList<TrainingImageSpec> trainingImages = new ArrayList<>();

    public OCRHelper() {
    }

    void init() {
        if (trainingImages.size() == 0) {
            trainingImages.add(new TrainingImageSpec(TRAINING_DIGITS, new CharacterRange(Character.forDigit(0, 10), Character.forDigit(9, 10))));
        }
    }

    public static void doOCRTest(Context ctx) {
        String result = getInstance().performOCR(ctx, SAMPLE_DIGITS);
        Timber.i("RESULT=" + result);
    }

    public String performOCR(Context ctx, String targImageLoc) {
        try {
            init();
            OCRScanner ocrScanner = new OCRScanner();
            HashMap<Character, ArrayList<TrainingImage>> trainingImages = getTrainingImageHashMap(ctx, this.trainingImages);
            ocrScanner.addTrainingImages(trainingImages);
            Bitmap targetImage = BitmapHelper.loadBitmapFromAsset(ctx, targImageLoc);
            return ocrScanner.scan(targetImage, 0, 0, 0, 0, null);
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return null;
    }

    HashMap<Character, ArrayList<TrainingImage>> getTrainingImageHashMap(Context ctx, ArrayList<TrainingImageSpec> imgs) throws Exception {
        TrainingImageLoader loader = new TrainingImageLoader();
        HashMap<Character, ArrayList<TrainingImage>> trainingImages = new HashMap<Character, ArrayList<TrainingImage>>();
        for (int i = 0; i < imgs.size(); i++) {
            loader.load(
                    imgs.get(i).getImage(ctx),
                    imgs.get(i).getCharRange(),
                    trainingImages,
                    imgs.get(i).getAssetFilename());
        }
        return trainingImages;
    }
}

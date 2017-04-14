package com.cartlc.trackbattery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.OCRScanner;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.TrainingImage;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.TrainingImageLoader;
import timber.log.Timber;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String performMSEOCR(ArrayList<TrainingImageSpec> imgs, String targImageLoc) throws Exception {
        try {
            OCRScanner ocrScanner = new OCRScanner();
            HashMap<Character, ArrayList<TrainingImage>> trainingImages = getTrainingImageHashMap(imgs);
            ocrScanner.addTrainingImages(trainingImages);
            Bitmap targetImage = BitmapFactory.decodeFile(targImageLoc);
            return ocrScanner.scan(targetImage, 0, 0, 0, 0, null);
        } catch (Exception ex) {
            Timber.e(ex);
            return null;
        }
    }

    private HashMap<Character, ArrayList<TrainingImage>> getTrainingImageHashMap(ArrayList<TrainingImageSpec> imgs) throws Exception
    {
        TrainingImageLoader loader = new TrainingImageLoader();
        HashMap<Character, ArrayList<TrainingImage>> trainingImages = new HashMap<Character, ArrayList<TrainingImage>>();
        Frame frame = new Frame();

        for (int i = 0; i < imgs.size(); i++)
        {
            loader.load(
                    frame,
                    imgs.get(i).getFileLocation(),
                    imgs.get(i).getCharRange(),
                    trainingImages);
        }

        return trainingImages;
    }
}

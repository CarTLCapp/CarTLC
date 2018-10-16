package com.cartlc.ocrtest.act;


import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import com.cartlc.ocrtest.BuildConfig;
import com.cartlc.ocrtest.R;
import com.cartlc.ocrtest.app.TBApplication;
import com.cartlc.ocrlib.OCRHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    TBApplication mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (TBApplication) getApplicationContext();
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (!BuildConfig.DEBUG) {
            MenuItem item = menu.findItem(R.id.action_test_ocr);
            item.setVisible(false);
            item = menu.findItem(R.id.action_show_test_images);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_show_test_images) {
            mApp.displayImageActivity(OCRHelper.TRAINING_DIGITS, OCRHelper.SAMPLE_DIGITS);
            return true;
        }
        if (id == R.id.action_test_ocr) {
            OCRHelper.doOCRTest(this);
        }
        return super.onOptionsItemSelected(item);
    }
}

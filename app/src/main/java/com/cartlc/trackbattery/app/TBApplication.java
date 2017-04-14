package com.cartlc.trackbattery.app;


import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import com.cartlc.trackbattery.act.DisplayImageActivity;
import timber.log.Timber;

/**
 * Created by dug on 4/14/17.
 */

public class TBApplication extends Application {
    public TBApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
    }

    public void displayImageActivity(String filename1, String filename2)
    {
        Intent intent = new Intent(this, DisplayImageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(DisplayImageActivity.EXTRA_ASSET_FILENAME1, filename1);
        intent.putExtra(DisplayImageActivity.EXTRA_ASSET_FILENAME2, filename2);
        startActivity(intent);
    }
}

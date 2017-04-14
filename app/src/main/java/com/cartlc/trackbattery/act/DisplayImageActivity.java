package com.cartlc.trackbattery.act;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.cartlc.trackbattery.R;
import com.cartlc.trackbattery.image.BitmapHelper;
import timber.log.Timber;

public class DisplayImageActivity extends AppCompatActivity {

    public static final String EXTRA_ASSET_FILENAME1 = "asset_filename1";
    public static final String EXTRA_ASSET_FILENAME2 = "asset_filename2";

    @BindView(R.id.image1) ImageView mImage1;
    @BindView(R.id.image2) ImageView mImage2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);
        ButterKnife.bind(this);
        handleIntent(getIntent());
    }

    void handleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String filename = extras.getString(EXTRA_ASSET_FILENAME1);
            if (filename != null) {
                Bitmap bitmap = BitmapHelper.loadBitmapFromAsset(this, filename);
                if (bitmap != null) {
                    mImage1.setImageBitmap(bitmap);
                } else {
                    Timber.e("Could not find asset file: " + filename);
                }
            }
            filename = extras.getString(EXTRA_ASSET_FILENAME2);
            if (filename != null) {
                Bitmap bitmap = BitmapHelper.loadBitmapFromAsset(this, filename);
                if (bitmap != null) {
                    mImage2.setImageBitmap(bitmap);
                } else {
                    Timber.e("Could not find asset file: " + filename);
                }
            }
        }
    }
}

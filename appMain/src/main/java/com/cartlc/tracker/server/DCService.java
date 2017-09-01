package com.cartlc.tracker.server;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.cartlc.tracker.etc.PrefHelper;

import timber.log.Timber;

/**
 * Created by dug on 5/22/17.
 */
public class DCService extends IntentService {

    public static final String ACTION_ZIP_CODE        = "zipcode";
    public static final String DATA_ZIP_CODE          = "zipcode";

    static final String SERVER_NAME            = "CarTLC.DataCollectionService";

    DCPing mPing;
    DCZip mZip;

    public DCService() {
        super(SERVER_NAME);
        mPing = new DCPing();
        mZip = new DCZip();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ServerHelper.Init(this);
        if (!ServerHelper.getInstance().hasConnection()) {
            Timber.i("No connection -- service aborted");
            return;
        }
        String action = intent.getAction();
        if (ACTION_ZIP_CODE.equals(action)) {
            String zipCode = intent.getStringExtra(DATA_ZIP_CODE);
            mZip.findZipCode(zipCode);
        } else {
            if (PrefHelper.getInstance().getTechID() == 0 || PrefHelper.getInstance().hasRegistrationChanged()) {
                if (PrefHelper.getInstance().hasName()) {
                    mPing.sendRegistration();
                }
            }
            mPing.ping();
        }
    }

}

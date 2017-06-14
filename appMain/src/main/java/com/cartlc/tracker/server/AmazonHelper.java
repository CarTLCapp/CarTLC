package com.cartlc.tracker.server;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataPicture;
import com.cartlc.tracker.data.PrefHelper;
import com.cartlc.tracker.data.TablePictureCollection;

import java.io.File;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/31/17.
 */

public class AmazonHelper {

    static AmazonHelper sInstance;

    public static AmazonHelper getInstance() {
        return sInstance;
    }

    static public void Init(Context ctx) {
        if (sInstance == null) {
            new AmazonHelper(ctx.getApplicationContext());
        }
    }

    final static String BUCKET_NAME_DEVELOP      = "cartlc";
    final static String BUCKET_NAME_RELEASE      = "fleettlc";
    final static String IDENTITY_POOL_ID_DEVELOP = "us-east-2:38d2f2a2-9454-4472-9fec-9468f3700ba5";
    final static String IDENTITY_POOL_ID_RELEASE = "us-east-2:389282dd-de71-4849-a68b-2b126b3de5f3";

    final Context mCtx;
    final String  BUCKET_NAME;
    final String  IDENTITY_POOL_ID;
    CognitoCachingCredentialsProvider mCred;
    AmazonS3                          mClient;
    TransferUtility                   mTrans;

    public AmazonHelper(Context ctx) {
        sInstance = this;
        mCtx = ctx;

        if (PrefHelper.getInstance().isDevelopment()) {
            BUCKET_NAME = BUCKET_NAME_DEVELOP;
            IDENTITY_POOL_ID = IDENTITY_POOL_ID_DEVELOP;
        } else {
            BUCKET_NAME = BUCKET_NAME_RELEASE;
            IDENTITY_POOL_ID = IDENTITY_POOL_ID_RELEASE;
        }
    }

    void init() {
        if (mCred == null) {
            mCred = new CognitoCachingCredentialsProvider(
                    mCtx,
                    IDENTITY_POOL_ID,
                    Regions.US_EAST_2
            );
            mClient = new AmazonS3Client(mCred);
            mTrans = new TransferUtility(mClient, mCtx);
        }
    }

    public void sendPictures(List<DataEntry> list) {
        for (DataEntry entry : list) {
            sendPictures(entry);
        }
    }

    void sendPictures(DataEntry entry) {
        for (DataPicture item : entry.pictureCollection.pictures) {
            if (!item.uploaded) {
                sendPicture(entry, item);
            }
        }
    }

    void sendPicture(final DataEntry entry, final DataPicture item) {
        File uploadingFile = item.getScaledFile();
        if (uploadingFile == null) {
            return;
        }
        init();

        String key = item.getUnscaledFile().getName();

        TransferObserver observer = mTrans.upload(
                BUCKET_NAME,        /* The bucket to upload to */
                key,                /* The key for the uploaded object */
                uploadingFile       /* The file where the data to upload existsUnscaled */
        );
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    uploadComplete(entry, item);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            }

            @Override
            public void onError(int id, Exception ex) {
                Timber.e(ex);
            }
        });
    }

    synchronized void uploadComplete(DataEntry entry, DataPicture item) {
        TablePictureCollection.getInstance().setUploaded(item);
        entry.checkPictureUploadComplete();
    }

}

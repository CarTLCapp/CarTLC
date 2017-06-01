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
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataPicture;
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

    final static String BUCKET_NAME = "cartlc";

    CognitoCachingCredentialsProvider mCred;
    AmazonS3                          mClient;
    TransferUtility                   mTrans;
    final Context mCtx;

    public AmazonHelper(Context ctx) {
        sInstance = this;
        mCtx = ctx;
    }

    void init() {
        if (mCred == null) {
            mCred = new CognitoCachingCredentialsProvider(
                    mCtx,
                    "us-east-2:38d2f2a2-9454-4472-9fec-9468f3700ba5", // Identity Pool ID
                    Regions.US_EAST_2 // Region
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
        String uploadingFilename = item.getUploadingFilename();
        if (uploadingFilename == null) {
            return;
        }
        File uploadingFile = new File(uploadingFilename);
        String key = item.getPictureFile().getName();

        init();
        TransferObserver observer = mTrans.upload(
                BUCKET_NAME,        /* The bucket to upload to */
                key,                /* The key for the uploaded object */
                uploadingFile       /* The file where the data to upload exists */
        );
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Timber.d("onStateChanged(" + state.toString() + ")");
                if (state == TransferState.COMPLETED) {
                    uploadComplete(entry, item);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//                int percentage = (int) (bytesCurrent/bytesTotal * 100);
            }

            @Override
            public void onError(int id, Exception ex) {
                Timber.e(ex);
            }
        });
    }

    synchronized void uploadComplete(DataEntry entry, DataPicture item) {
        item.uploaded = true;
        TablePictureCollection.getInstance().update(item, null);
        entry.checkPictureUploadComplete();
    }

}

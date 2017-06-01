package com.cartlc.tracker.server;

import android.graphics.Bitmap;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataPicture;
import com.cartlc.tracker.data.TableEntry;
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

    static public void Init() {
        if (sInstance == null) {
            new AmazonHelper();
        }
    }

    final static String  KEY_AAK     = "AKIAJF5DCYNIXY36SB6Q";
    final static String  KEY_ASK     = "SHf467aH25t6Q1eAPg0iqfabVrUMA6gqBTrN78rD";
    final static String  BUCKET_NAME = "cartlc";
    static final Regions REGION      = Regions.US_EAST_1;

    BasicAWSCredentials mCred;
    AmazonS3            mClient;

    public AmazonHelper() {
        sInstance = this;
        mCred = new BasicAWSCredentials(KEY_AAK, KEY_ASK);
        mClient = new AmazonS3Client(mCred);
        mClient.setRegion(Region.getRegion(REGION));
    }

    boolean pushFile(String key, File file) {
        try {
            Timber.i("SENDING " + file.toString() + " as " + file.getName());
            PutObjectRequest por = new PutObjectRequest(BUCKET_NAME, key, file);
            mClient.putObject(por);
            return true;
        } catch (AmazonServiceException ase) {
            Timber.e("Caught an AmazonServiceException, which means your request made it to Amazon S3, but was rejected with an error response for some reason.");
            Timber.e("Error Message:    " + ase.getMessage());
            Timber.e("HTTP Status Code: " + ase.getStatusCode());
            Timber.e("AWS Error Code:   " + ase.getErrorCode());
            Timber.e("Error Type:       " + ase.getErrorType());
            Timber.e("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered an internal error while trying to communicate with S3,  such as not being able to access the network.");
            Timber.e("Error Message: " + ace.getMessage());
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return false;
    }

    public int sendPictures(List<DataEntry> list) {
        int count = 0;
        for (DataEntry entry : list) {
            count += sendPictures(entry);
        }
        return count;
    }

    int sendPictures(DataEntry entry) {
        int count = 0;
        for (DataPicture item : entry.pictureCollection.pictures) {
            if (item.uploaded) {
                count++;
            } else if (sendPicture(item)) {
                item.uploaded = true;
                TablePictureCollection.getInstance().update(item, null);
                count++;
            }
        }
        if (count == entry.pictureCollection.pictures.size()) {
            TableEntry.getInstance().setUploadedAws(entry, true);
        }
        return count;
    }

    boolean sendPicture(DataPicture item) {
        String uploadingFilename = item.getUploadingFilename();
        File uploadingFile = new File(uploadingFilename);
        String key = item.getPictureFile().getName();
        return pushFile(key, uploadingFile);
    }

}

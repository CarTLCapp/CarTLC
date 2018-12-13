/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package modules;

import javax.inject.*;

import java.io.File;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import play.Logger;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Transfer.TransferState;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.internal.S3SyncProgressListener;
import com.amazonaws.services.s3.transfer.PersistableTransfer;
import com.amazonaws.services.s3.model.GetObjectRequest;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.typesafe.config.Config;

@Singleton
public class AmazonHelper {

    static final String BUCKET_NAME_DEVELOP = "fleetdev2";
    static final String BUCKET_NAME_RELEASE = "fleettlc";
    static final String REGION = "us-east-2";

    public interface OnDownloadComplete {
        void onDownloadComplete();
    }

    class CommonActivity {

        final String bucketName;
        final String host;

        CommonActivity(String host) {
            this.host = host;
            if (host.startsWith("fleettlc")) {
                bucketName = BUCKET_NAME_RELEASE;
            } else {
                bucketName = BUCKET_NAME_DEVELOP;
            }
        }
    }

    final private Config configuration;
    final private ExecutorService executor;

    @Inject
    public AmazonHelper(Config configuration) {
        this.configuration = configuration;
        executor = Executors.newSingleThreadExecutor();
    }

    public File getLocalFile(String filename) {
        String downloadDir = configuration.getString("pictureDownloadDir");
        return new File(downloadDir, filename);
    }

    public File getLocalDirectory() {
        return new File(configuration.getString("pictureDownloadDir"));
    }

    // --------------
    // DOWNLOAD FILE
    // --------------

    class DownloadActivity extends CommonActivity implements Runnable {
        final List<File> files;
        final OnDownloadComplete listener;
        Download curDownload;
        int curPos;

        DownloadActivity(String host, List<File> files, OnDownloadComplete listener) {
            super(host);
            this.files = files;
            this.listener = listener;
        }

        public void run() {
            scheduleNext();
        }

        private void scheduleNext() {
            if (curPos >= files.size()) {
                listener.onDownloadComplete();
            } else {
                next(files.get(curPos++));
            }
        }

        private void next(File targetFile) {
            Logger.warn("DOWNLOAD: BUCKET=" + bucketName + ", KEY=" + targetFile.getName() + " TARGET=" + targetFile.getAbsolutePath());
            try {
                TransferManager xferManager = TransferManagerBuilder.standard().build();
                Download download = xferManager.download(bucketName, targetFile.getName(), targetFile);
                download.waitForCompletion();
                Logger.info("COMPLETED: " + targetFile.getAbsolutePath());
            } catch (AmazonServiceException e) {
                Logger.error("Amazon service error: " + e.getMessage());
            } catch (AmazonClientException e) {
                Logger.error("Amazon client error: " + e.getMessage());
            } catch (InterruptedException e) {
                Logger.error("Transfer interrupted: " + e.getMessage());
            }
            scheduleNext();
        }
    }

    public void download(String host, List<File> files, OnDownloadComplete listener) {
        executor.execute(new DownloadActivity(host, files, listener));
    }

    public void download(String host, String filename, OnDownloadComplete listener) {
        ArrayList<File> files = new ArrayList<File>();
        files.add(getLocalFile(filename));
        download(host, files, listener);
    }

    // ------------
    // LIST FILES
    // -----------

    public interface OnListComplete {
        void onListComplete(ArrayList<String> keys);
    }

    class ListActivity extends CommonActivity implements Runnable {

        final OnListComplete listener;

        ListActivity(String host, OnListComplete listener) {
            super(host);
            this.listener = listener;
        }

        public void run() {
            Logger.warn("LIST: BUCKET=" + bucketName);
            try {
                AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                        .withCredentials(new ProfileCredentialsProvider())
                        .withRegion(REGION)
                        .build();

                ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(2000);
                ListObjectsV2Result result;

                ArrayList<String> files = new ArrayList<>();
                do {
                    result = s3Client.listObjectsV2(req);

                    for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                        files.add(objectSummary.getKey());
                    }
                    // If there are more than maxKeys keys in the bucket, get a continuation token
                    // and list the next objects.
                    String token = result.getNextContinuationToken();
                    req.setContinuationToken(token);
                } while (result.isTruncated());
                Logger.warn("LIST: TOTAL FOUND=" + files.size());
                listener.onListComplete(files);
            } catch (AmazonServiceException e) {
                // The call was transmitted successfully, but Amazon S3 couldn't process
                // it, so it returned an error response.
                Logger.error("SERVICE EXCEPTION: " + e.getMessage());
            } catch (SdkClientException e) {
                // Amazon S3 couldn't be contacted for a response, or the client
                // couldn't parse the response from Amazon S3.
                Logger.error("CLIENT EXCEPTION: " + e.getMessage());
            }
        }
    }

    public void list(String host, OnListComplete listener) {
        executor.execute(new ListActivity(host, listener));
    }

    // ------------
    // DELETE FILE
    // ------------

    public interface OnDeleteComplete {
        void onDeleteComplete(int deleted, int errors);
    }

    class DeleteActivity extends CommonActivity implements Runnable {

        final OnDeleteComplete listener;
        final List<String> keys;
        final boolean deleteLocalFile;

        DeleteActivity(String host, List<String> keys, boolean deleteLocalFile, OnDeleteComplete listener) {
            super(host);
            this.listener = listener;
            this.keys = keys;
            this.deleteLocalFile = deleteLocalFile;
        }

        public void run() {
            int deleted = 0;
            int errors = 0;

            for (String key : keys) {
                try {
                    Logger.warn("DELETING FROM BUCKET=" + bucketName + ", KEY=" + key);
                    AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                            .withCredentials(new ProfileCredentialsProvider())
                            .withRegion(REGION)
                            .build();

                    s3Client.deleteObject(new DeleteObjectRequest(bucketName, key));
                    if (deleteLocalFile) {
                        getLocalFile(key).delete();
                    }
                    deleted++;
                } catch (AmazonServiceException e) {
                    // The call was transmitted successfully, but Amazon S3 couldn't process
                    // it, so it returned an error response.
                    Logger.error("SERVICE EXCEPTION: " + e.getMessage());
                    errors++;
                } catch (SdkClientException e) {
                    // Amazon S3 couldn't be contacted for a response, or the client
                    // couldn't parse the response from Amazon S3.
                    Logger.error("CLIENT EXCEPTION: " + e.getMessage());
                    errors++;
                }
            }
            listener.onDeleteComplete(deleted, errors);
        }
    }

    public DeleteAction deleteAction() {
        return new DeleteAction();
    }

    public class DeleteAction {
        String mHost;
        OnDeleteComplete mListener;
        boolean mDeleteLocalFile;

        public DeleteAction() {
        }

        public DeleteAction host(String host) {
            mHost = host;
            return this;
        }

        public DeleteAction listener(OnDeleteComplete listener) {
            mListener = listener;
            return this;
        }

        public DeleteAction deleteLocalFile(boolean flag) {
            this.mDeleteLocalFile = flag;
            return this;
        }

        public void delete(List<String> keys) {
            executor.execute(new DeleteActivity(mHost, keys, mDeleteLocalFile, mListener));
        }
    }
}


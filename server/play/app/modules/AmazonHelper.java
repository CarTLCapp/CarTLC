package modules;

import javax.inject.*;
import java.io.File;
import java.lang.Exception;
import play.Logger;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Transfer.TransferState;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.internal.S3SyncProgressListener;
import com.amazonaws.services.s3.transfer.internal.S3ProgressListener;
import com.amazonaws.services.s3.transfer.PersistableTransfer;
import com.amazonaws.services.s3.model.GetObjectRequest;

import play.Logger;
import com.typesafe.config.Config;

@Singleton
public class AmazonHelper {

    static final String BUCKET_NAME_DEVELOP = "cartlc";
    static final String BUCKET_NAME_RELEASE = "fleettlc";

    public class DownloadException extends Exception {
        DownloadException(String msg) {
            super(msg);
        }
    }

    public interface OnDownloadComplete {
        void onDownloadComplete(File file);
    }

    class DownloadActivity {
        final String key;
        final OnDownloadComplete listener;
        final File targetFile;
        final String host;
        Download download;

        DownloadActivity(String host, String filename, OnDownloadComplete listener) {
            this.key = filename;
            this.listener = listener;
            this.targetFile = getLocalFile(filename);
            this.host = host;
        }

        void run() throws DownloadException {
            String bucketName;
            if (host.startsWith("fleettlc")) {
                bucketName = BUCKET_NAME_RELEASE;
            } else {
                bucketName = BUCKET_NAME_DEVELOP;
            }
            Logger.info("DOWNLOAD: BUCKET=" + bucketName + ", KEY=" + key + " TARGET=" + targetFile.getAbsolutePath());
            try {
                TransferManager xferManager = TransferManagerBuilder.defaultTransferManager();
                GetObjectRequest obj = new GetObjectRequest(bucketName, key);
                download = xferManager.download(obj, targetFile, new S3SyncProgressListener() {
                    public void onPersistableTransfer(PersistableTransfer persistableTransfer) {
                        if (download.getState() == TransferState.Completed) {
                            listener.onDownloadComplete(targetFile);
                        }
                    }
                });
            } catch (AmazonServiceException e) {
                Logger.error("ERROR: " + e.getMessage());
                throw(new DownloadException(e.getErrorMessage()));
            }
        }
    }

    final Config configuration;

    @Inject
    public AmazonHelper(Config configuration) {
        this.configuration = configuration;
    }

    public void download(String host, String filename, OnDownloadComplete listener) throws DownloadException {
        new DownloadActivity(host, filename, listener).run();
    }

    public File getLocalFile(String filename) {
        String downloadDir = configuration.getString("pictureDownloadDir");
        return new File(downloadDir, filename);
    }
}


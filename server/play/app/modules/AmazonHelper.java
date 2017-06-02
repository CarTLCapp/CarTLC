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

import play.Configuration;

@Singleton
public class AmazonHelper {

    static final String BUCKET_NAME = "cartlc";

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
        Download download;

        DownloadActivity(String filename, File targetFile, OnDownloadComplete listener) {
            this.key = filename;
            this.listener = listener;
            this.targetFile = targetFile;
        }

        void run() throws DownloadException {
            try {
                TransferManager xferManager = TransferManagerBuilder.defaultTransferManager();
                GetObjectRequest obj = new GetObjectRequest(BUCKET_NAME, key);
                download = xferManager.download(obj, targetFile, new S3SyncProgressListener() {
                    public void onPersistableTransfer(PersistableTransfer persistableTransfer) {
                        if (download.getState() == TransferState.Completed) {
                            listener.onDownloadComplete(targetFile);
                        }
                    }
                });
            } catch (AmazonServiceException e) {
                throw(new DownloadException(e.getErrorMessage()));
            }
        }
    }

    final Configuration configuration;

    @Inject
    public AmazonHelper(Configuration configuration) {
        this.configuration = configuration;
    }

    public void download(String filename, OnDownloadComplete listener) throws DownloadException {
        String downloadDir = configuration.getString("pictureDownloadDir");
        File targetFile = new File(downloadDir, filename);
        new DownloadActivity(filename, targetFile, listener).run();
    }
}


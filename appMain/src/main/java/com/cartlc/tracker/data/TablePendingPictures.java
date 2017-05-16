package com.cartlc.tracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 4/17/17.
 */

public class TablePendingPictures extends TableString {

    static final String TABLE_NAME = "pending_pictures";

    static TablePendingPictures sInstance;

    static void Init(SQLiteDatabase db) {
        new TablePendingPictures(db);
    }

    public static TablePendingPictures getInstance() {
        return sInstance;
    }

    TablePendingPictures(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        mOrdering = " DESC";
        sInstance = this;
    }

    public File genNewPictureFile() {
        File filePicture = PrefHelper.getInstance().genFullPictureFile();
        add(filePicture.getAbsolutePath());
        return filePicture;
    }

    public Uri genNewPictureUri(Context ctx) {
        return genNewPictureUri(ctx, genNewPictureFile());
    }

    public Uri genNewPictureUri(Context ctx, File file) {
        return FileProvider.getUriForFile(ctx, "com.cartcl.tracker.fileprovider", file);
    }

    public List<Uri> queryPictures(Context ctx) {
        ArrayList<Uri> list = new ArrayList();
        for (String filePath : query()) {
            File filePicture = new File(filePath);
            list.add(genNewPictureUri(ctx, filePicture));
        }
        return list;
    }

    public DataPictureCollection createCollection() {
        DataPictureCollection collection = new DataPictureCollection(
                PrefHelper.getInstance().getNextPictureCollectionID());
        collection.add(query());
        return collection;
    }

    public void deleteAllPending() {
        for (String file : query()) {
            File filePicture = new File(file);
            filePicture.delete();
        }
        clear();
    }
}

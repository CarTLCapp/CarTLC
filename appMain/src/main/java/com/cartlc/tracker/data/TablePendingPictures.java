package com.cartlc.tracker.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

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
        sInstance = this;
    }

    public File genNewPictureFile() {
        File filePicture = PrefHelper.getInstance().genFullPictureFile();
        add(filePicture.getAbsolutePath());
        return filePicture;
    }

    public Uri genNewPictureUri(Context ctx) {
        return DataPicture.getUri(ctx, genNewPictureFile());
    }

    public List<DataPicture> queryPictures() {
        ArrayList<DataPicture> list = new ArrayList();
        try {
            final String[] columns  = {KEY_ROWID, KEY_VALUE};
            final String   orderBy  = KEY_VALUE + " DESC";
            Cursor         cursor   = mDb.query(mTableName, columns, null, null, null, null, orderBy);
            int            idxRow   = cursor.getColumnIndex(KEY_ROWID);
            int            idxValue = cursor.getColumnIndex(KEY_VALUE);
            while (cursor.moveToNext()) {
                list.add(new DataPicture(cursor.getLong(idxRow), cursor.getString(idxValue)));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
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

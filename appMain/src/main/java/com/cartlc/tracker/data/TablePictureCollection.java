/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.etc.PrefHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */

public class TablePictureCollection {

    static TablePictureCollection sInstance;

    static void Init(SQLiteDatabase db) {
        new TablePictureCollection(db);
    }

    public static TablePictureCollection getInstance() {
        return sInstance;
    }

    static final String TABLE_NAME = "picture_collection";

    static final String KEY_ROWID              = "_id";
    static final String KEY_COLLECTION_ID      = "collection_id";
    static final String KEY_PICTURE_FILENAME   = "picture_filename";
    static final String KEY_UPLOADING_FILENAME = "uploading_filename";
    static final String KEY_NOTE               = "picture_note";
    static final String KEY_UPLOADED           = "uploaded";

    final SQLiteDatabase mDb;

    public TablePictureCollection(SQLiteDatabase db) {
        sInstance = this;
        this.mDb = db;
    }

    public void clear() {
        try {
            mDb.delete(TABLE_NAME, null, null);
        } catch (Exception ex) {
        }
    }

//    public void drop() {
//        mDb.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(TABLE_NAME);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_COLLECTION_ID);
        sbuf.append(" long default 0, ");
        sbuf.append(KEY_PICTURE_FILENAME);
        sbuf.append(" text, ");
        sbuf.append(KEY_UPLOADING_FILENAME);
        sbuf.append(" text, ");
        sbuf.append(KEY_NOTE);
        sbuf.append(" text, ");
        sbuf.append(KEY_UPLOADED);
        sbuf.append(" bit default 0)");
        mDb.execSQL(sbuf.toString());
    }

    public static void upgrade3(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEY_NOTE + " text");
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TablePictureCollection.class, "upgrade3()", "db");
        }
    }

    public DataPicture add(File picture, Long collection_id) {
        DataPicture item = new DataPicture();
        item.unscaledFilename = picture.getAbsolutePath();
        update(item, collection_id);
        return item;
    }

    public void add(DataPictureCollection collection) {
        mDb.beginTransaction();
        try {
            for (DataPicture ele : collection.pictures) {
                update(ele, collection.id);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TablePictureCollection.class, "add()", "db");
        } finally {
            mDb.endTransaction();
        }
    }

    public DataPictureCollection query(long collection_id) {
        final String selection = KEY_COLLECTION_ID + " =?";
        final String[] selectionArgs = {Long.toString(collection_id)};
        DataPictureCollection collection = new DataPictureCollection(collection_id);
        collection.pictures = query(selection, selectionArgs);
        return collection;
    }

    public List<DataPicture> queryPictures(long collection_id) {
        final String selection = KEY_COLLECTION_ID + "=?";
        final String[] selectionArgs = {Long.toString(collection_id)};
        return query(selection, selectionArgs);
    }

    public List<DataPicture> query(String selection, String[] selectionArgs) {
        List<DataPicture> list = new ArrayList();
        try {
            Cursor cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            int idxPicture = cursor.getColumnIndex(KEY_PICTURE_FILENAME);
            int idxUploading = cursor.getColumnIndex(KEY_UPLOADING_FILENAME);
            int idxUploaded = cursor.getColumnIndex(KEY_UPLOADED);
            int idxNote = cursor.getColumnIndex(KEY_NOTE);
            while (cursor.moveToNext()) {
                list.add(new DataPicture(
                        cursor.getLong(idxRowId),
                        cursor.getString(idxPicture),
                        cursor.getString(idxUploading),
                        cursor.getString(idxNote),
                        cursor.getShort(idxUploaded) != 0));
            }
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TablePictureCollection.class, "query()", "db");
        }
        return list;
    }

    public List<DataPicture> removeNonExistant(List<DataPicture> list) {
        List<DataPicture> filtered = new ArrayList();
        for (DataPicture item : list) {
            if (item.existsUnscaled() || item.existsScaled()) {
                filtered.add(item);
            } else {
                remove(item);
            }
        }
        return filtered;
    }

    synchronized
    public void clearUploadedUnscaledPhotos() {
        String selection = KEY_UPLOADED + "=1";
        List<DataPicture> list = query(selection, null);
        for (DataPicture item : list) {
            if (item.existsUnscaled()) {
                item.getUnscaledFile().delete();
            }
        }
    }

    public int countPictures(long collection_id) {
        int count = 0;
        try {
            final String[] columns = {KEY_ROWID, KEY_PICTURE_FILENAME, KEY_UPLOADING_FILENAME};
            final String selection = KEY_COLLECTION_ID + "=?";
            final String[] selectionArgs = {Long.toString(collection_id)};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            int idxPicture = cursor.getColumnIndex(KEY_PICTURE_FILENAME);
            int idxUploading = cursor.getColumnIndex(KEY_UPLOADING_FILENAME);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            ArrayList<Long> delete = new ArrayList();
            while (cursor.moveToNext()) {
                String unscaled = cursor.getString(idxPicture);
                String uploading = cursor.getString(idxUploading);
                File unscaledFile;
                File uploadingFile;
                if (unscaled != null) {
                    unscaledFile = new File(unscaled);
                } else {
                    unscaledFile = null;
                }
                if (uploading != null) {
                    uploadingFile = new File(uploading);
                } else {
                    uploadingFile = null;
                }
                if ((unscaledFile != null && unscaledFile.exists()) || (uploadingFile != null && uploadingFile.exists())) {
                    count++;
                } else {
                    delete.add(cursor.getLong(idxRowId));
                }
            }
            cursor.close();
            String where = KEY_ROWID + "=?";
            for (Long id : delete) {
                String[] whereArgs = {Long.toString(id)};
                mDb.delete(TABLE_NAME, where, whereArgs);
            }
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TablePictureCollection.class, "countPictures()", "db");
        }
        return count;
    }

    public DataPictureCollection createCollectionFromPending() {
        DataPictureCollection collection = new DataPictureCollection(
                PrefHelper.getInstance().getNextPictureCollectionID());
        collection.pictures = removeNonExistant(queryPictures(0));
        return collection;
    }

    public void clearPendingPictures() {
        try {
            final String where = KEY_COLLECTION_ID + " =0";
            mDb.delete(TABLE_NAME, where, null);
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TablePictureCollection.class, "clearPendingPictures()", "db");
        }
    }

    public void remove(DataPicture item) {
        try {
            final String where = KEY_COLLECTION_ID + "=?";
            final String[] whereArgs = {Long.toString(item.id)};
            mDb.delete(TABLE_NAME, where, whereArgs);
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TablePictureCollection.class, "remove()", "db");
        }
    }

    synchronized
    public void setUploaded(DataPicture item) {
        item.uploaded = true;
        update(item, null);
    }

    public void update(DataPicture item, Long collection_id) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_PICTURE_FILENAME, item.unscaledFilename);
            values.put(KEY_UPLOADING_FILENAME, item.scaledFilename);
            values.put(KEY_NOTE, item.note);
            values.put(KEY_UPLOADED, item.uploaded ? 1 : 0);
            if (collection_id != null) {
                values.put(KEY_COLLECTION_ID, collection_id);
            }
            if (item.id > 0) {
                String where = KEY_ROWID + "=?";
                String[] whereArgs = {Long.toString(item.id)};
                if (mDb.update(TABLE_NAME, values, where, whereArgs) == 0) {
                    Timber.e("Mysterious failure updating picture ID " + item.id);
                    item.id = 0;
                }
            }
            if (item.id == 0) {
                item.id = mDb.insert(TABLE_NAME, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TablePictureCollection.class, "update()", "db");
        } finally {
            mDb.endTransaction();
        }
    }

    public void clearUploaded() {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_UPLOADED, 0);
            if (mDb.update(TABLE_NAME, values, null, null) == 0) {
                Timber.e("TablePictureCollection.clearUploaded(): Unable to update entries");
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TablePictureCollection.class, "clearUploaded()", "db");
        } finally {
            mDb.endTransaction();
        }
    }

}

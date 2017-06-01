package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    static final String KEY_ROWID = "_id";
    static final String KEY_COLLECTION_ID = "collection_id";
    static final String KEY_PICTURE_FILENAME = "picture_filename";
    static final String KEY_UPLOADED = "uploaded";

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

    public void drop() {
        mDb.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(TABLE_NAME);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_COLLECTION_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_PICTURE_FILENAME);
        sbuf.append(" text, ");
        sbuf.append(KEY_UPLOADED);
        sbuf.append(" bit)");
        mDb.execSQL(sbuf.toString());
    }

    public void add(DataPictureCollectionItem collection) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (DataPicture ele : collection.pictures) {
                values.clear();
                values.put(KEY_COLLECTION_ID, collection.id);
                values.put(KEY_PICTURE_FILENAME, ele.pictureFilename);
                values.put(KEY_UPLOADED, ele.uploaded);
                mDb.insert(TABLE_NAME, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public DataPictureCollectionItem query(long collection_id) {
        DataPictureCollectionItem collection = null;
        try {
            final String[] columns = {KEY_ROWID, KEY_PICTURE_FILENAME, KEY_UPLOADED};
            final String selection = KEY_COLLECTION_ID + " =?";
            final String[] selectionArgs = {Long.toString(collection_id)};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            int idxPicture = cursor.getColumnIndex(KEY_PICTURE_FILENAME);
            int idxUploaded = cursor.getColumnIndex(KEY_UPLOADED);
            collection = new DataPictureCollectionItem(collection_id);
            while (cursor.moveToNext()) {
                collection.add(new DataPicture(
                                    cursor.getLong(idxRowId),
                                    cursor.getString(idxPicture),
                                    cursor.getShort(idxUploaded) != 0));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return collection;
    }

}

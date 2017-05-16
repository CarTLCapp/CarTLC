package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */

public abstract class TableCollection {

    static final String KEY_ROWID       = "_id";
    static final String KEY_GROUPING_ID = "grouping_id";
    static final String KEY_VALUE_ID    = "value_id";

    final SQLiteDatabase mDb;
    final String         mTableName;

    public TableCollection(SQLiteDatabase db, String tableName) {
        this.mTableName = tableName;
        this.mDb = db;
    }

    public void clear() {
        try {
            mDb.delete(mTableName, null, null);
        } catch (Exception ex) {
        }
    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(mTableName);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_GROUPING_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_VALUE_ID);
        sbuf.append(" long)");
        mDb.execSQL(sbuf.toString());
    }


    public int count() {
        int count = 0;
        try {
            Cursor cursor = mDb.query(mTableName, null, null, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return count;
    }

    public void add(DataEquipmentCollection collection) {
        add(collection.id, collection.equipmentListIds);
    }

    public void add(long collectionId, List<Long> ids) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (Long id : ids) {
                values.clear();
                values.put(KEY_GROUPING_ID, collectionId);
                values.put(KEY_VALUE_ID, id);
                mDb.insert(mTableName, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }



    public List<Long> query(long other_id) {
        List<Long> collection = new ArrayList();
        try {
            final String[] columns       = {KEY_VALUE_ID};
            final String   selection     = KEY_GROUPING_ID + " =?";
            final String[] selectionArgs = {Long.toString(other_id)};
            Cursor         cursor        = mDb.query(mTableName, columns, selection, selectionArgs, null, null, null, null);
            int            idxValue      = cursor.getColumnIndex(KEY_VALUE_ID);
            while (cursor.moveToNext()) {
                collection.add(cursor.getLong(idxValue));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return collection;
    }

}

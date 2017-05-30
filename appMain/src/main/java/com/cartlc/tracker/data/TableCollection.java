package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */

public abstract class TableCollection {

    static final String KEY_ROWID         = "_id";
    static final String KEY_COLLECTION_ID = "collection_id";
    static final String KEY_VALUE_ID      = "value_id";
    static final String KEY_SERVER_ID     = "server_id";

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
        sbuf.append(KEY_COLLECTION_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_VALUE_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_SERVER_ID);
        sbuf.append(" int)");
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

    public void add(DataCollectionEquipment collection) {
        add(collection.id, collection.equipmentListIds);
    }

    public void add(long collectionId, List<Long> ids) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (Long id : ids) {
                values.clear();
                values.put(KEY_COLLECTION_ID, collectionId);
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

    public void add(long collectionId, long valueId) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_COLLECTION_ID, collectionId);
            values.put(KEY_VALUE_ID, valueId);
            mDb.insert(mTableName, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public void add(DataCollectionItem item) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_COLLECTION_ID, item.collection_id);
            values.put(KEY_VALUE_ID, item.equipment_id);
            values.put(KEY_SERVER_ID, item.server_id);
            item.id = mDb.insert(mTableName, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }
    public void update(DataCollectionItem item) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_COLLECTION_ID, item.collection_id);
            values.put(KEY_VALUE_ID, item.equipment_id);
            values.put(KEY_SERVER_ID, item.server_id);
            String where = KEY_ROWID + "=?";
            String [] whereArgs = { Long.toString(item.id) };
            mDb.update(mTableName, values, where, whereArgs);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }


    public List<Long> query(long collection_id) {
        List<Long> collection = new ArrayList();
        try {
            final String[] columns = {KEY_VALUE_ID};
            final String selection = KEY_COLLECTION_ID + " =?";
            final String[] selectionArgs = {Long.toString(collection_id)};
            Cursor cursor = mDb.query(mTableName, columns, selection, selectionArgs, null, null, null, null);
            int idxValue = cursor.getColumnIndex(KEY_VALUE_ID);
            while (cursor.moveToNext()) {
                collection.add(cursor.getLong(idxValue));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return collection;
    }

    public List<DataCollectionItem> query() {
        List<DataCollectionItem> items = new ArrayList();
        mDb.beginTransaction();
        try {
            final String[] columns = {KEY_ROWID, KEY_COLLECTION_ID, KEY_VALUE_ID, KEY_SERVER_ID};
            Cursor cursor = mDb.query(mTableName, columns, null, null, null, null, null, null);
            int idxValue = cursor.getColumnIndex(KEY_VALUE_ID);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
            int idxCollectionId = cursor.getColumnIndex(KEY_COLLECTION_ID);
            while (cursor.moveToNext()) {
                DataCollectionItem item = new DataCollectionItem();
                item.id = cursor.getLong(idxRowId);
                item.collection_id = cursor.getLong(idxCollectionId);
                item.equipment_id = cursor.getLong(idxValue);
                item.server_id = cursor.getInt(idxServerId);
                items.add(item);
            }
            cursor.close();
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
        return items;
    }


    public DataCollectionItem queryByServerId(int server_id) {
        DataCollectionItem item = null;
        mDb.beginTransaction();
        try {
            final String[] columns = {KEY_ROWID, KEY_COLLECTION_ID, KEY_VALUE_ID};
            String selection = KEY_SERVER_ID + "=?";
            String selectionArgs [] = { Integer.toString(server_id) };
            Cursor cursor = mDb.query(mTableName, columns, selection, selectionArgs, null, null, null, null);
            int idxValue = cursor.getColumnIndex(KEY_VALUE_ID);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            int idxCollectionId = cursor.getColumnIndex(KEY_COLLECTION_ID);
            if (cursor.moveToFirst()) {
                item = new DataCollectionItem();
                item.id = cursor.getLong(idxRowId);
                item.collection_id = cursor.getLong(idxCollectionId);
                item.equipment_id = cursor.getLong(idxValue);
                item.server_id = server_id;
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
        return item;
    }



}

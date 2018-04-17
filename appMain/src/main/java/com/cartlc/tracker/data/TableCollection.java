/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cartlc.tracker.app.TBApplication;

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
    static final String KEY_IS_BOOT       = "is_boot_strap";

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

    public void drop() {
        mDb.execSQL("DROP TABLE IF EXISTS " + mTableName);
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
        sbuf.append(" int, ");
        sbuf.append(KEY_IS_BOOT);
        sbuf.append(" bit default 0)");
        mDb.execSQL(sbuf.toString());
    }

    public int count() {
        int count = 0;
        try {
            Cursor cursor = mDb.query(mTableName, null, null, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableCollection.class, "count()", "db");
        }
        return count;
    }

    public int countValues(long valueId) {
        int count = 0;
        try {
            String where = KEY_VALUE_ID + "=?";
            String [] whereArgs = new String [] { Long.toString(valueId) };
            Cursor cursor = mDb.query(mTableName, null, where, whereArgs, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableCollection.class, "countValues()", "db");
        }
        return count;
    }

    public void save(DataCollectionEquipment collection) {
        save(collection.id, collection.equipmentListIds);
    }

    public void save(long collectionId, List<Long> ids) {
        removeByCollectionId(collectionId);
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
            TBApplication.ReportError(ex, TableCollection.class, "saveUploaded()", "db");
        } finally {
            mDb.endTransaction();
        }
    }

    public void addTest(long collectionId, List<Long> ids) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (Long id : ids) {
                values.clear();
                values.put(KEY_COLLECTION_ID, collectionId);
                values.put(KEY_VALUE_ID, id);
                values.put(KEY_IS_BOOT, 1);
                mDb.insert(mTableName, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableCollection.class, "saveUploaded()", "db");
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
            TBApplication.ReportError(ex, TableCollection.class, "add(id,id)", "db");
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
            values.put(KEY_VALUE_ID, item.value_id);
            values.put(KEY_SERVER_ID, item.server_id);
            values.put(KEY_IS_BOOT, item.isBootStrap);
            item.id = mDb.insert(mTableName, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableCollection.class, "add(item)", "db");
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
            values.put(KEY_VALUE_ID, item.value_id);
            values.put(KEY_SERVER_ID, item.server_id);
            values.put(KEY_IS_BOOT, item.isBootStrap);
            String where = KEY_ROWID + "=?";
            String[] whereArgs = {Long.toString(item.id)};
            mDb.update(mTableName, values, where, whereArgs);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableCollection.class, "update(item)", "db");
        } finally {
            mDb.endTransaction();
        }
    }

    public List<Long> query(long collection_id) {
        List<Long> collection = new ArrayList();
        try {
            final String[] columns = {KEY_VALUE_ID};
            final String selection = KEY_COLLECTION_ID + "=?";
            final String[] selectionArgs = {Long.toString(collection_id)};
            Cursor cursor = mDb.query(mTableName, columns, selection, selectionArgs, null, null, null, null);
            int idxValue = cursor.getColumnIndex(KEY_VALUE_ID);
            while (cursor.moveToNext()) {
                collection.add(cursor.getLong(idxValue));
            }
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableCollection.class, "query(id)", "db");
        }
        return collection;
    }

    public List<DataCollectionItem> query() {
        List<DataCollectionItem> items = new ArrayList();
        mDb.beginTransaction();
        try {
            Cursor cursor = mDb.query(mTableName, null, null, null, null, null, null, null);
            int idxValue = cursor.getColumnIndex(KEY_VALUE_ID);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
            int idxCollectionId = cursor.getColumnIndex(KEY_COLLECTION_ID);
            int idxTest = cursor.getColumnIndex(KEY_IS_BOOT);
            while (cursor.moveToNext()) {
                DataCollectionItem item = new DataCollectionItem();
                item.id = cursor.getLong(idxRowId);
                item.collection_id = cursor.getLong(idxCollectionId);
                item.value_id = cursor.getLong(idxValue);
                item.server_id = cursor.getInt(idxServerId);
                item.isBootStrap = cursor.getShort(idxTest) != 0;
                items.add(item);
            }
            cursor.close();
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableCollection.class, "query()", "db");
        } finally {
            mDb.endTransaction();
        }
        return items;
    }

    public DataCollectionItem queryByServerId(int server_id) {
        DataCollectionItem item = null;
        mDb.beginTransaction();
        try {
            String selection = KEY_SERVER_ID + "=?";
            String selectionArgs[] = {Integer.toString(server_id)};
            Cursor cursor = mDb.query(mTableName, null, selection, selectionArgs, null, null, null, null);
            int idxValue = cursor.getColumnIndex(KEY_VALUE_ID);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            int idxCollectionId = cursor.getColumnIndex(KEY_COLLECTION_ID);
            int idxTest = cursor.getColumnIndex(KEY_IS_BOOT);
            if (cursor.moveToFirst()) {
                item = new DataCollectionItem();
                item.id = cursor.getLong(idxRowId);
                item.collection_id = cursor.getLong(idxCollectionId);
                item.value_id = cursor.getLong(idxValue);
                item.server_id = server_id;
                item.isBootStrap = cursor.getShort(idxTest) != 0;
            }
            cursor.close();
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableCollection.class, "queryByServerId(id)", "db");
        } finally {
            mDb.endTransaction();
        }
        return item;
    }

    public void remove(long id) {
        try {
            String where = KEY_ROWID + "=?";
            String[] whereArgs = new String[]{Long.toString(id)};
            mDb.delete(mTableName, where, whereArgs);
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableCollection.class, "remove(id)", "db");
        }
    }

    public void removeByCollectionId(long id) {
        try {
            String where = KEY_COLLECTION_ID + "=?";
            String[] whereArgs = new String[]{Long.toString(id)};
            mDb.delete(mTableName, where, whereArgs);
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableCollection.class, "removeByCollectionId(id)", "db");
        }
    }

    public void clearUploaded() {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_SERVER_ID, 0);
            if (mDb.update(mTableName, values, null, null) == 0) {
                Timber.e("TableCollection.clearUploaded(): Unable to update entries");
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableCollection.class, "clearUploaded()", "db");
        } finally {
            mDb.endTransaction();
        }
    }

}

package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */

public class TableProjectAddressCombo {

    static TableProjectAddressCombo sInstance;

    static void Init(SQLiteDatabase db) {
        new TableProjectAddressCombo(db);
    }

    public static TableProjectAddressCombo getInstance() {
        return sInstance;
    }

    static final String TABLE_NAME = "list_project_address_combo";

    static final String KEY_ROWID      = "_id";
    static final String KEY_PROJECT_ID = "project_id";
    static final String KEY_ADDRESS_ID = "address_id";

    final SQLiteDatabase mDb;

    public TableProjectAddressCombo(SQLiteDatabase db) {
        sInstance = this;
        this.mDb = db;
    }

    public void clear() {
        try {
            mDb.delete(TABLE_NAME, null, null);
        } catch (Exception ex) {
        }
    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(TABLE_NAME);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_PROJECT_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_ADDRESS_ID);
        sbuf.append(" long)");
        mDb.execSQL(sbuf.toString());
    }

    public long add(DataProjectAddressCombo projectGroup) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_PROJECT_ID, projectGroup.projectNameId);
            values.put(KEY_ADDRESS_ID, projectGroup.addressId);
            projectGroup.id = mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
        return projectGroup.id;
    }

    public int count() {
        int count = 0;
        try {
            Cursor cursor = mDb.query(true, TABLE_NAME, null, null, null, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return count;
    }

    public List<DataProjectAddressCombo> query() {
        ArrayList<DataProjectAddressCombo> list = new ArrayList();
        try {
            final String[]   columns      = {KEY_ROWID, KEY_PROJECT_ID, KEY_ADDRESS_ID};
            Cursor           cursor       = mDb.query(true, TABLE_NAME, columns, null, null, null, null, null, null);
            int              idxRowId     = cursor.getColumnIndex(KEY_ROWID);
            int              idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID);
            int              idxAddressId = cursor.getColumnIndex(KEY_ADDRESS_ID);
            DataProjectAddressCombo item;
            while (cursor.moveToNext()) {
                item = new DataProjectAddressCombo(cursor.getLong(idxRowId), cursor.getLong(idxProjectId), cursor.getLong(idxAddressId));
                list.add(item);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        Collections.sort(list);
        return list;
    }

    public DataProjectAddressCombo query(long id) {
        DataProjectAddressCombo item = null;
        try {
            final String[] columns       = {KEY_PROJECT_ID, KEY_ADDRESS_ID};
            final String   selection     = KEY_ROWID + " =?";
            final String[] selectionArgs = {Long.toString(id)};
            Cursor         cursor        = mDb.query(true, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            int            idxProjectId  = cursor.getColumnIndex(KEY_PROJECT_ID);
            int            idxAddressId  = cursor.getColumnIndex(KEY_ADDRESS_ID);
            if (cursor.moveToFirst()) {
                item = new DataProjectAddressCombo(cursor.getLong(idxProjectId), cursor.getLong(idxAddressId));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return item;
    }

    public long queryProjectGroupId(long projectNameId, long addressId) {
        long id = -1L;
        try {
            final String[] columns = {KEY_ROWID};
            StringBuilder  sbuf    = new StringBuilder();
            sbuf.append(KEY_PROJECT_ID);
            sbuf.append(" =? AND ");
            sbuf.append(KEY_ADDRESS_ID);
            sbuf.append(" =?");
            final String   selection     = sbuf.toString();
            final String[] selectionArgs = {Long.toString(projectNameId), Long.toString(addressId)};
            Cursor         cursor        = mDb.query(true, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            int            idxRowId      = cursor.getColumnIndex(KEY_ROWID);
            if (cursor.moveToFirst()) {
                id = cursor.getLong(idxRowId);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return id;
    }

}

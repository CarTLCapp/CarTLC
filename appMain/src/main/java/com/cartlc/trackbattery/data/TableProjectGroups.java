package com.cartlc.trackbattery.data;

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

public class TableProjectGroups {

    static TableProjectGroups sInstance;

    static void Init(SQLiteDatabase db) {
        new TableProjectGroups(db);
    }

    public static TableProjectGroups getInstance() {
        return sInstance;
    }

    static final String TABLE_NAME = "list_project_groups";

    static final String KEY_ROWID = "_id";
    static final String KEY_PROJECT_ID = "project_id";
    static final String KEY_ADDRESS_ID = "address_id";

    final SQLiteDatabase mDb;

    public TableProjectGroups(SQLiteDatabase db) {
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

    public long add(DataProjectGroup projectGroup) {
        long id = -1L;
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_PROJECT_ID, projectGroup.projectId);
            values.put(KEY_ADDRESS_ID, projectGroup.addressId);
            id = mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
        return id;
    }

    public List<DataProjectGroup> query() {
        ArrayList<DataProjectGroup> list = new ArrayList();
        try {
            final String[] columns = {KEY_PROJECT_ID, KEY_ADDRESS_ID};
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, null, null, null, null, null, null);
            int idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID);
            int idxAddressId = cursor.getColumnIndex(KEY_ADDRESS_ID);
            DataProjectGroup item;
            while (cursor.moveToNext()) {
                item = new DataProjectGroup(cursor.getLong(idxProjectId), cursor.getLong(idxAddressId));
                list.add(item);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        Collections.sort(list);
        return list;
    }

    public DataProjectGroup query(long id) {
        DataProjectGroup item = null;
        try {
            final String[] columns = {KEY_PROJECT_ID, KEY_ADDRESS_ID};
            final String selection = KEY_ROWID + " =?";
            final String[] selectionArgs = {Long.toString(id)};
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            int idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID);
            int idxAddressId = cursor.getColumnIndex(KEY_ADDRESS_ID);
            if (cursor.moveToFirst()) {
                item = new DataProjectGroup(cursor.getLong(idxProjectId), cursor.getLong(idxAddressId));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return item;
    }

}

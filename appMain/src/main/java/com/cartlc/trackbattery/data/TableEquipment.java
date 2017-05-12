package com.cartlc.trackbattery.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 4/17/17.
 */

public class TableEquipment {

    static final String TABLE_NAME = "list_equipment";

    static final String KEY_ROWID = "_id";
    static final String KEY_NAME = "name";
    static final String KEY_PROJECT_ID = "project_id";

    static TableEquipment sInstance;

    static void Init(SQLiteDatabase db) {
        new TableEquipment(db);
    }

    public static TableEquipment getInstance() {
        return sInstance;
    }

    final SQLiteDatabase mDb;

    TableEquipment(SQLiteDatabase db) {
        this.mDb = db;
        sInstance = this;
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
        sbuf.append(KEY_NAME);
        sbuf.append(" text, ");
        sbuf.append(KEY_PROJECT_ID);
        sbuf.append(" long)");
        mDb.execSQL(sbuf.toString());
    }

    public int count() {
        int count = 0;
        try {
            Cursor cursor = mDb.query(TABLE_NAME, null, null, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return count;
    }

    public long add(String name, long projectId) {
        long id = -1L;
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, name);
            values.put(KEY_PROJECT_ID, projectId);
            id = mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
        return id;
    }

    public void add(List<DataEquipment> list) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (DataEquipment value : list) {
                values.clear();
                values.put(KEY_NAME, value.name);
                values.put(KEY_PROJECT_ID, value.projectId);
                mDb.insert(TABLE_NAME, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public List<String> query(long projectId) {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_NAME};
            final String selection = KEY_PROJECT_ID + "=?";
            final String orderBy = KEY_NAME + " ASC";
            final String[] selectionArgs = {Long.toString(projectId)};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, orderBy);
            int idxValue = cursor.getColumnIndex(KEY_NAME);
            while (cursor.moveToNext()) {
                list.add(cursor.getString(idxValue));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }
}

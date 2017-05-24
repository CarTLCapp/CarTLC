package com.cartlc.tracker.data;

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

    static final String KEY_ROWID   = "_id";
    static final String KEY_NAME    = "name";
    static final String KEY_LOCAL   = "is_local";

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
        sbuf.append(KEY_LOCAL);
        sbuf.append(" bit)");
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

    public long add(String name) {
        long id = -1L;
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, name);
            id = mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
        return id;
    }

    public long addLocal(String name) {
        long id = -1L;
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, name);
            values.put(KEY_LOCAL, 1);
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
                values.put(KEY_LOCAL, value.isLocal ? 1 : 0);
                value.id = mDb.insert(TABLE_NAME, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public DataEquipment query(long id) {
        try {
            return query_(id);
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return null;
    }

    public DataEquipment query_(long id) {
        DataEquipment  item          = null;
        final String[] columns       = {KEY_NAME, KEY_LOCAL};
        final String   selection     = KEY_ROWID + "=?";
        final String[] selectionArgs = {Long.toString(id)};
        Cursor         cursor        = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
        final int      idxName       = cursor.getColumnIndex(KEY_NAME);
        final int      idxLocal      = cursor.getColumnIndex(KEY_LOCAL);
        if (cursor.moveToFirst()) {
            String  name    = cursor.getString(idxName);
            boolean local   = cursor.getShort(idxLocal) != 0 ? true : false;
            item = new DataEquipment(id, name, local);
        }
        cursor.close();
        return item;
    }

    public long query(String name) {
        long id = -1L;
        final String[] columns       = {KEY_ROWID, KEY_LOCAL};
        final String   selection     = KEY_NAME + "=?";
        final String[] selectionArgs = {name};
        Cursor         cursor        = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
        final int      idxRowId       = cursor.getColumnIndex(KEY_ROWID);
        if (cursor.moveToFirst()) {
            id = cursor.getLong(idxRowId);
        }
        cursor.close();
        return id;
    }

}

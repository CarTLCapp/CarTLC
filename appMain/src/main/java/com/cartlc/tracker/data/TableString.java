package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 4/14/17.
 */

public class TableString {

    static final String KEY_ROWID = "_id";
    static final String KEY_VALUE = "value";

    protected final SQLiteDatabase mDb;
    protected final String         mTableName;
    protected       List<String>   mEntries;

    protected TableString(SQLiteDatabase db, String tableName) {
        this.mTableName = tableName;
        this.mDb = db;
    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(mTableName);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_VALUE);
        sbuf.append(" text not null)");
        mDb.execSQL(sbuf.toString());
    }

    public void clear() {
        try {
            mDb.delete(mTableName, null, null);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    public void remove(String value) {
        try {
            String   where     = KEY_VALUE + "=?";
            String[] whereArgs = {value};
            mDb.delete(mTableName, where, whereArgs);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    public void remove(long id) {
        try {
            String   where     = KEY_ROWID + "=?";
            String[] whereArgs = {Long.toString(id)};
            mDb.delete(mTableName, where, whereArgs);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    public void add(List<String> list) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (String value : list) {
                values.clear();
                values.put(KEY_VALUE, value);
                mDb.insert(mTableName, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public long add(String item) {
        long id = -1L;
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_VALUE, item);
            id = mDb.insert(mTableName, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
        return id;
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

    public List<String> query() {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_VALUE};
            final String   orderBy = KEY_VALUE + " ASC";

            Cursor cursor   = mDb.query(mTableName, columns, null, null, null, null, orderBy);
            int    idxValue = cursor.getColumnIndex(KEY_VALUE);
            while (cursor.moveToNext()) {
                list.add(cursor.getString(idxValue));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public String query(long id) {
        String projectName = null;
        try {
            final String[] columns       = {KEY_VALUE};
            final String   selection     = KEY_ROWID + "=?";
            final String[] selectionArgs = {Long.toString(id)};
            Cursor         cursor        = mDb.query(mTableName, columns, selection, selectionArgs, null, null, null);
            int            idxValue      = cursor.getColumnIndex(KEY_VALUE);
            if (cursor.moveToFirst()) {
                projectName = cursor.getString(idxValue);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return projectName;
    }

    public long query(String name) {
        long rowId = -1L;
        try {
            final String[] columns       = {KEY_ROWID};
            final String   selection     = KEY_VALUE + "=?";
            final String[] selectionArgs = {name};
            Cursor         cursor        = mDb.query(mTableName, columns, selection, selectionArgs, null, null, null);
            int            idxValue      = cursor.getColumnIndex(KEY_ROWID);
            if (cursor.moveToFirst()) {
                rowId = cursor.getLong(idxValue);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return rowId;
    }

    public List<String> getEntries() {
        if (mEntries == null) {
            mEntries = query();
        }
        return mEntries;
    }

    public int indexOf(String project) {
        return getEntries().indexOf(project);
    }
}

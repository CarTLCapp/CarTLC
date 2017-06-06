package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 4/17/17.
 */

public class TableNote {
    static final String TABLE_NAME = "list_notes";

    static final String KEY_ROWID     = "_id";
    static final String KEY_NAME      = "name";
    static final String KEY_VALUE     = "value";
    static final String KEY_TYPE      = "type";
    static final String KEY_SERVER_ID = "server_id";
    static final String KEY_IS_TEST   = "is_test";

    static TableNote sInstance;

    static void Init(SQLiteDatabase db) {
        new TableNote(db);
    }

    public static TableNote getInstance() {
        return sInstance;
    }

    final SQLiteDatabase mDb;

    TableNote(SQLiteDatabase db) {
        mDb = db;
        sInstance = this;
    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(TABLE_NAME);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_NAME);
        sbuf.append(" text not null, ");
        sbuf.append(KEY_VALUE);
        sbuf.append(" text, ");
        sbuf.append(KEY_TYPE);
        sbuf.append(" int, ");
        sbuf.append(KEY_SERVER_ID);
        sbuf.append(" int, ");
        sbuf.append(KEY_IS_TEST);
        sbuf.append(" bit)");
        mDb.execSQL(sbuf.toString());
    }

    public void drop() {
        mDb.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public void clear() {
        try {
            mDb.delete(TABLE_NAME, null, null);
        } catch (Exception ex) {
            Timber.e(ex);
        }
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

    public void add(List<DataNote> list) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (DataNote value : list) {
                values.clear();
                values.put(KEY_NAME, value.name);
                values.put(KEY_TYPE, value.type.ordinal());
                values.put(KEY_VALUE, value.value);
                values.put(KEY_SERVER_ID, value.server_id);
                values.put(KEY_IS_TEST, value.isTest ? 1 : 0);
                mDb.insert(TABLE_NAME, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public long add(DataNote item) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_NAME, item.name);
            values.put(KEY_TYPE, item.type.ordinal());
            values.put(KEY_VALUE, item.value);
            values.put(KEY_SERVER_ID, item.server_id);
            values.put(KEY_IS_TEST, item.isTest ? 1 : 0);
            item.id = mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
        return item.id;
    }

    public void clearValues() {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_VALUE, (String) null);
            mDb.update(TABLE_NAME, values, null, null);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public long query(String name) {
        long rowId = -1L;
        try {
            final String[] columns = {KEY_ROWID};
            final String selection = KEY_NAME + "=?";
            final String[] selectionArgs = {name};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
            int idxValue = cursor.getColumnIndex(KEY_ROWID);
            if (cursor.moveToFirst()) {
                rowId = cursor.getLong(idxValue);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return rowId;
    }

    public DataNote query(long id) {
        final String selection = KEY_ROWID + "=?";
        final String[] selectionArgs = {Long.toString(id)};
        List<DataNote> list = query(selection, selectionArgs);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public DataNote queryByServerId(int server_id) {
        final String selection = KEY_SERVER_ID + "=?";
        final String[] selectionArgs = {Integer.toString(server_id)};
        List<DataNote> list = query(selection, selectionArgs);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public List<DataNote> query() {
        return query(null, null);
    }

    public List<DataNote> query(String selection, String [] selectionArgs) {
        List<DataNote> list = new ArrayList();
        try {
            final String[] columns = {KEY_ROWID, KEY_NAME, KEY_VALUE, KEY_TYPE, KEY_SERVER_ID, KEY_IS_TEST};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            int idxName = cursor.getColumnIndex(KEY_NAME);
            int idxValue = cursor.getColumnIndex(KEY_VALUE);
            int idxType = cursor.getColumnIndex(KEY_TYPE);
            int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
            int idxTest = cursor.getColumnIndex(KEY_IS_TEST);
            while (cursor.moveToNext()) {
                DataNote item = new DataNote();
                item.id = cursor.getLong(idxRowId);
                item.name = cursor.getString(idxName);
                item.value = cursor.getString(idxValue);
                item.type = DataNote.Type.from(cursor.getInt(idxType));
                item.server_id = cursor.getInt(idxServerId);
                item.isTest = cursor.getShort(idxTest) != 0;
                list.add(item);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public String getName(long id) {
        String name = null;
        try {
            final String[] columns = {KEY_NAME};
            final String selection = KEY_ROWID + "=?";
            final String[] selectionArgs = {Long.toString(id)};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
            int idxName = cursor.getColumnIndex(KEY_NAME);
            if (cursor.moveToFirst()) {
                name = cursor.getString(idxName);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return name;
    }

    public void update(DataNote item) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_NAME, item.name);
            values.put(KEY_TYPE, item.type.ordinal());
            values.put(KEY_VALUE, item.value);
            values.put(KEY_SERVER_ID, item.server_id);
            String where = KEY_ROWID + "=?";
            String[] whereArgs = {Long.toString(item.id)};
            mDb.update(TABLE_NAME, values, where, whereArgs);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public void updateValue(DataNote item) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_VALUE, item.value);
            String where = KEY_ROWID + "=?";
            String[] whereArgs = {Long.toString(item.id)};
            mDb.update(TABLE_NAME, values, where, whereArgs);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public void removeTest() {
        try {
            String where = KEY_IS_TEST + "=1";
            mDb.delete(TABLE_NAME, where, null);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }
}

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

    static final String KEY_ROWID     = "_id";
    static final String KEY_NAME      = "name";
    static final String KEY_SERVER_ID = "server_id";
    static final String KEY_CHECKED   = "is_checked";
    static final String KEY_LOCAL     = "is_local";

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
        sbuf.append(KEY_NAME);
        sbuf.append(" text, ");
        sbuf.append(KEY_SERVER_ID);
        sbuf.append(" int, ");
        sbuf.append(KEY_CHECKED);
        sbuf.append(" bit, ");
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

    public int countChecked() {
        int count = 0;
        try {
            final String selection = KEY_CHECKED + "=1";
            Cursor cursor = mDb.query(TABLE_NAME, null, selection, null, null, null, null);
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
                values.put(KEY_CHECKED, value.isChecked ? 1 : 0);
                values.put(KEY_LOCAL, value.isLocal ? 1 : 0);
                values.put(KEY_SERVER_ID, value.server_id);
                value.id = mDb.insert(TABLE_NAME, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public void add(DataEquipment item) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_NAME, item.name);
            values.put(KEY_CHECKED, item.isChecked ? 1 : 0);
            values.put(KEY_LOCAL, item.isLocal ? 1 : 0);
            values.put(KEY_SERVER_ID, item.server_id);
            item.id = mDb.insert(TABLE_NAME, null, values);
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
        DataEquipment item = null;
        final String[] columns = {KEY_NAME, KEY_CHECKED, KEY_LOCAL, KEY_SERVER_ID};
        final String selection = KEY_ROWID + "=?";
        final String[] selectionArgs = {Long.toString(id)};
        Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
        final int idxName = cursor.getColumnIndex(KEY_NAME);
        final int idxChecked = cursor.getColumnIndex(KEY_CHECKED);
        final int idxLocal = cursor.getColumnIndex(KEY_LOCAL);
        final int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(idxName);
            boolean checked = cursor.getShort(idxChecked) != 0 ? true : false;
            boolean local = cursor.getShort(idxLocal) != 0 ? true : false;
            item = new DataEquipment(id, name, checked, local);
            item.server_id = cursor.getInt(idxServerId);
        }
        cursor.close();
        return item;
    }

    public List<DataEquipment> query() {
        ArrayList<DataEquipment> list = new ArrayList();
        final String[] columns = {KEY_ROWID, KEY_NAME, KEY_CHECKED, KEY_LOCAL, KEY_SERVER_ID};
        Cursor cursor = mDb.query(TABLE_NAME, columns, null, null, null, null, null, null);
        final int idxName = cursor.getColumnIndex(KEY_NAME);
        final int idxChecked = cursor.getColumnIndex(KEY_CHECKED);
        final int idxLocal = cursor.getColumnIndex(KEY_LOCAL);
        final int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
        final int idxRowId = cursor.getColumnIndex(KEY_ROWID);
        DataEquipment item;
        while (cursor.moveToNext()) {
            item = new DataEquipment(
                    cursor.getLong(idxRowId),
                    cursor.getString(idxName),
                    cursor.getShort(idxChecked) != 0,
                    cursor.getShort(idxLocal) != 0);
            item.server_id = cursor.getInt(idxServerId);
            list.add(item);
        }
        cursor.close();
        return list;
    }

    public DataEquipment queryByServerId(int server_id) {
        DataEquipment item = null;
        try {
            final String[] columns = {KEY_ROWID, KEY_NAME, KEY_CHECKED, KEY_LOCAL};
            final String selection = KEY_SERVER_ID + "=?";
            final String[] selectionArgs = {Integer.toString(server_id)};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            final int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            final int idxLocal = cursor.getColumnIndex(KEY_LOCAL);
            final int idxName = cursor.getColumnIndex(KEY_NAME);
            final int idxChecked = cursor.getColumnIndex(KEY_CHECKED);
            if (cursor.moveToFirst()) {
                item = new DataEquipment(
                        cursor.getLong(idxRowId),
                        cursor.getString(idxName),
                        cursor.getShort(idxChecked) != 0,
                        cursor.getShort(idxLocal) != 0);
                item.server_id = server_id;
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return item;
    }

    public List<Long> queryChecked() {
        ArrayList<Long> list = new ArrayList();
        try {
            final String[] columns = {KEY_ROWID};
            final String selection = KEY_CHECKED + "=1";
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, null, null, null, null, null);
            final int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            while (cursor.moveToNext()) {
                list.add(cursor.getLong(idxRowId));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public long query(String name) {
        long id = -1L;
        final String[] columns = {KEY_ROWID, KEY_CHECKED, KEY_LOCAL};
        final String selection = KEY_NAME + "=?";
        final String[] selectionArgs = {name};
        Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
        final int idxRowId = cursor.getColumnIndex(KEY_ROWID);
        if (cursor.moveToFirst()) {
            id = cursor.getLong(idxRowId);
        }
        cursor.close();
        return id;
    }

    public void setChecked(DataEquipment item, boolean flag) {
        item.isChecked = flag;
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            String where = KEY_ROWID + "=?";
            String[] whereArgs = {Long.toString(item.id)};
            values.put(KEY_CHECKED, item.isChecked ? 1 : 0);
            mDb.update(TABLE_NAME, values, where, whereArgs);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public void clearChecked() {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_CHECKED, 0);
            mDb.update(TABLE_NAME, values, null, null);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public void update(DataEquipment item) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, item.name);
            values.put(KEY_LOCAL, item.isLocal ? 1 : 0);
            values.put(KEY_CHECKED, item.isChecked ? 1 : 0);
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
}

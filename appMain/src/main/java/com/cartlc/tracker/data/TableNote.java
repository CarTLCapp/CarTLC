/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cartlc.tracker.app.TBApplication;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 4/17/17.
 */

public class TableNote {
    static final String TABLE_NAME = "list_notes";

    static final String KEY_ROWID      = "_id";
    static final String KEY_NAME       = "name";
    static final String KEY_VALUE      = "value";
    static final String KEY_TYPE       = "type";
    static final String KEY_NUM_DIGITS = "num_digits";
    static final String KEY_SERVER_ID  = "server_id";
    static final String KEY_IS_BOOT    = "is_boot_strap";

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
        sbuf.append(KEY_NUM_DIGITS);
        sbuf.append(" smallint default 0, ");
        sbuf.append(KEY_SERVER_ID);
        sbuf.append(" int, ");
        sbuf.append(KEY_IS_BOOT);
        sbuf.append(" bit default 0)");
        mDb.execSQL(sbuf.toString());
    }

    public static void upgrade3(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEY_NUM_DIGITS + " smallint default 0");
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableNote.class, "upgrade3()", "db");
        }
    }

    public void clear() {
        try {
            mDb.delete(TABLE_NAME, null, null);
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableNote.class, "clear()", "db");
        }
    }

    public int count() {
        int count = 0;
        try {
            Cursor cursor = mDb.query(TABLE_NAME, null, null, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableNote.class, "count()", "db");
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
                values.put(KEY_NUM_DIGITS, value.num_digits);
                values.put(KEY_SERVER_ID, value.serverId);
                values.put(KEY_IS_BOOT, value.isBootStrap ? 1 : 0);
                mDb.insert(TABLE_NAME, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableNote.class, "add(list)", "db");
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
            values.put(KEY_NUM_DIGITS, item.num_digits);
            values.put(KEY_SERVER_ID, item.serverId);
            values.put(KEY_IS_BOOT, item.isBootStrap ? 1 : 0);
            item.id = mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableNote.class, "add(item)", "db");
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
            TBApplication.ReportError(ex, TableNote.class, "clearValues()", "db");
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
            TBApplication.ReportError(ex, TableNote.class, "query()", name);
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

    public List<DataNote> query(String selection, String[] selectionArgs) {
        List<DataNote> list = new ArrayList();
        try {
            Cursor cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            int idxName = cursor.getColumnIndex(KEY_NAME);
            int idxValue = cursor.getColumnIndex(KEY_VALUE);
            int idxType = cursor.getColumnIndex(KEY_TYPE);
            int idxNumDigits = cursor.getColumnIndex(KEY_NUM_DIGITS);
            int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
            int idxTest = cursor.getColumnIndex(KEY_IS_BOOT);
            while (cursor.moveToNext()) {
                DataNote item = new DataNote();
                item.id = cursor.getLong(idxRowId);
                item.name = cursor.getString(idxName);
                item.value = cursor.getString(idxValue);
                item.type = DataNote.Type.from(cursor.getInt(idxType));
                item.num_digits = cursor.getShort(idxNumDigits);
                item.serverId = cursor.getInt(idxServerId);
                item.isBootStrap = cursor.getShort(idxTest) != 0;
                list.add(item);
            }
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableNote.class, "query(selection)", "db");
        }
        return list;
    }

//    public String getName(long id) {
//        String name = null;
//        try {
//            final String[] columns = {KEY_NAME};
//            final String selection = KEY_ROWID + "=?";
//            final String[] selectionArgs = {Long.toString(id)};
//            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
//            int idxName = cursor.getColumnIndex(KEY_NAME);
//            if (cursor.moveToFirst()) {
//                name = cursor.getString(idxName);
//            }
//            cursor.close();
//        } catch (Exception ex) {
//            Timber.e(ex);
//        }
//        return name;
//    }

    public void update(DataNote item) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_NAME, item.name);
            values.put(KEY_TYPE, item.type.ordinal());
            values.put(KEY_VALUE, item.value);
            values.put(KEY_SERVER_ID, item.serverId);
            values.put(KEY_NUM_DIGITS, item.num_digits);
            String where = KEY_ROWID + "=?";
            String[] whereArgs = {Long.toString(item.id)};
            mDb.update(TABLE_NAME, values, where, whereArgs);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableNote.class, "update(item)", "db");
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
            TBApplication.ReportError(ex, TableNote.class, "updateValue()", "db");
        } finally {
            mDb.endTransaction();
        }
    }

    void remove(long id) {
        String where = KEY_ROWID + "=?";
        String[] whereArgs = {Long.toString(id)};
        mDb.delete(TABLE_NAME, where, whereArgs);
    }

    public void removeIfUnused(DataNote note) {
        if (TableCollectionNoteEntry.getInstance().countNotes(note.id) == 0) {
            Timber.i("remove(" + note.id + ", " + note.name + ")");
            remove(note.id);
        } else {
            Timber.i("Did not remove unused note because some entries are using it: " + note.toString());
        }
    }

    public void clearUploaded() {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_SERVER_ID, 0);
            if (mDb.update(TABLE_NAME, values, null, null) == 0) {
                Timber.e("TableNote.clearUploaded(): Unable to update entries");
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableNote.class, "clearUploaded()", "db");
        } finally {
            mDb.endTransaction();
        }
    }

}

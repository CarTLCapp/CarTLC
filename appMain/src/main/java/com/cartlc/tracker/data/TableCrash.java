package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 8/16/17.
 */

public class TableCrash {

    public static class CrashLine {
        public long    id;
        public int     code;
        public String  message;
        public String  trace;
        public long    date;
        public boolean uploaded;
    }

    static final String TABLE_NAME = "table_crash";

    static final String KEY_ROWID    = "_id";
    static final String KEY_DATE     = "date";
    static final String KEY_CODE     = "code";
    static final String KEY_MESSAGE  = "message";
    static final String KEY_TRACE    = "trace";
    static final String KEY_UPLOADED = "uploaded";

    static TableCrash sInstance;

    static void Init(SQLiteDatabase db) {
        new TableCrash(db);
    }

    public static TableCrash getInstance() {
        return sInstance;
    }

    final SQLiteDatabase mDb;

    TableCrash(SQLiteDatabase db) {
        sInstance = this;
        this.mDb = db;
    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(TABLE_NAME);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_DATE);
        sbuf.append(" long, ");
        sbuf.append(KEY_CODE);
        sbuf.append(" smallint, ");
        sbuf.append(KEY_MESSAGE);
        sbuf.append(" text, ");
        sbuf.append(KEY_TRACE);
        sbuf.append(" text, ");
        sbuf.append(KEY_UPLOADED);
        sbuf.append(" bit default 0)");
        mDb.execSQL(sbuf.toString());
    }

    public void clearUploaded() {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_UPLOADED, 0);
            if (mDb.update(TABLE_NAME, values, null, null) == 0) {
                Timber.e("Unable to update crash entries");
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public List<CrashLine> queryNeedsUploading() {
        final String orderBy = KEY_DATE + " DESC";
        String where = KEY_UPLOADED + "=0";
        Cursor cursor = mDb.query(TABLE_NAME, null, where, null, null, null, orderBy, null);
        int idxRow = cursor.getColumnIndex(KEY_ROWID);
        int idxDate = cursor.getColumnIndex(KEY_DATE);
        int idxCode = cursor.getColumnIndex(KEY_CODE);
        int idxMessage = cursor.getColumnIndex(KEY_MESSAGE);
        int idxTrace = cursor.getColumnIndex(KEY_TRACE);
        int idxUploaded = cursor.getColumnIndex(KEY_UPLOADED);
        ArrayList<CrashLine> lines = new ArrayList();
        while (cursor.moveToNext()) {
            CrashLine line = new CrashLine();
            line.id = cursor.getLong(idxRow);
            line.date = cursor.getLong(idxDate);
            line.code = cursor.getShort(idxCode);
            line.message = cursor.getString(idxMessage);
            line.trace = cursor.getString(idxTrace);
            line.uploaded = cursor.getShort(idxUploaded) != 0;
            lines.add(line);
        }
        return lines;
    }

    public void message(int code, String message, String trace) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_DATE, System.currentTimeMillis());
            values.put(KEY_CODE, code);
            values.put(KEY_MESSAGE, message);
            values.put(KEY_TRACE, trace);
            mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public void setUploaded(CrashLine line) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_UPLOADED, 1);
            String where = KEY_ROWID + "=?";
            String[] whereArgs = {Long.toString(line.id)};
            if (mDb.update(TABLE_NAME, values, where, whereArgs) == 0) {
                Timber.e("Unable to update crash entry");
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }
}

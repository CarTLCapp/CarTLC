package com.fleettlc.trackbattery.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import timber.log.Timber;

import java.util.ArrayList;

/**
 * Created by dug on 4/14/17.
 */

public class TableString {
    static final String KEY_ROWID = "_id";
    static final String KEY_VALUE = "value";

    protected final SQLiteDatabase db;
    protected final String tableName;

    protected TableString(SQLiteDatabase db, String tableName) {
        this.tableName = tableName;
        this.db = db;
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(tableName);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_VALUE);
        sbuf.append(" text not null)");
        db.execSQL(sbuf.toString());
    }

    public void clear() {
        db.delete(tableName, null, null);
    }

    public void add(ArrayList<String> list) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (String value : list) {
                values.clear();
                values.put(KEY_VALUE, value);
                db.insert(tableName, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            db.endTransaction();
        }
    }

    public ArrayList<String> query() {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_VALUE};
            final String orderBy = KEY_VALUE + " ASC";
            Cursor cursor = db.query(tableName, columns, null, null, null, null, orderBy);
            while (cursor.moveToNext()) {
                list.add(cursor.getString(0));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }
}

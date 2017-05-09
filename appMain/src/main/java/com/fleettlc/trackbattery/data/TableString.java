package com.fleettlc.trackbattery.data;

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
    static final String KEY_FATHER = "father";

    protected final SQLiteDatabase db;
    protected final String tableName;

    protected TableString(SQLiteDatabase db, String tableName) {
        this.tableName = tableName;
        this.db = db;
    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(tableName);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_VALUE);
        sbuf.append(" text not null, ");
        sbuf.append(KEY_FATHER);
        sbuf.append(" integer)");
        db.execSQL(sbuf.toString());
    }

    public void clear() {
        db.delete(tableName, null, null);
    }

    public void add(List<Entry> list) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (Entry entry : list) {
                values.clear();
                values.put(KEY_VALUE, entry.value);
                values.put(KEY_FATHER, entry.father);
                db.insert(tableName, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            db.endTransaction();
        }
    }

    public void addStrings(List<String> list) {
        ArrayList<Entry> entries = new ArrayList();
        for (String value : list) {
            entries.add(new Entry(value));
        }
        add(entries);
    }

    public List<String> query() {
        return query(null);
    }

    public List<String> query(Integer father) {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_VALUE};
            final String orderBy = KEY_VALUE + " ASC";
            String where;
            String[] whereArgs;
            if (father == null) {
                where = null;
                whereArgs = null;
            } else {
                where = KEY_FATHER + "=?";
                whereArgs = new String[]{String.valueOf(father)};
            }
            Cursor cursor = db.query(tableName, columns, where, whereArgs, null, null, orderBy);
            int idxValue = cursor.getColumnIndex(KEY_VALUE);
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

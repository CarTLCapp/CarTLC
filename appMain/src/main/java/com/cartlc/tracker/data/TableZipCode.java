package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import timber.log.Timber;

/**
 * Created by dug on 8/24/17.
 */

public class TableZipCode {

    static final String TABLE_NAME = "zipcodes";

    static final String KEY_ROWID       = "_id";
    static final String KEY_ZIPCODE     = "zipcode";
    static final String KEY_STATE_LONG  = "state_long";
    static final String KEY_STATE_SHORT = "state_short";
    static final String KEY_CITY        = "city";

    final SQLiteDatabase mDb;

    static TableZipCode sInstance;

    static void Init(SQLiteDatabase db) {
        new TableZipCode(db);
    }

    public static TableZipCode getInstance() {
        return sInstance;
    }

    TableZipCode(SQLiteDatabase db) {
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
        sbuf.append(KEY_ZIPCODE);
        sbuf.append(" varchar(64), ");
        sbuf.append(KEY_STATE_LONG);
        sbuf.append(" varchar(64), ");
        sbuf.append(KEY_STATE_SHORT);
        sbuf.append(" varchar(16), ");
        sbuf.append(KEY_CITY);
        sbuf.append(" varchar(1024))");
        mDb.execSQL(sbuf.toString());
    }

    public void add(DataZipCode data) {
        if (query(data.zipCode) != null) {
            return;
        }
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ZIPCODE, data.zipCode);
            values.put(KEY_STATE_LONG, data.stateLongName);
            values.put(KEY_STATE_SHORT, data.stateShortName);
            values.put(KEY_CITY, data.city);
            mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public DataZipCode query(String zipCode) {
        if (zipCode == null) {
            return null;
        }
        DataZipCode data = null;
        try {
            String selection = KEY_ZIPCODE + "=?";
            String[] selectionArgs = new String[]{zipCode};
            Cursor cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
            if (cursor.getCount() > 0) {
                int idxZipCode = cursor.getColumnIndex(KEY_ZIPCODE);
                int idxStateLong = cursor.getColumnIndex(KEY_STATE_LONG);
                int idxStateShort = cursor.getColumnIndex(KEY_STATE_SHORT);
                int idxCity = cursor.getColumnIndex(KEY_CITY);
                if (cursor.moveToNext()) {
                    data = new DataZipCode();
                    data.zipCode = cursor.getString(idxZipCode);
                    data.stateShortName = cursor.getString(idxStateShort);
                    data.stateLongName = cursor.getString(idxStateLong);
                    data.city = cursor.getString(idxCity);
                }
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return data;
    }

    public String queryState(String zipCode) {
        DataZipCode data = query(zipCode);
        if (data != null) {
            return data.stateLongName;
        }
        return null;
    }

    public String queryCity(String zipCode) {
        DataZipCode data = query(zipCode);
        if (data != null) {
            return data.city;
        }
        return null;
    }
}

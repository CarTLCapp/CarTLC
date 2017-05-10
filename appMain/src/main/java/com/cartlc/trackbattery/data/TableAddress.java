package com.cartlc.trackbattery.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */

public class TableAddress {

    static TableAddress sInstance;

    static void Init(SQLiteDatabase db) {
        new TableAddress(db);
    }

    public static TableAddress getInstance() {
        return sInstance;
    }

    static final String TABLE_NAME = "list_address";

    static final String KEY_ROWID = "_id";
    static final String KEY_COMPANY = "company";
    static final String KEY_LOCATION = "location";
    static final String KEY_CITY = "city";
    static final String KEY_STATE = "state";

    final SQLiteDatabase mDb;

    public TableAddress(SQLiteDatabase db) {
        sInstance = this;
        this.mDb = db;
    }

    public void clear() {
        mDb.delete(TABLE_NAME, null, null);
    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(TABLE_NAME);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_COMPANY);
        sbuf.append(" text, ");
        sbuf.append(KEY_LOCATION);
        sbuf.append(" text, ");
        sbuf.append(KEY_CITY);
        sbuf.append(" text, ");
        sbuf.append(KEY_STATE);
        sbuf.append(" text)");
        mDb.execSQL(sbuf.toString());
    }

    public void add(List<Address> list) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (Address address : list) {
                values.clear();
                values.put(KEY_COMPANY, address.company);
                values.put(KEY_LOCATION, address.location);
                values.put(KEY_CITY, address.city);
                values.put(KEY_STATE, address.state);
                mDb.insert(TABLE_NAME, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public List<String> queryStates() {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_STATE};
            final String orderBy = KEY_STATE + " ASC";
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, null, null, null, null, orderBy, null);
            int idxValue = cursor.getColumnIndex(KEY_STATE);
            String state;
            while (cursor.moveToNext()) {
                state = cursor.getString(idxValue);
                list.add(state);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public List<String> queryCities(String state) {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_CITY};
            final String orderBy = KEY_CITY + " ASC";
            final String selection = KEY_STATE + " =?";
            final String[] selectionArgs = {state};
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy, null);
            int idxValue = cursor.getColumnIndex(KEY_CITY);
            while (cursor.moveToNext()) {
                list.add(cursor.getString(idxValue));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public List<String> queryCompanies(String state, String city) {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_COMPANY};
            final String orderBy = KEY_COMPANY + " ASC";
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(KEY_STATE);
            sbuf.append(" =? AND ");
            sbuf.append(KEY_CITY);
            sbuf.append(" =?");
            final String selection = sbuf.toString();
            final String[] selectionArgs = {state, city};
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy, null);
            int idxValue = cursor.getColumnIndex(KEY_COMPANY);
            while (cursor.moveToNext()) {
                list.add(cursor.getString(idxValue));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public List<String> queryLocations(String state, String city, String company) {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_LOCATION};
            final String orderBy = KEY_LOCATION + " ASC";
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(KEY_STATE);
            sbuf.append(" =? AND ");
            sbuf.append(KEY_CITY);
            sbuf.append(" =? AND ");
            sbuf.append(KEY_COMPANY);
            sbuf.append(" =?");
            final String selection = sbuf.toString();
            final String[] selectionArgs = {state, city, company};
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy, null);
            int idxValue = cursor.getColumnIndex(KEY_LOCATION);
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

package com.cartlc.trackbattery.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 4/17/17.
 */

public class TableEntries {

    static final String TABLE_NAME = "table_entries";

    static final String KEY_ROWID = "_id";
    static final String KEY_DATE = "date";
    static final String KEY_PROJECT_ID = "project_id";
    static final String KEY_ADDRESS_ID = "address_id";
    static final String KEY_EQUIPMENT_COLLECTION_ID = "requipment_collection_id";
    static final String KEY_PICTURE_COLLECTION_ID = "picture_collection_id";
    static final String KEY_TRUCK_ID = "truck_id";
    static final String KEY_NOTES_ID = "notes_id";

    static TableEntries sInstance;

    static void Init(SQLiteDatabase db) {
        new TableEntries(db);
    }

    public static TableEntries getInstance() {
        return sInstance;
    }

    final SQLiteDatabase mDb;

    TableEntries(SQLiteDatabase db) {
        sInstance = this;
        this.mDb = db;
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
        sbuf.append(KEY_DATE);
        sbuf.append(" long, ");
        sbuf.append(KEY_PROJECT_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_ADDRESS_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_EQUIPMENT_COLLECTION_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_PICTURE_COLLECTION_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_TRUCK_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_NOTES_ID);
        sbuf.append(" long)");
        mDb.execSQL(sbuf.toString());
    }

    public List<Long> queryProjects() {
        ArrayList<Long> list = new ArrayList();
        try {
            final String[] columns = {KEY_PROJECT_ID};
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, null, null, null, null, null, null);
            int idxValue = cursor.getColumnIndex(KEY_PROJECT_ID);
            while (cursor.moveToNext()) {
                list.add(cursor.getLong(idxValue));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }

        return list;
    }

    public int count(long projectId) {
        int count = 0;
        try {
            final String selection = KEY_PROJECT_ID + " =?";
            final String[] selectionArgs = {Long.toString(projectId)};
            Cursor cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return count;
    }
}

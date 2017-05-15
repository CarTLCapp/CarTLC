package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */

public class TableEquipmentCollection {

    static TableEquipmentCollection sInstance;

    static void Init(SQLiteDatabase db) {
        new TableEquipmentCollection(db);
    }

    public static TableEquipmentCollection getInstance() {
        return sInstance;
    }

    static final String TABLE_NAME = "equipment_collection";

    static final String KEY_ROWID = "_id";
    static final String KEY_COLLECTION_ID = "collection_id";
    static final String KEY_EQUIPMENT_ID = "equipment_id";

    final SQLiteDatabase mDb;

    public TableEquipmentCollection(SQLiteDatabase db) {
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
        sbuf.append(KEY_COLLECTION_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_EQUIPMENT_ID);
        sbuf.append(" long)");
        mDb.execSQL(sbuf.toString());
    }

    public void add(DataEquipmentCollection collection) {
        ContentValues values = new ContentValues();
        for (Long equipmentId : collection.equipmentListIds) {
            values.clear();
            values.put(KEY_COLLECTION_ID, collection.id);
            values.put(KEY_EQUIPMENT_ID, equipmentId);
            mDb.insert(TABLE_NAME, null, values);
        }
    }

    public DataEquipmentCollection query(long collection_id) {
        DataEquipmentCollection collection = null;
        try {
            final String[] columns = {KEY_EQUIPMENT_ID};
            final String selection = KEY_COLLECTION_ID + " =?";
            final String[] selectionArgs = {Long.toString(collection_id)};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            int idxValue = cursor.getColumnIndex(KEY_EQUIPMENT_ID);
            collection = new DataEquipmentCollection(collection_id);
            while (cursor.moveToNext()) {
                collection.add(cursor.getLong(idxValue));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return collection;
    }

}

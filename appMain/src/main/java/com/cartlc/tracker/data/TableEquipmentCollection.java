package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */

public abstract class TableEquipmentCollection {

    static final String KEY_ROWID        = "_id";
    static final String KEY_OTHER_ID     = "other_id";
    static final String KEY_EQUIPMENT_ID = "equipment_id";

    final SQLiteDatabase mDb;
    final String         mTableName;

    public TableEquipmentCollection(SQLiteDatabase db, String tableName) {
        this.mTableName = tableName;
        this.mDb = db;
    }

    public void clear() {
        try {
            mDb.delete(mTableName, null, null);
        } catch (Exception ex) {
        }
    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(mTableName);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_OTHER_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_EQUIPMENT_ID);
        sbuf.append(" long)");
        mDb.execSQL(sbuf.toString());
    }


    public int count() {
        int count = 0;
        try {
            Cursor cursor = mDb.query(mTableName, null, null, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return count;
    }

    public void add(DataEquipmentCollection collection) {
        add(collection.id, collection.equipmentListIds);
    }

    public void add(long collectionId, List<Long> equipmentIds) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (Long equipmentId : equipmentIds) {
                values.clear();
                values.put(KEY_OTHER_ID, collectionId);
                values.put(KEY_EQUIPMENT_ID, equipmentId);
                mDb.insert(mTableName, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public void addByName(long collectionId, List<String> equipments) {
        List<Long> list = new ArrayList();
        for (String equipname : equipments) {
            long id = TableEquipment.getInstance().query(equipname);
            if (id < 0) {
                id = TableEquipment.getInstance().add(equipname);
            }
            list.add(id);
        }
        add(collectionId, list);
    }

    public List<Long> query(long other_id) {
        List<Long> collection = new ArrayList();
        try {
            final String[] columns       = {KEY_EQUIPMENT_ID};
            final String   selection     = KEY_OTHER_ID + " =?";
            final String[] selectionArgs = {Long.toString(other_id)};
            Cursor         cursor        = mDb.query(mTableName, columns, selection, selectionArgs, null, null, null, null);
            int            idxValue      = cursor.getColumnIndex(KEY_EQUIPMENT_ID);
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

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

public class TableEntries {

    static final String TABLE_NAME = "table_entries";

    static final String KEY_ROWID = "_id";
    static final String KEY_DATE = "date";
    static final String KEY_PROJECT_ID = "project_id";
    static final String KEY_ADDRESS_ID = "address_id";
    static final String KEY_EQUIPMENT_COLLECTION_ID = "requipment_collection_id";
    static final String KEY_PICTURE_COLLECTION_ID = "picture_collection_id";
    static final String KEY_TRUCK_NUMBER = "truck_number";
    static final String KEY_NOTES_ID = "notes_id";
    static final String KEY_UPLOADED = "uploaded";

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
        sbuf.append(KEY_TRUCK_NUMBER);
        sbuf.append(" int, ");
        sbuf.append(KEY_NOTES_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_UPLOADED);
        sbuf.append(" bit)");
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

    public List<DataEntry> queryPendingUploaded() {
        String where = KEY_UPLOADED + "=1";
        return query(where, null);
    }

    List<DataEntry> query(String where, String[] whereArgs) {
        ArrayList<DataEntry> list = new ArrayList();
        try {
            final String[] columns = {KEY_ROWID,
                    KEY_DATE, KEY_PROJECT_ID, KEY_ADDRESS_ID, KEY_EQUIPMENT_COLLECTION_ID,
                    KEY_PICTURE_COLLECTION_ID, KEY_TRUCK_NUMBER, KEY_NOTES_ID, KEY_UPLOADED};
            final String orderBy = KEY_DATE + " DESC";
            Cursor cursor = mDb.query(TABLE_NAME, columns, where, whereArgs, null, null, orderBy, null);
            int idxRow = cursor.getColumnIndex(KEY_ROWID);
            int idxProjectNameId = cursor.getColumnIndex(KEY_PROJECT_ID);
            int idxAddress = cursor.getColumnIndex(KEY_ADDRESS_ID);
            int idxEquipmentCollectionId = cursor.getColumnIndex(KEY_EQUIPMENT_COLLECTION_ID);
            int idxPictureCollectionId = cursor.getColumnIndex(KEY_PICTURE_COLLECTION_ID);
            int idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER);
            int idxNotesId = cursor.getColumnIndex(KEY_NOTES_ID);
            int idxUploaded = cursor.getColumnIndex(KEY_UPLOADED);
            DataEntry entry;
            while (cursor.moveToNext()) {
                entry = new DataEntry();
                entry.id = cursor.getLong(idxRow);
                entry.projectNameId = cursor.getLong(idxProjectNameId);
                entry.addressId = cursor.getLong(idxAddress);
                long equipmentCollectionId = cursor.getLong(idxEquipmentCollectionId);
                entry.equipmentCollection = new DataEquipmentCollection(equipmentCollectionId, entry.projectNameId);
                entry.pictureCollectionId = cursor.getLong(idxPictureCollectionId);
                entry.truckNumber = cursor.getInt(idxTruckNumber);
                entry.notesId = cursor.getLong(idxNotesId);
                entry.uploaded = cursor.getShort(idxUploaded) != 0;
                list.add(entry);
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

    public int countUploaded(long projectId) {
        int count = 0;
        try {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(KEY_PROJECT_ID);
            sbuf.append("=? AND ");
            sbuf.append(KEY_UPLOADED);
            sbuf.append("=1");
            final String selection = sbuf.toString();
            final String[] selectionArgs = {Long.toString(projectId)};
            Cursor cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return count;
    }

    public void add(DataEntry entry) {
        mDb.beginTransaction();
        try {
            TableEquipmentCollection.getInstance().add(entry.equipmentCollection);
            PrefHelper.getInstance().incNextEquipmentCollectionID();

            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_DATE, entry.date);
            values.put(KEY_PROJECT_ID, entry.projectNameId);
            values.put(KEY_ADDRESS_ID, entry.addressId);
            values.put(KEY_EQUIPMENT_COLLECTION_ID, entry.equipmentCollection.id);
            values.put(KEY_TRUCK_NUMBER, entry.truckNumber);
            values.put(KEY_NOTES_ID, entry.notesId);
            mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public void setUploaded(DataEntry entry) {
        try {
            entry.uploaded = true;
            ContentValues values = new ContentValues();
            values.put(KEY_UPLOADED, entry.uploaded ? 1 : 0);
            String where = KEY_ROWID + "=?";
            String[] whereArgs = {Long.toString(entry.id)};
            if (mDb.update(TABLE_NAME, values, where, whereArgs) == 0) {
                Timber.e("Unable to update entry");
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }
}

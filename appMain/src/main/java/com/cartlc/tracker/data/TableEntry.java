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

public class TableEntry {

    public static class Count {
        public long comboId;
        public int totalEntries        = 0;
        public int totalUploadedAws    = 0;
        public int totalUploadedMaster = 0;

        public Count(long id) {
            comboId = id;
        }

        public boolean uploadedAll() {
            return totalUploadedAws >= totalEntries && totalUploadedMaster >= totalEntries;
        }
    }

    static final String TABLE_NAME = "table_entries";

    static final String KEY_ROWID                    = "_id";
    static final String KEY_DATE                     = "date";
    static final String KEY_PROJECT_ADDRESS_COMBO_ID = "combo_id";
    static final String KEY_EQUIPMENT_COLLECTION_ID  = "equipment_collection_id";
    static final String KEY_PICTURE_COLLECTION_ID    = "picture_collection_id";
    static final String KEY_NOTE_COLLECTION_ID       = "note_collection_id";
    static final String KEY_TRUCK_NUMBER             = "truck_number";
    static final String KEY_LICENSE_PLATE            = "license_plate";
    static final String KEY_UPLOADED_MASTER          = "uploaded_master";
    static final String KEY_UPLOADED_AWS             = "uploaded_aws";

    static TableEntry sInstance;

    static void Init(SQLiteDatabase db) {
        new TableEntry(db);
    }

    public static TableEntry getInstance() {
        return sInstance;
    }

    final SQLiteDatabase mDb;

    TableEntry(SQLiteDatabase db) {
        sInstance = this;
        this.mDb = db;
    }

    public static void upgrade(SQLiteDatabase db) {
        Timber.d("MYDEBUG: upgrade()");
        try {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEY_LICENSE_PLATE + " text");
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    public void clear() {
        try {
            mDb.delete(TABLE_NAME, null, null);
        } catch (Exception ex) {
        }
    }

    public void drop() {
        mDb.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
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
        sbuf.append(KEY_PROJECT_ADDRESS_COMBO_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_EQUIPMENT_COLLECTION_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_PICTURE_COLLECTION_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_NOTE_COLLECTION_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_TRUCK_NUMBER);
        sbuf.append(" int, ");
        sbuf.append(KEY_LICENSE_PLATE);
        sbuf.append(" text, ");
        sbuf.append(KEY_UPLOADED_MASTER);
        sbuf.append(" bit default 0, ");
        sbuf.append(KEY_UPLOADED_AWS);
        sbuf.append(" bit default 0)");
        mDb.execSQL(sbuf.toString());
    }

    public List<DataEntry> queryPendingDataToUploadToMaster() {
        String where = KEY_UPLOADED_MASTER + "=0";
        return query(where, null);
    }


    public List<DataEntry> queryPendingPicturesToUpload() {
        String where = KEY_UPLOADED_AWS + "=0";
        return query(where, null);
    }

    List<DataEntry> query(String where, String[] whereArgs) {
        ArrayList<DataEntry> list = new ArrayList();
        try {
            final String[] columns = {KEY_ROWID,
                    KEY_DATE, KEY_PROJECT_ADDRESS_COMBO_ID, KEY_EQUIPMENT_COLLECTION_ID,
                    KEY_PICTURE_COLLECTION_ID, KEY_NOTE_COLLECTION_ID, KEY_TRUCK_NUMBER, KEY_LICENSE_PLATE, KEY_UPLOADED_MASTER, KEY_UPLOADED_AWS};
            final String orderBy = KEY_DATE + " DESC";
            Cursor cursor = mDb.query(TABLE_NAME, columns, where, whereArgs, null, null, orderBy, null);
            int idxRow = cursor.getColumnIndex(KEY_ROWID);
            int idxProjectAddressCombo = cursor.getColumnIndex(KEY_PROJECT_ADDRESS_COMBO_ID);
            int idxDate = cursor.getColumnIndex(KEY_DATE);
            int idxEquipmentCollectionId = cursor.getColumnIndex(KEY_EQUIPMENT_COLLECTION_ID);
            int idxPictureCollectionId = cursor.getColumnIndex(KEY_PICTURE_COLLECTION_ID);
            int idxNotetCollectionId = cursor.getColumnIndex(KEY_NOTE_COLLECTION_ID);
            int idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER);
            int idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE);
            int idxUploadedMaster = cursor.getColumnIndex(KEY_UPLOADED_MASTER);
            int idxUploadedAws = cursor.getColumnIndex(KEY_UPLOADED_AWS);
            DataEntry entry;
            while (cursor.moveToNext()) {
                entry = new DataEntry();
                entry.id = cursor.getLong(idxRow);
                entry.date = cursor.getLong(idxDate);
                long projectAddressComboId = cursor.getLong(idxProjectAddressCombo);
                entry.projectAddressCombo = TableProjectAddressCombo.getInstance().query(projectAddressComboId);
                long equipmentCollectionId = cursor.getLong(idxEquipmentCollectionId);
                entry.equipmentCollection = TableCollectionEquipmentEntry.getInstance().queryForCollectionId(equipmentCollectionId);
                long pictureCollectionId = cursor.getLong(idxPictureCollectionId);
                entry.pictureCollection = TablePictureCollection.getInstance().query(pictureCollectionId);
                entry.noteCollectionId = cursor.getLong(idxNotetCollectionId);
                entry.truckNumber = cursor.getInt(idxTruckNumber);
                entry.licensePlateNumber = cursor.getString(idxLicensePlate);
                entry.uploadedMaster = cursor.getShort(idxUploadedMaster) != 0;
                entry.uploadedAws = cursor.getShort(idxUploadedAws) != 0;
                list.add(entry);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public Count countProjectAddressCombo(long comboId) {
        Count count = new Count(comboId);
        try {
            final String selection = KEY_PROJECT_ADDRESS_COMBO_ID + " =?";
            final String[] selectionArgs = {Long.toString(comboId)};
            final String[] columns = {KEY_UPLOADED_AWS, KEY_UPLOADED_MASTER};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            int idxUploadedAws = cursor.getColumnIndex(KEY_UPLOADED_AWS);
            int idxUploadedMaster = cursor.getColumnIndex(KEY_UPLOADED_MASTER);
            while (cursor.moveToNext()) {
                if (cursor.getShort(idxUploadedAws) != 0) {
                    count.totalUploadedAws++;
                }
                if (cursor.getShort(idxUploadedMaster) != 0) {
                    count.totalUploadedMaster++;
                }
                count.totalEntries++;
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return count;
    }

    public int countAddresses(long addressId) {
        int count = 0;
        try {
            final String[] columns = {KEY_PROJECT_ADDRESS_COMBO_ID};
            Cursor cursor = mDb.query(TABLE_NAME, columns, null, null, null, null, null, null);
            int idxProjectAddressCombo = cursor.getColumnIndex(KEY_PROJECT_ADDRESS_COMBO_ID);
            DataProjectAddressCombo projectAddressCombo;
            while (cursor.moveToNext()) {
                long projectAddressComboId = cursor.getLong(idxProjectAddressCombo);
                projectAddressCombo = TableProjectAddressCombo.getInstance().query(projectAddressComboId);
                if (projectAddressCombo.addressId == addressId) {
                    count++;
                }
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return count;
    }

    public int countEquipments(final long equipmentId) {
        int count = 0;
        try {
            final String[] columns = {KEY_EQUIPMENT_COLLECTION_ID};
            Cursor cursor = mDb.query(TABLE_NAME, columns, null, null, null, null, null, null);
            int idxEquipmentCollectionId = cursor.getColumnIndex(KEY_EQUIPMENT_COLLECTION_ID);
            while (cursor.moveToNext()) {
                long equipmentCollectionId = cursor.getLong(idxEquipmentCollectionId);
                DataCollectionEquipmentEntry collection = TableCollectionEquipmentEntry.getInstance().queryForCollectionId(equipmentCollectionId);
                for (long equipId : collection.equipmentListIds) {
                    if (equipId == equipmentId) {
                        count++;
                    }
                }
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return count;
    }


    public int countNotes(final long noteId) {
        int count = 0;
        try {
            final String[] columns = {KEY_NOTE_COLLECTION_ID};
            Cursor cursor = mDb.query(TABLE_NAME, columns, null, null, null, null, null, null);
            int idxNoteCollectionId = cursor.getColumnIndex(KEY_NOTE_COLLECTION_ID);
            while (cursor.moveToNext()) {
                long noteCollectionId = cursor.getLong(idxNoteCollectionId);
                List<DataNote> notes = TableCollectionNoteEntry.getInstance().query(noteCollectionId);
                for (DataNote note : notes) {
                    if (noteId == note.id) {
                        count++;
                    }
                }
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return count;
    }

    public int countProjects(long projectId) {
        int count = 0;
        try {
            final String[] columns = {KEY_PROJECT_ADDRESS_COMBO_ID};
            Cursor cursor = mDb.query(TABLE_NAME, columns, null, null, null, null, null, null);
            int idxProjectAddressCombo = cursor.getColumnIndex(KEY_PROJECT_ADDRESS_COMBO_ID);
            DataProjectAddressCombo projectAddressCombo;
            while (cursor.moveToNext()) {
                long projectAddressComboId = cursor.getLong(idxProjectAddressCombo);
                projectAddressCombo = TableProjectAddressCombo.getInstance().query(projectAddressComboId);
                if (projectAddressCombo.projectNameId == projectId) {
                    count++;
                }
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return count;
    }


    public void add(DataEntry entry) {
        mDb.beginTransaction();
        try {
            TableCollectionEquipmentEntry.getInstance().add(entry.equipmentCollection);
            TablePictureCollection.getInstance().add(entry.pictureCollection);

            PrefHelper.getInstance().incNextEquipmentCollectionID();
            PrefHelper.getInstance().incNextPictureCollectionID();
            PrefHelper.getInstance().incNextNoteCollectionID();

            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_DATE, entry.date);
            values.put(KEY_PROJECT_ADDRESS_COMBO_ID, entry.projectAddressCombo.id);
            values.put(KEY_EQUIPMENT_COLLECTION_ID, entry.equipmentCollection.id);
            values.put(KEY_TRUCK_NUMBER, entry.truckNumber);
            values.put(KEY_LICENSE_PLATE, entry.licensePlateNumber);
            values.put(KEY_NOTE_COLLECTION_ID, entry.noteCollectionId);
            values.put(KEY_PICTURE_COLLECTION_ID, entry.pictureCollection.id);
            mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public void setUploadedMaster(DataEntry entry, boolean flag) {
        entry.uploadedMaster = flag;
        setUploaded(entry, KEY_UPLOADED_MASTER, flag);
    }


    public void setUploadedAws(DataEntry entry, boolean flag) {
        entry.uploadedAws = flag;
        setUploaded(entry, KEY_UPLOADED_AWS, flag);
    }

    void setUploaded(DataEntry entry, String key, boolean flag) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(key, flag ? 1 : 0);
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

    public void clearUploaded() {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_UPLOADED_MASTER, 0);
            values.put(KEY_UPLOADED_AWS, 0);
            if (mDb.update(TABLE_NAME, values, null, null) == 0) {
                Timber.e("Unable to update entries");
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }
}

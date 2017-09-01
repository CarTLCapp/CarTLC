package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cartlc.tracker.etc.PrefHelper;
import com.cartlc.tracker.etc.TruckStatus;

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
    static final String KEY_TRUCK_ID                 = "truck_id";
    static final String KEY_STATUS                   = "status";
    static final String KEY_SERVER_ID                = "server_id";
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

    public void upgrade3() {
        final String TABLE_NAME2 = TABLE_NAME + "_v2";
        final String KEY_TRUCK_NUMBER = "truck_number";
        final String KEY_LICENSE_PLATE = "license_plate";
        try {
            mDb.execSQL("ALTER TABLE " + TABLE_NAME + " RENAME TO " + TABLE_NAME2);
            create();
            Cursor cursor = mDb.query(TABLE_NAME2, null, null, null, null, null, null, null);
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
            int truckNumber;
            String licensePlateNumber;
            ContentValues values = new ContentValues();
            while (cursor.moveToNext()) {
                DataEntry entry = new DataEntry();
                entry.id = cursor.getLong(idxRow);
                entry.date = cursor.getLong(idxDate);
                long projectAddressComboId = cursor.getLong(idxProjectAddressCombo);
                long equipmentCollectionId = cursor.getLong(idxEquipmentCollectionId);
                long pictureCollectionId = cursor.getLong(idxPictureCollectionId);
                entry.noteCollectionId = cursor.getLong(idxNotetCollectionId);
                truckNumber = cursor.getInt(idxTruckNumber);
                if (idxLicensePlate >= 0) {
                    licensePlateNumber = cursor.getString(idxLicensePlate);
                } else {
                    licensePlateNumber = null;
                }
                entry.truckId = TableTruck.getInstance().save(truckNumber, licensePlateNumber);
                entry.uploadedMaster = cursor.getShort(idxUploadedMaster) != 0;
                entry.uploadedAws = cursor.getShort(idxUploadedAws) != 0;

                values.clear();
                values.put(KEY_DATE, entry.date);
                values.put(KEY_PROJECT_ADDRESS_COMBO_ID, projectAddressComboId);
                values.put(KEY_EQUIPMENT_COLLECTION_ID, equipmentCollectionId);
                values.put(KEY_NOTE_COLLECTION_ID, entry.noteCollectionId);
                values.put(KEY_PICTURE_COLLECTION_ID, pictureCollectionId);
                values.put(KEY_TRUCK_ID, entry.truckId);
                values.put(KEY_UPLOADED_AWS, entry.uploadedAws);
                values.put(KEY_UPLOADED_MASTER, entry.uploadedMaster);
                mDb.insert(TABLE_NAME, null, values);

                // Doing this one at a time for safety reasons
                String where = KEY_ROWID + "=?";
                String[] whereArgs = new String[]{Long.toString(entry.id)};
                mDb.delete(TABLE_NAME2, where, whereArgs);
            }
            cursor.close();
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
        sbuf.append(KEY_TRUCK_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_STATUS);
        sbuf.append(" tinyint, ");
        sbuf.append(KEY_SERVER_ID);
        sbuf.append(" int, ");
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
            final String orderBy = KEY_DATE + " DESC";
            Cursor cursor = mDb.query(TABLE_NAME, null, where, whereArgs, null, null, orderBy, null);
            int idxRow = cursor.getColumnIndex(KEY_ROWID);
            int idxProjectAddressCombo = cursor.getColumnIndex(KEY_PROJECT_ADDRESS_COMBO_ID);
            int idxDate = cursor.getColumnIndex(KEY_DATE);
            int idxEquipmentCollectionId = cursor.getColumnIndex(KEY_EQUIPMENT_COLLECTION_ID);
            int idxPictureCollectionId = cursor.getColumnIndex(KEY_PICTURE_COLLECTION_ID);
            int idxNotetCollectionId = cursor.getColumnIndex(KEY_NOTE_COLLECTION_ID);
            int idxTruckId = cursor.getColumnIndex(KEY_TRUCK_ID);
            int idxStatus = cursor.getColumnIndex(KEY_STATUS);
            int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
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
                entry.truckId = cursor.getInt(idxTruckId);
                if (!cursor.isNull(idxStatus)) {
                    entry.status = TruckStatus.from(cursor.getInt(idxStatus));
                }
                entry.server_id = cursor.getInt(idxServerId);
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

//    public int countEquipments(final long equipmentId) {
//        int count = 0;
//        try {
//            final String[] columns = {KEY_EQUIPMENT_COLLECTION_ID};
//            Cursor cursor = mDb.query(TABLE_NAME, columns, null, null, null, null, null, null);
//            int idxEquipmentCollectionId = cursor.getColumnIndex(KEY_EQUIPMENT_COLLECTION_ID);
//            while (cursor.moveToNext()) {
//                long equipmentCollectionId = cursor.getLong(idxEquipmentCollectionId);
//                DataCollectionEquipmentEntry collection = TableCollectionEquipmentEntry.getInstance().queryForCollectionId(equipmentCollectionId);
//                for (long equipId : collection.equipmentListIds) {
//                    if (equipId == equipmentId) {
//                        count++;
//                    }
//                }
//            }
//            cursor.close();
//        } catch (Exception ex) {
//            Timber.e(ex);
//        }
//        return count;
//    }
//
//    public int countNotes(final long noteId) {
//        int count = 0;
//        try {
//            final String[] columns = {KEY_NOTE_COLLECTION_ID};
//            Cursor cursor = mDb.query(TABLE_NAME, columns, null, null, null, null, null, null);
//            int idxNoteCollectionId = cursor.getColumnIndex(KEY_NOTE_COLLECTION_ID);
//            while (cursor.moveToNext()) {
//                long noteCollectionId = cursor.getLong(idxNoteCollectionId);
//                List<DataNote> notes = TableCollectionNoteEntry.getInstance().query(noteCollectionId);
//                for (DataNote note : notes) {
//                    if (noteId == note.id) {
//                        count++;
//                    }
//                }
//            }
//            cursor.close();
//        } catch (Exception ex) {
//            Timber.e(ex);
//        }
//        return count;
//    }

    public int countTrucks(final long truckId) {
        int count = 0;
        try {
            final String[] columns = {KEY_TRUCK_ID};
            final String selection = KEY_TRUCK_ID + "=?";
            final String[] selectionArgs = new String[]{Long.toString(truckId)};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            count = cursor.getCount();
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
            values.put(KEY_TRUCK_ID, entry.truckId);
            values.put(KEY_NOTE_COLLECTION_ID, entry.noteCollectionId);
            values.put(KEY_PICTURE_COLLECTION_ID, entry.pictureCollection.id);
            values.put(KEY_SERVER_ID, entry.server_id);
            if (entry.status != null) {
                values.put(KEY_STATUS, entry.status.ordinal());
            }
            mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    // Right now only used to update a few fields.
    // Later this will be extended to save everything.
    public void save(DataEntry entry) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_SERVER_ID, entry.server_id);
            values.put(KEY_UPLOADED_AWS, entry.uploadedAws ? 1 : 0);
            values.put(KEY_UPLOADED_MASTER, entry.uploadedMaster ? 1 : 0);
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

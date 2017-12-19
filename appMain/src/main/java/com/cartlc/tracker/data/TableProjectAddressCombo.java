package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cartlc.tracker.app.TBApplication;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */

public class TableProjectAddressCombo {

    static TableProjectAddressCombo sInstance;

    static void Init(SQLiteDatabase db) {
        new TableProjectAddressCombo(db);
    }

    public static TableProjectAddressCombo getInstance() {
        return sInstance;
    }

    static final String TABLE_NAME = "list_project_address_combo";

    static final String KEY_ROWID      = "_id";
    static final String KEY_PROJECT_ID = "project_id";
    static final String KEY_ADDRESS_ID = "address_id";
    static final String KEY_LAST_USED  = "last_used";

    final SQLiteDatabase mDb;

    public TableProjectAddressCombo(SQLiteDatabase db) {
        sInstance = this;
        this.mDb = db;
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
        sbuf.append(KEY_PROJECT_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_ADDRESS_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_LAST_USED);
        sbuf.append(" long)");
        mDb.execSQL(sbuf.toString());
    }

    public long add(DataProjectAddressCombo projectGroup) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_PROJECT_ID, projectGroup.projectNameId);
            values.put(KEY_ADDRESS_ID, projectGroup.addressId);
            values.put(KEY_LAST_USED, System.currentTimeMillis());
            projectGroup.id = mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjectAddressCombo.class, "add()", "db");
        } finally {
            mDb.endTransaction();
        }
        return projectGroup.id;
    }

    public boolean save(DataProjectAddressCombo projectGroup) {
        mDb.beginTransaction();
        boolean success = false;
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_PROJECT_ID, projectGroup.projectNameId);
            values.put(KEY_ADDRESS_ID, projectGroup.addressId);
            values.put(KEY_LAST_USED, System.currentTimeMillis());
            String where = KEY_ROWID + "=?";
            String [] whereArgs = new String [] {
                Long.toString(projectGroup.id)
            };
            mDb.update(TABLE_NAME, values, where, whereArgs);
            mDb.setTransactionSuccessful();
            success = true;
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjectAddressCombo.class, "save()", "db");
        } finally {
            mDb.endTransaction();
        }
        return success;
    }

    /**
     * If there are other project groups that are exactly the same as the passed,
     * then merge all entries into this one and delete the other.
     *
     * @param projectGroup
     */
    public void mergeIdenticals(DataProjectAddressCombo projectGroup) {
        List<Long> identicals = queryProjectGroupIds(projectGroup.projectNameId, projectGroup.addressId);
        if (identicals.size() <= 1) {
            return;
        }
        identicals.remove(projectGroup.id);
        for (Long other_id : identicals) {
            List<DataEntry> entries = TableEntry.getInstance().queryForProjectAddressCombo(other_id);
            Timber.i("Found " + entries.size() + " entries with matching combo id " + other_id + " to " + projectGroup.id);
            for (DataEntry entry : entries) {
                entry.projectAddressCombo = projectGroup;
                entry.uploadedMaster = false;
                TableEntry.getInstance().saveProjectAddressCombo(entry);
            }
            remove(other_id);
        }
    }

    public int count() {
        int count = 0;
        try {
            Cursor cursor = mDb.query(true, TABLE_NAME, null, null, null, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjectAddressCombo.class, "count()", "db");
        }
        return count;
    }

    public int countAddress(long addressId) {
        int count = 0;
        try {
            String selection = KEY_ADDRESS_ID + "=?";
            String [] selectionArgs = new String[] { Long.toString(addressId) };
            Cursor cursor = mDb.query(true, TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjectAddressCombo.class, "countAddress()", "db");
        }
        return count;
    }

    public int countProjects(long projectId) {
        int count = 0;
        try {
            String selection = KEY_PROJECT_ID + "=?";
            String [] selectionArgs = new String[] { Long.toString(projectId) };
            Cursor cursor = mDb.query(true, TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjectAddressCombo.class, "countProjects()", "db");
        }
        return count;
    }

    public List<DataProjectAddressCombo> query() {
        ArrayList<DataProjectAddressCombo> list = new ArrayList();
        try {
            String orderBy = KEY_LAST_USED + " DESC";
            final String[] columns = {KEY_ROWID, KEY_PROJECT_ID, KEY_ADDRESS_ID};
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, null, null, null, null, orderBy, null);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            int idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID);
            int idxAddressId = cursor.getColumnIndex(KEY_ADDRESS_ID);
            DataProjectAddressCombo item;
            while (cursor.moveToNext()) {
                long projectId = cursor.getLong(idxProjectId);
                if (!TableProjects.getInstance().isDisabled(projectId)) {
                    long id = cursor.getLong(idxRowId);
                    long addressId = cursor.getLong(idxAddressId);
                    item = new DataProjectAddressCombo(id, projectId, addressId);
                    list.add(item);
                }
            }
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjectAddressCombo.class, "query()", "db");
        }
        return list;
    }

    public DataProjectAddressCombo query(long id) {
        DataProjectAddressCombo item = null;
        try {
            final String[] columns = {KEY_PROJECT_ID, KEY_ADDRESS_ID};
            final String selection = KEY_ROWID + " =?";
            final String[] selectionArgs = {Long.toString(id)};
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            int idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID);
            int idxAddressId = cursor.getColumnIndex(KEY_ADDRESS_ID);
            if (cursor.moveToFirst()) {
                item = new DataProjectAddressCombo(id,
                        cursor.getLong(idxProjectId),
                        cursor.getLong(idxAddressId));
            }
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjectAddressCombo.class, "query(id)", "db");
        }
        return item;
    }

    public long queryProjectGroupId(long projectNameId, long addressId) {
        List<Long> ids = queryProjectGroupIds(projectNameId, addressId);
        if (ids.size() >= 1) {
            return ids.get(0);
        }
        return -1;
    }

    public ArrayList<Long> queryProjectGroupIds(long projectNameId, long addressId) {
        ArrayList<Long> ids = new ArrayList<>();
        try {
            final String[] columns = {KEY_ROWID};
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(KEY_PROJECT_ID);
            sbuf.append(" =? AND ");
            sbuf.append(KEY_ADDRESS_ID);
            sbuf.append(" =?");
            final String selection = sbuf.toString();
            final String[] selectionArgs = {Long.toString(projectNameId), Long.toString(addressId)};
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(idxRowId));
            }
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjectAddressCombo.class, "query(project,address)", "db");
        }
        return ids;
    }

    public void updateUsed(long id) {
        try {
            String where = KEY_ROWID + "=?";
            String [] whereArgs = {Long.toString(id)};
            ContentValues values = new ContentValues();
            values.put(KEY_LAST_USED, System.currentTimeMillis());
            mDb.update(TABLE_NAME, values, where, whereArgs);
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjectAddressCombo.class, "updateUsed()", "db");
        }
    }

    public void remove(long combo_id) {
        String where = KEY_ROWID + "=?";
        String[] whereArgs = {Long.toString(combo_id)};
        mDb.delete(TABLE_NAME, where, whereArgs);
    }
}

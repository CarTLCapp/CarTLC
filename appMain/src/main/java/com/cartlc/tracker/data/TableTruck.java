package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cartlc.tracker.app.TBApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 8/31/17.
 */

public class TableTruck {

    static final String TABLE_NAME = "table_trucks_v14";

    static final String KEY_ROWID         = "_id";
    static final String KEY_TRUCK_NUMBER  = "truck_number";
    static final String KEY_LICENSE_PLATE = "license_plate";
    static final String KEY_SERVER_ID     = "server_id";
    static final String KEY_PROJECT_ID    = "project_id";
    static final String KEY_COMPANY_NAME  = "company_name";
    static final String KEY_HAS_ENTRY     = "has_entry";

    static TableTruck sInstance;

    static void Init(SQLiteDatabase db) {
        new TableTruck(db);
    }

    public static TableTruck getInstance() {
        return sInstance;
    }

    final SQLiteDatabase mDb;

    TableTruck(SQLiteDatabase db) {
        sInstance = this;
        this.mDb = db;
    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(TABLE_NAME);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_TRUCK_NUMBER);
        sbuf.append(" varchar(128), ");
        sbuf.append(KEY_LICENSE_PLATE);
        sbuf.append(" varchar(128), ");
        sbuf.append(KEY_SERVER_ID);
        sbuf.append(" integer, ");
        sbuf.append(KEY_PROJECT_ID);
        sbuf.append(" int default 0, ");
        sbuf.append(KEY_COMPANY_NAME);
        sbuf.append(" varchar(256), ");
        sbuf.append(KEY_HAS_ENTRY);
        sbuf.append(" bit default 0)");
        mDb.execSQL(sbuf.toString());
    }

    public void upgrade16() {
        try {
            mDb.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEY_HAS_ENTRY + " bit default 0");
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableEntry.class, "upgrade16()", "db");
        }
    }

    // Will ensure the said truck and license plate are stored in the database.
    // Trys to take care of existing entries smartly. Prefers license plate entry
    // to truck number entries in case of duplication.
    // @Returns id of newly saved truck.
    public long save(String truckNumber, String licensePlate, long projectId, String companyName) {
        String selection;
        String[] selectionArgs;
        Cursor cursor = null;
        ContentValues values = new ContentValues();
        DataTruck truck = new DataTruck();
        if (!TextUtils.isEmpty(licensePlate)) {
            selection = KEY_LICENSE_PLATE + "=?";
            selectionArgs = new String[]{licensePlate};
            cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
            if (cursor.moveToNext()) {
                final int idxId = cursor.getColumnIndex(KEY_ROWID);
                final int idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER);
                final int idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE);
                final int idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID);
                final int idxCompanyName = cursor.getColumnIndex(KEY_COMPANY_NAME);
                truck.truckNumber = cursor.getString(idxTruckNumber);
                truck.licensePlateNumber = cursor.getString(idxLicensePlate);
                truck.projectNameId = cursor.getInt(idxProjectId);
                truck.companyName = cursor.getString(idxCompanyName);
                truck.id = cursor.getLong(idxId);
                String where = KEY_ROWID + "=?";
                String[] whereArgs = new String[]{Long.toString(truck.id)};
                boolean doUpdate = false;
                if (truckNumber != null && !truckNumber.equals(truck.truckNumber)) {
                    values.put(KEY_TRUCK_NUMBER, truckNumber);
                    doUpdate = true;
                }
                if (truck.projectNameId != projectId && projectId > 0) {
                    values.put(KEY_PROJECT_ID, projectId);
                    doUpdate = true;
                }
                if (companyName != null && !companyName.equals(truck.companyName)) {
                    values.put(KEY_COMPANY_NAME, companyName);
                    doUpdate = true;
                }
                if (doUpdate) {
                    mDb.update(TABLE_NAME, values, where, whereArgs);
                }
            } else {
                if (truckNumber != null) {
                    cursor.close();
                    selection = KEY_TRUCK_NUMBER + "=?";
                    selectionArgs = new String[]{truckNumber};
                    String[] columns = {KEY_ROWID};
                    cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
                    if (cursor.moveToNext()) {
                        int idxId = cursor.getColumnIndex(KEY_ROWID);
                        truck.id = cursor.getLong(idxId);
                        String where = KEY_ROWID + "=?";
                        String[] whereArgs = new String[]{Long.toString(truck.id)};
                        values.put(KEY_LICENSE_PLATE, licensePlate);
                        mDb.update(TABLE_NAME, values, where, whereArgs);
                    } else {
                        values.put(KEY_TRUCK_NUMBER, truckNumber);
                        values.put(KEY_LICENSE_PLATE, licensePlate);
                        truck.id = mDb.insert(TABLE_NAME, null, values);
                    }
                } else {
                    values.put(KEY_LICENSE_PLATE, licensePlate);
                }
                if (projectId > 0) {
                    values.put(KEY_PROJECT_ID, projectId);
                }
                if (!TextUtils.isEmpty(companyName)) {
                    values.put(KEY_COMPANY_NAME, companyName);
                }
                truck.id = mDb.insert(TABLE_NAME, null, values);
            }
        } else {
            if (truckNumber == null) {
                Timber.e("Invalid truck entry ignored");
                return 0;
            }
            try {
                selection = KEY_TRUCK_NUMBER + "=?";
                selectionArgs = new String[]{truckNumber};
                cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
                if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                    final int rowIdIndex = cursor.getColumnIndex(KEY_ROWID);
                    final int idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID);
                    final int idxCompanyName = cursor.getColumnIndex(KEY_COMPANY_NAME);
                    if (rowIdIndex >= 0) {
                        truck.id = cursor.getLong(rowIdIndex);
                        truck.projectNameId = cursor.getInt(idxProjectId);
                        truck.companyName = cursor.getString(idxCompanyName);
                        boolean doUpdate = false;
                        if (truck.projectNameId != projectId && projectId > 0) {
                            values.put(KEY_PROJECT_ID, projectId);
                            doUpdate = true;
                        }
                        if (companyName != null && !companyName.equals(truck.companyName)) {
                            values.put(KEY_COMPANY_NAME, companyName);
                            doUpdate = true;
                        }
                        if (doUpdate) {
                            String where = KEY_ROWID + "=?";
                            String[] whereArgs = new String[]{Long.toString(truck.id)};
                            mDb.update(TABLE_NAME, values, where, whereArgs);
                        }
                    }
                    cursor.close();
                } else {
                    cursor.close();
                    values.put(KEY_TRUCK_NUMBER, truckNumber);
                    if (projectId > 0) {
                        values.put(KEY_PROJECT_ID, projectId);
                    }
                    if (!TextUtils.isEmpty(companyName)) {
                        values.put(KEY_COMPANY_NAME, companyName);
                    }
                    truck.id = mDb.insert(TABLE_NAME, null, values);
                }
            } catch (CursorIndexOutOfBoundsException ex) {
                StringBuilder sbuf = new StringBuilder();
                sbuf.append(ex.getMessage());
                sbuf.append(" while working with truck " + truckNumber);
                TBApplication.ReportError(sbuf.toString(), TableTruck.class, "save(items)", "db");
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return truck.id;
    }

    public long save(DataTruck truck) {
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_TRUCK_NUMBER, truck.truckNumber);
            values.put(KEY_LICENSE_PLATE, truck.licensePlateNumber);
            values.put(KEY_PROJECT_ID, truck.projectNameId);
            values.put(KEY_COMPANY_NAME, truck.companyName);
            values.put(KEY_SERVER_ID, truck.serverId);
            values.put(KEY_HAS_ENTRY, truck.hasEntry);
            if (truck.id > 0) {
                String where = KEY_ROWID + "=?";
                String[] whereArgs = new String[]{Long.toString(truck.id)};
                if (mDb.update(TABLE_NAME, values, where, whereArgs) == 0) {
                    values.put(KEY_ROWID, truck.id);
                    long confirm_id = mDb.insert(TABLE_NAME, null, values);
                    if (confirm_id != truck.id) {
                        Timber.e("Did not transfer truck properly for ID " + truck.id + "...got back " + confirm_id);
                    }
                }
            } else {
                truck.id = mDb.insert(TABLE_NAME, null, values);
            }
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableTruck.class, "save(truck)", "db");
        }
        return truck.id;
    }

    public DataTruck query(long id) {
        String selection = KEY_ROWID + "=?";
        String[] selectionArgs = new String[]{Long.toString(id)};
        Cursor cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
        DataTruck truck = new DataTruck();
        if (cursor.moveToNext()) {
            final int idxId = cursor.getColumnIndex(KEY_ROWID);
            final int idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER);
            final int idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE);
            final int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
            final int idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID);
            final int idxCompanyName = cursor.getColumnIndex(KEY_COMPANY_NAME);
            final int idxHasEntry = cursor.getColumnIndex(KEY_HAS_ENTRY);
            truck.id = cursor.getLong(idxId);
            truck.truckNumber = cursor.getString(idxTruckNumber);
            truck.licensePlateNumber = cursor.getString(idxLicensePlate);
            truck.serverId = cursor.getLong(idxServerId);
            truck.projectNameId = cursor.getInt(idxProjectId);
            truck.companyName = cursor.getString(idxCompanyName);
            truck.hasEntry = cursor.getShort(idxHasEntry) != 0;
        } else {
            truck = null;
        }
        cursor.close();
        return truck;
    }

    public List<DataTruck> query() {
        return query(null, null);
    }

    public List<DataTruck> query(String selection, String[] selectionArgs) {
        Cursor cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
        final int idxId = cursor.getColumnIndex(KEY_ROWID);
        final int idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER);
        final int idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE);
        final int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
        final int idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID);
        final int idxCompanyName = cursor.getColumnIndex(KEY_COMPANY_NAME);
        final int idxHasEntry = cursor.getColumnIndex(KEY_HAS_ENTRY);
        List<DataTruck> list = new ArrayList();
        while (cursor.moveToNext()) {
            DataTruck truck = new DataTruck();
            truck.id = cursor.getLong(idxId);
            truck.truckNumber = cursor.getString(idxTruckNumber);
            truck.licensePlateNumber = cursor.getString(idxLicensePlate);
            truck.serverId = cursor.getLong(idxServerId);
            truck.projectNameId = cursor.getInt(idxProjectId);
            truck.companyName = cursor.getString(idxCompanyName);
            truck.hasEntry = cursor.getShort(idxHasEntry) != 0;
            list.add(truck);
        }
        cursor.close();
        return list;
    }

    public List<String> queryStrings(DataProjectAddressCombo curGroup) {
        String selection;
        String[] selectionArgs;
        if (curGroup != null) {
            StringBuffer sbuf = new StringBuffer();
            sbuf.append("(");
            sbuf.append(KEY_PROJECT_ID);
            sbuf.append("=? OR ");
            sbuf.append(KEY_PROJECT_ID);
            sbuf.append("=0)");
            sbuf.append(" AND ");
            sbuf.append("(");
            sbuf.append(KEY_COMPANY_NAME);
            sbuf.append("=? OR ");
            sbuf.append(KEY_COMPANY_NAME);
            sbuf.append(" IS NULL)");
            sbuf.append(" AND ");
            sbuf.append("(");
            sbuf.append(KEY_HAS_ENTRY);
            sbuf.append("=0)");
            selection = sbuf.toString();
            selectionArgs = new String[]{
                    Long.toString(curGroup.projectNameId),
                    curGroup.getCompanyName()
            };
        } else {
            selection = null;
            selectionArgs = null;
        }
        List<DataTruck> trucks = query(selection, selectionArgs);
        Collections.sort(trucks);
        ArrayList<String> list = new ArrayList<>();
        for (DataTruck truck : trucks) {
            list.add(truck.toString());
        }
        return list;
    }

    public DataTruck queryByServerId(long id) {
        String selection = KEY_SERVER_ID + "=?";
        String[] selectionArgs = new String[]{Long.toString(id)};
        Cursor cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
        DataTruck truck = new DataTruck();
        if (cursor.moveToNext()) {
            final int idxId = cursor.getColumnIndex(KEY_ROWID);
            final int idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER);
            final int idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE);
            final int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
            final int idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID);
            final int idxCompanyName = cursor.getColumnIndex(KEY_COMPANY_NAME);
            final int idxHasEntry = cursor.getColumnIndex(KEY_HAS_ENTRY);
            truck.id = cursor.getLong(idxId);
            truck.truckNumber = cursor.getString(idxTruckNumber);
            truck.licensePlateNumber = cursor.getString(idxLicensePlate);
            truck.serverId = cursor.getLong(idxServerId);
            truck.projectNameId = cursor.getInt(idxProjectId);
            truck.companyName = cursor.getString(idxCompanyName);
            truck.hasEntry = cursor.getShort(idxHasEntry) != 0;
        } else {
            truck = null;
        }
        cursor.close();
        return truck;
    }

    void remove(long id) {
        String where = KEY_ROWID + "=?";
        String[] whereArgs = {Long.toString(id)};
        mDb.delete(TABLE_NAME, where, whereArgs);
    }

    public void removeIfUnused(DataTruck truck) {
        if (TableEntry.getInstance().countTrucks(truck.id) == 0) {
            Timber.i("remove(" + truck.toString() + ")");
            remove(truck.id);
        }
    }

}

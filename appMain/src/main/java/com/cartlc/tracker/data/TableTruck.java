package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.amazonaws.util.StringUtils;
import com.cartlc.tracker.app.TBApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 8/31/17.
 */

public class TableTruck {

    static final String TABLE_NAME = "table_trucks";

    static final String KEY_ROWID         = "_id";
    static final String KEY_TRUCK_NUMBER  = "truck_number";
    static final String KEY_LICENSE_PLATE = "license_plate";
    static final String KEY_SERVER_ID     = "server_id";
    static final String KEY_PROJECT_ID    = "project_id";
    static final String KEY_COMPANY_NAME  = "company_name";

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
        sbuf.append(" int default 0, ");
        sbuf.append(KEY_LICENSE_PLATE);
        sbuf.append(" varchar(128), ");
        sbuf.append(KEY_SERVER_ID);
        sbuf.append(" integer, ");
        sbuf.append(KEY_PROJECT_ID);
        sbuf.append(" int default 0, ");
        sbuf.append(KEY_COMPANY_NAME);
        sbuf.append(" varchar(256))");
        mDb.execSQL(sbuf.toString());
    }

    public static void upgrade11(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEY_PROJECT_ID + " int default 0");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEY_COMPANY_NAME + " varchar(256)");
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableTruck.class, "upgrade11()", "db");
        }
    }
    // Will ensure the said truck and license plate are stored in the database.
    // Trys to take care of existing entries smartly. Prefers license plate entry
    // to truck number entries in case of duplication.
    // @Returns id of newly saved truck.
    public long save(long truckNumber, String licensePlate, long projectId, String companyName) {
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
                truck.truckNumber = cursor.getInt(idxTruckNumber);
                truck.licensePlateNumber = cursor.getString(idxLicensePlate);
                truck.projectNameId = cursor.getInt(idxProjectId);
                truck.companyName = cursor.getString(idxCompanyName);
                truck.id = cursor.getLong(idxId);
                String where = KEY_ROWID + "=?";
                String[] whereArgs = new String[]{Long.toString(truck.id)};
                boolean doUpdate = false;
                if ((truckNumber != truck.truckNumber) && truckNumber > 0) {
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
                if (truckNumber > 0) {
                    cursor.close();
                    selection = KEY_TRUCK_NUMBER + "=?";
                    selectionArgs = new String[]{Long.toString(truckNumber)};
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
            if (truckNumber == 0) {
                Timber.e("Invalid truck entry ignored");
                return 0;
            }
            try {
                String[] columns = {KEY_ROWID};
                selection = KEY_TRUCK_NUMBER + "=?";
                selectionArgs = new String[]{Long.toString(truckNumber)};
                cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
                if (cursor.getCount() == 0) {
                    values.put(KEY_TRUCK_NUMBER, truckNumber);
                    if (projectId > 0) {
                        values.put(KEY_PROJECT_ID, projectId);
                    }
                    if (!TextUtils.isEmpty(companyName)) {
                        values.put(KEY_COMPANY_NAME, companyName);
                    }
                    truck.id = mDb.insert(TABLE_NAME, null, values);
                } else {
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
                }
            } catch (CursorIndexOutOfBoundsException ex) {
                TBApplication.ReportError(ex, TableTruck.class, "saveUploaded()", "db");
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return truck.id;
    }

    public long save(DataTruck truck) {
        ContentValues values = new ContentValues();
        values.put(KEY_TRUCK_NUMBER, truck.truckNumber);
        values.put(KEY_LICENSE_PLATE, truck.licensePlateNumber);
        values.put(KEY_PROJECT_ID, truck.projectNameId);
        values.put(KEY_COMPANY_NAME, truck.companyName);
        values.put(KEY_SERVER_ID, truck.serverId);
        if (truck.id > 0) {
            String where = KEY_ROWID + "=?";
            String[] whereArgs = new String[]{Long.toString(truck.id)};
            mDb.update(TABLE_NAME, values, where, whereArgs);
        } else {
            truck.id = mDb.insert(TABLE_NAME, null, values);
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
            truck.id = cursor.getLong(idxId);
            truck.truckNumber = cursor.getInt(idxTruckNumber);
            truck.licensePlateNumber = cursor.getString(idxLicensePlate);
            truck.serverId = cursor.getLong(idxServerId);
            truck.projectNameId = cursor.getInt(idxProjectId);
            truck.companyName = cursor.getString(idxCompanyName);
        } else {
            truck = null;
        }
        return truck;
    }

    public List<DataTruck> query() {
        return query(null, null);
    }

    public List<DataTruck> query(String selection, String [] selectionArgs) {
        Cursor cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
        final int idxId = cursor.getColumnIndex(KEY_ROWID);
        final int idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER);
        final int idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE);
        final int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
        final int idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID);
        final int idxCompanyName = cursor.getColumnIndex(KEY_COMPANY_NAME);
        List<DataTruck> list = new ArrayList();
        while (cursor.moveToNext()) {
            DataTruck truck = new DataTruck();
            truck.id = cursor.getLong(idxId);
            truck.truckNumber = cursor.getInt(idxTruckNumber);
            truck.licensePlateNumber = cursor.getString(idxLicensePlate);
            truck.serverId = cursor.getLong(idxServerId);
            truck.projectNameId = cursor.getInt(idxProjectId);
            truck.companyName = cursor.getString(idxCompanyName);
            list.add(truck);
        }
        return list;
    }

    public List<String> queryStrings(DataProjectAddressCombo curGroup) {
        String selection;
        String [] selectionArgs;
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
            selection = sbuf.toString();
            selectionArgs = new String[]{
                    curGroup.getProjectName(),
                    curGroup.getCompanyName()
            };
        } else {
            selection = null;
            selectionArgs = null;
        }
        List<DataTruck> trucks = query(selection, selectionArgs);
        Collections.sort(trucks);
        ArrayList<String> list = new ArrayList();
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
            truck.id = cursor.getLong(idxId);
            truck.truckNumber = cursor.getInt(idxTruckNumber);
            truck.licensePlateNumber = cursor.getString(idxLicensePlate);
            truck.serverId = cursor.getLong(idxServerId);
            truck.projectNameId = cursor.getInt(idxProjectId);
            truck.companyName = cursor.getString(idxCompanyName);
        } else {
            truck = null;
        }
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

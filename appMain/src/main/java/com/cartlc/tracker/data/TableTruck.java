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

    // Will find exact match of the truck given the parameters. Otherwise will create
    // a new truck with these values.
    // @Returns id of newly saved truck.
    public long save(String truckNumber, String licensePlate, long projectId, String companyName) {
        ContentValues values = new ContentValues();
        DataTruck truck;
        List<DataTruck> trucks = query(truckNumber, licensePlate, projectId, companyName);
        if (trucks.size() > 0) {
            truck = trucks.get(0);
            if (truck.projectNameId == 0) {
                truck.projectNameId = projectId;
                values.put(KEY_PROJECT_ID, projectId);
            }
            if (truck.companyName == null) {
                truck.companyName = companyName;
                values.put(KEY_COMPANY_NAME, companyName);
            }
            truck.hasEntry = true;
            values.put(KEY_HAS_ENTRY, 1);
            String where = KEY_ROWID + "=?";
            String[] whereArgs = new String[]{Long.toString(truck.id)};
            mDb.update(TABLE_NAME, values, where, whereArgs);
        } else {
            truck = new DataTruck();
            truck.truckNumber = truckNumber;
            truck.licensePlateNumber = licensePlate;
            truck.companyName = companyName;
            truck.projectNameId = projectId;
            truck.hasEntry = true;
            values.put(KEY_TRUCK_NUMBER, truckNumber);
            values.put(KEY_COMPANY_NAME, companyName);
            values.put(KEY_PROJECT_ID, projectId);
            values.put(KEY_LICENSE_PLATE, licensePlate);
            values.put(KEY_HAS_ENTRY, 1);
            truck.id = mDb.insert(TABLE_NAME, null, values);
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
            values.put(KEY_HAS_ENTRY, truck.hasEntry ? 1 : 0);
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

    public List<DataTruck> queryByTruckNumber(int truck_number) {
        String selection = KEY_TRUCK_NUMBER + "=?";
        String [] selectionArgs = {
            Integer.toString(truck_number)
        };
        return query(selection, selectionArgs);
    }

    public List<DataTruck> queryByLicensePlate(String license_plate) {
        String selection = KEY_TRUCK_NUMBER + "=?";
        String [] selectionArgs = {
                license_plate
        };
        return query(selection, selectionArgs);
    }

    public List<DataTruck> query(String truck_number, String license_plate, long projectId, String companyName) {
        StringBuffer selection = new StringBuffer();
        ArrayList<String> selectionArgs = new ArrayList<>();

        if (truck_number != null && truck_number.trim().length() > 0) {
            selection.append(KEY_TRUCK_NUMBER);
            selection.append("=?");
            selectionArgs.add(truck_number);
        }
        if (license_plate != null && license_plate.trim().length() > 0) {
            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append(KEY_LICENSE_PLATE);
            selection.append("=?");
            selectionArgs.add(license_plate);
        }
        if (projectId > 0) {
            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append("(");
            selection.append(KEY_PROJECT_ID);
            selection.append("=? OR ");
            selection.append(KEY_PROJECT_ID);
            selection.append("=0)");
            selectionArgs.add(Long.toString(projectId));
        }
        if (companyName != null && companyName.trim().length() > 0) {
            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append("(");
            selection.append(KEY_COMPANY_NAME);
            selection.append("=? OR ");
            selection.append(KEY_COMPANY_NAME);
            selection.append(" IS NULL)");
            selectionArgs.add(companyName);
        }
        String [] selArgs = new String[selectionArgs.size()];
        return query(selection.toString(), selectionArgs.toArray(selArgs));
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
